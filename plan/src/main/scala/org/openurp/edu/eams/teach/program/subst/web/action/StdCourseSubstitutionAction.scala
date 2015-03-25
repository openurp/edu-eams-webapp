package org.openurp.edu.eams.teach.program.subst.web.action

import java.sql.Date




import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.core.Student
import com.ekingstar.eams.teach.Course
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.StdCourseSubstitution
import org.openurp.edu.eams.teach.program.model.StdCourseSubstitutionBean
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import com.ekingstar.eams.teach.service.CourseService
import com.ekingstar.eams.web.action.common.RestrictionSupportAction
//remove if not needed


class StdCourseSubstitutionAction extends RestrictionSupportAction {

  var coursePlanProvider: CoursePlanProvider = _

  var courseService: CourseService = _

  protected def getTopDeparts(): List[_] = {
    val topDeparts = new ArrayList()
    val departs = getDeparts
    var iter = departs.iterator()
    while (iter.hasNext) {
      val depart = iter.next().asInstanceOf[Department]
      if (null == depart.getParent) {
        topDeparts.add(depart)
      }
    }
    topDeparts
  }

  def courses(): String = {
    val studentCode = get("studentCode")
    val students = entityDao.get(classOf[Student], "code", studentCode)
    val courses = new ArrayList[Course]()
    if (CollectUtils.isNotEmpty(students)) {
      val plan = coursePlanProvider.getCoursePlan(students.get(0))
      if (plan != null) {
        for (courseGroup <- plan.getGroups; planCourse <- courseGroup.getPlanCourses) {
          courses.add(planCourse.getCourse)
        }
      }
      put("courses", courses)
    }
    forward()
  }

  def search(): String = {
    val builder = OqlBuilder.from(classOf[StdCourseSubstitution], "stdCourseSubstitution")
    populateConditions(builder)
    val originCode = get("originCode")
    val originName = get("originName")
    val substituteCode = get("substituteCode")
    val substituteName = get("substituteName")
    if (Strings.isNotEmpty(originCode)) {
      builder.where("exists(from stdCourseSubstitution.origins origin where origin.code like '%" + 
        originCode.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isNotEmpty(originName)) {
      builder.where("exists(from stdCourseSubstitution.origins origin where origin.name like '%" + 
        originName.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isNotEmpty(substituteCode)) {
      builder.where("exists(from stdCourseSubstitution.substitutes substitute where substitute.code like '%" + 
        substituteCode.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isNotEmpty(substituteName)) {
      builder.where("exists(from stdCourseSubstitution.substitutes substitute where substitute.name like '%" + 
        substituteName.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isBlank(get(Order.ORDER_STR))) {
      builder.orderBy("stdCourseSubstitution.std.code desc")
    } else {
      builder.orderBy(get(Order.ORDER_STR))
    }
    builder.where("stdCourseSubstitution.std.department in (:departments)", getDeparts)
    if (CollectUtils.isNotEmpty(getStdTypes)) {
      builder.where("stdCourseSubstitution.std.type in (:stdTypes)", getStdTypes)
    }
    if (CollectUtils.isNotEmpty(getEducations)) {
      builder.where("stdCourseSubstitution.std.education in (:educations)", getEducations)
    }
    builder.limit(getPageLimit)
    put("stdCourseSubstitutions", entityDao.search(builder))
    forward()
  }

  def saveAndForward(entity: Entity): String = {
    val stdCourseSubstitution = entity.asInstanceOf[StdCourseSubstitution]
    var originCodesStr = get("originCodes")
    if (originCodesStr.length > 0 && originCodesStr.substring(0, 1) == ",") {
      originCodesStr = originCodesStr.substring(1, originCodesStr.length)
    }
    val substituteCodesStr = get("substituteCodes")
    fillCourse(stdCourseSubstitution.getOrigins, originCodesStr)
    fillCourse(stdCourseSubstitution.getSubstitutes, substituteCodesStr)
    var stdCourseSubId = 0
    if (stdCourseSubstitution.isTransient) {
      stdCourseSubstitution.asInstanceOf[StdCourseSubstitutionBean]
        .setCreatedAt(new Date(System.currentTimeMillis()))
    } else {
      stdCourseSubId = stdCourseSubstitution.id
    }
    if (stdCourseSubstitution.getOrigins.isEmpty || stdCourseSubstitution.getSubstitutes.isEmpty) {
      editSetting(entity)
      addMessage(getText("info.save.failure"))
      put("stdCourseSubstitution", stdCourseSubstitution)
      forward("edit")
    } else {
      val builder = OqlBuilder.from(classOf[StdCourseSubstitution], "stdCourseSubstitution")
      builder.where("stdCourseSubstitution.std.id=:stdId", stdCourseSubstitution.getStd.id)
        .where("stdCourseSubstitution.std.project= :project", getProject)
      if (stdCourseSubId != 0) {
        builder.where("stdCourseSubstitution.id !=:stdCourseSubId", stdCourseSubId)
      }
      val stdCourseSubstitutions = entityDao.search(builder)
      if (stdCourseSubstitutions.size > 0) {
        for (stdCourseSub <- stdCourseSubstitutions if stdCourseSub.getOrigins == stdCourseSubstitution.getOrigins && 
          stdCourseSub.getSubstitutes == stdCourseSubstitution.getSubstitutes) {
          return redirect("search", "该替代课程组合已存在!")
        }
      }
      if (isDoubleCourseSubstitution(stdCourseSubstitution)) {
        entityDao.saveOrUpdate(stdCourseSubstitution)
        redirect("search", "info.save.success")
      } else {
        redirect("search", "原课程与替代课程一样!")
      }
    }
  }

  private def fillCourse(courses: Set[_], courseCodeSeq: String) {
    val courseCodes = Strings.split(courseCodeSeq, ",")
    courses.clear()
    if (courseCodes != null) {
      for (i <- 0 until courseCodes.length) {
        val course = courseService.getCourse(courseCodes(i))
        if (null != course) {
          courses.add(course)
        }
      }
    }
  }

  private def isDoubleCourseSubstitution(stdCourseSubstitution: StdCourseSubstitution): Boolean = {
    var bool = false
    val courseOrigins = stdCourseSubstitution.getOrigins
    val courseSubstitutes = stdCourseSubstitution.getSubstitutes
    for (Origin <- courseOrigins if !courseSubstitutes.contains(Origin)) {
      bool = true
    }
    for (Substitute <- courseSubstitutes if !courseOrigins.contains(Substitute)) {
      bool = true
    }
    bool
  }

  def getEntityName(): String = classOf[StdCourseSubstitution].getName
}
