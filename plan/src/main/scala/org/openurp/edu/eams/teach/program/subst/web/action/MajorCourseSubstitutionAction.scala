package org.openurp.edu.eams.teach.program.subst.web.action

import java.sql.Date




import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.lesson.Lesson
import org.openurp.edu.eams.teach.program.MajorCourseSubstitution
import org.openurp.edu.eams.teach.program.model.MajorCourseSubstitutionBean
import com.ekingstar.eams.teach.service.CourseService
import com.ekingstar.eams.web.action.common.RestrictionSupportAction
//remove if not needed


class MajorCourseSubstitutionAction extends RestrictionSupportAction {

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

  def search(): String = {
    val builder = OqlBuilder.from(classOf[MajorCourseSubstitution], "majorCourseSubstitution")
    populateConditions(builder)
    val originCode = get("originCode")
    val originName = get("originName")
    val substituteCode = get("substituteCode")
    val substituteName = get("substituteName")
    if (Strings.isNotEmpty(originCode)) {
      builder.where("exists(from majorCourseSubstitution.origins origin where origin.code like '%" + 
        originCode.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isNotEmpty(originName)) {
      builder.where("exists(from majorCourseSubstitution.origins origin where origin.name like '%" + 
        originName.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isNotEmpty(substituteCode)) {
      builder.where("exists(from majorCourseSubstitution.substitutes substitute where substitute.code like '%" + 
        substituteCode.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isNotEmpty(substituteName)) {
      builder.where("exists(from majorCourseSubstitution.substitutes substitute where substitute.name like '%" + 
        substituteName.trim().replaceAll("'", "") + 
        "%')")
    }
    if (Strings.isBlank(get(Order.ORDER_STR))) {
      builder.orderBy("majorCourseSubstitution.grades desc")
    } else {
      builder.orderBy(get(Order.ORDER_STR))
    }
    builder.limit(getPageLimit)
    put("majorCourseSubstitutions", entityDao.search(builder))
    forward()
  }

  protected def editSetting(entity: Entity) {
    val projectId = getInt("majorCourseSubstitution.major.project.id")
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.project.id=:projectId", projectId)
    val lessonList = entityDao.search(builder)
    val lessonStr = new ArrayList()
    for (lesson <- lessonList) {
      val str = lesson.getCourse.getCode + "[" + lesson.getCourse.getName + 
        "]"
      lessonStr.add(str)
    }
    put("lessonList", lessonStr)
    indexSetting()
  }

  def saveAndForward(entity: Entity): String = {
    val majorCourseSubstitution = entity.asInstanceOf[MajorCourseSubstitution]
    val originCodesStr = get("originCodes")
    val substituteCodesStr = get("substituteCodes")
    fillCourse(majorCourseSubstitution.getOrigins, originCodesStr)
    fillCourse(majorCourseSubstitution.getSubstitutes, substituteCodesStr)
    if (majorCourseSubstitution.isTransient) {
      majorCourseSubstitution.asInstanceOf[MajorCourseSubstitutionBean]
        .setCreatedAt(new Date(System.currentTimeMillis()))
    }
    if (majorCourseSubstitution.getOrigins.isEmpty || majorCourseSubstitution.getSubstitutes.isEmpty) {
      editSetting(entity)
      addMessage(getText("info.save.failure"))
      put("majorCourseSubstitution", majorCourseSubstitution)
      forward("edit")
    } else {
      val builder = OqlBuilder.from(classOf[MajorCourseSubstitution], "majorCourseSubstitution")
      builder.where("majorCourseSubstitution.grades=:grades", majorCourseSubstitution.getGrades)
        .where("majorCourseSubstitution.education.id=:educationId", majorCourseSubstitution.getEducation.id)
        .where("majorCourseSubstitution.project.id=:projectId", majorCourseSubstitution.getProject.id)
      if (majorCourseSubstitution.isPersisted) {
        builder.where("majorCourseSubstitution.id != :majorCourseSubstitutionId", majorCourseSubstitution.id)
      }
      if (getLong("majorCourseSubstitution.department.id") != null) {
        builder.where("majorCourseSubstitution.department.id=:departmentId", majorCourseSubstitution.getDepartment.id)
      } else {
        builder.where("majorCourseSubstitution.department.id is null")
      }
      if (getLong("majorCourseSubstitution.stdType.id") != null) {
        builder.where("majorCourseSubstitution.stdType.id=:stdTypeId", majorCourseSubstitution.getStdType.id)
      } else {
        builder.where("majorCourseSubstitution.stdType.id is null")
      }
      if (getLong("majorCourseSubstitution.major.id") != null) {
        builder.where("majorCourseSubstitution.major.id=:majorId", majorCourseSubstitution.getMajor.id)
      } else {
        builder.where("majorCourseSubstitution.major.id is null")
      }
      if (getLong("majorCourseSubstitution.direction.id") != null) {
        builder.where("majorCourseSubstitution.direction.id=:directionId", majorCourseSubstitution.getDirection.id)
      } else {
        builder.where("majorCourseSubstitution.direction.id is null")
      }
      val majorCourseSubstitutions = entityDao.search(builder)
      if (majorCourseSubstitutions.size > 0) {
        for (majorCourseSub <- majorCourseSubstitutions if majorCourseSub.getOrigins == majorCourseSubstitution.getOrigins && 
          majorCourseSub.getSubstitutes == majorCourseSubstitution.getSubstitutes) {
          return redirect("search", "该替代课程组合已存在!")
        }
      }
      if (isDoubleCourseSubstitution(majorCourseSubstitution)) {
        entityDao.saveOrUpdate(majorCourseSubstitution)
        redirect("search", "info.save.success")
      } else {
        redirect("search", "原课程与替代课程一样!")
      }
    }
  }

  private def isDoubleCourseSubstitution(majorCourseSubstitution: MajorCourseSubstitution): Boolean = {
    var bool = false
    val courseOrigins = majorCourseSubstitution.getOrigins
    val courseSubstitutes = majorCourseSubstitution.getSubstitutes
    for (Origin <- courseOrigins if !courseSubstitutes.contains(Origin)) {
      bool = true
    }
    for (Substitute <- courseSubstitutes if !courseOrigins.contains(Substitute)) {
      bool = true
    }
    bool
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

  def getEntityName(): String = {
    classOf[MajorCourseSubstitution].getName
  }
}
