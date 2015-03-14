package org.openurp.edu.eams.teach.program.personal.web.action

import javax.persistence.EntityNotFoundException
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.teach.Course
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourseGroup
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanCourseBean
import org.openurp.edu.eams.teach.program.personal.service.PersonalPlanCourseService
import com.ekingstar.eams.web.action.BaseAction
//remove if not needed
import scala.collection.JavaConversions._

class PersonalPlanCourseAction extends BaseAction {

  protected var planCommonDao: PlanCommonDao = _

  private var personalPlanCourseService: PersonalPlanCourseService = _

  def save(): String = {
    val o = extractPlanAndGroup()
    val plan = o.getLeft
    val group = o.getRight
    val planCourse = populateEntity(classOf[PersonalPlanCourse], "planCourse")
    val terms = get("planCourse.terms")
    planCourse.setTerms(PlanTermCreditTool.normalizeTerms(terms))
    val extra = "&courseGroup.id=" + group.getId + "&planId=" + plan.getId
    if (planCourse.isPersisted) {
      personalPlanCourseService.updatePlanCourse(planCourse, plan)
    } else {
      if (planCommonDao.hasCourse(group, planCourse.getCourse)) {
        getFlash.put("params", get("params"))
        return redirect(new Action(classOf[PersonalPlanCourseGroupAction], "arrangeGroupCourses", extra), 
          "teachPlan.duplicate.course")
      }
      personalPlanCourseService.addPlanCourse(planCourse, plan)
    }
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[PersonalPlanCourseGroupAction], "arrangeGroupCourses", extra), "info.save.success")
  }

  def batchAddCourses(): String = {
    val courseIds = getLongIds("course")
    val o = extractPlanAndGroup()
    val plan = o.getLeft
    val group = o.getRight
    val extra = "&courseGroup.id=" + group.getId + "&planId=" + plan.getId
    val msg = new StringBuffer()
    var errorNum = 0
    for (courseId <- courseIds) {
      val planCourse = new PersonalPlanCourseBean()
      val terms = get("course." + courseId + ".terms")
      planCourse.setTerms(PlanTermCreditTool.normalizeTerms(terms))
      planCourse.setCourseGroup(group)
      val course = entityDao.get(classOf[Course], courseId)
      planCourse.setCourse(course)
      planCourse.setCompulsory(getBool("course." + courseId + ".compulsory"))
      planCourse.setDepartment(entityDao.get(classOf[Department], getInt("course." + courseId + ".department.id")))
      if (planCommonDao.hasCourse(group, planCourse.getCourse)) {
        getFlash.put("params", get("params"))
        errorNum += 1
        msg.append("\n失败：").append(course.getCode).append(" ")
          .append(course.getName)
        //continue
      }
      personalPlanCourseService.addPlanCourse(planCourse, plan)
      msg.append("\n成功：").append(course.getCode).append(" ")
        .append(course.getName)
    }
    getFlash.put("params", get("params"))
    getFlash.put("message", ("\n添加 " + courseIds.length + ";成功 " + (courseIds.length - errorNum) + 
      ";失败 " + 
      errorNum) + 
      msg)
    redirect(new Action(classOf[PersonalPlanCourseGroupAction], "arrangeGroupCourses", extra), "")
  }

  def batchEditCourses(): String = {
    val planCourseIds = Strings.splitToLong(get("planCourse_Ids"))
    val o = extractPlanAndGroup()
    val plan = o.getLeft
    val group = o.getRight
    val extra = "&courseGroup.id=" + group.getId + "&planId=" + plan.getId
    for (planCourseId <- planCourseIds) {
      val planCourse = entityDao.get(classOf[PersonalPlanCourse], planCourseId)
      planCourse.setTerms(PlanTermCreditTool.normalizeTerms(get("planCourse_." + planCourseId + ".terms")))
      planCourse.setCompulsory(getBool("planCourse_." + planCourseId + ".compulsory"))
      planCourse.setDepartment(entityDao.get(classOf[Department], getInt("planCourse_." + planCourseId + ".department.id")))
      personalPlanCourseService.addPlanCourse(planCourse, plan)
    }
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[PersonalPlanCourseGroupAction], "arrangeGroupCourses", extra), "info.save.success")
  }

  def remove(): String = {
    val pair = extractPlanAndGroup()
    val plan = pair.getLeft
    val group = pair.getRight
    val planCourseIds = Strings.splitToLong(get("planCourseIds"))
    for (i <- 0 until planCourseIds.length) {
      val planCourse = entityDao.get(classOf[PersonalPlanCourse], planCourseIds(i))
      personalPlanCourseService.removePlanCourse(planCourse, plan)
    }
    getFlash.put("params", get("params"))
    val extra = "&courseGroup.id=" + group.getId + "&planId=" + plan.getId
    redirect(new Action(classOf[PersonalPlanCourseGroupAction], "arrangeGroupCourses", extra), "info.delete.success")
  }

  private def extractPlanAndGroup(): Pair[PersonalPlan, PersonalPlanCourseGroup] = {
    val planId = getLong("planId")
    var groupId = getLong("planCourse.courseGroup.id")
    if (null == groupId) {
      groupId = getLong("courseGroup.id")
    }
    if (null == planId || null == groupId) {
      throw new EntityNotFoundException("plan id or planCourse id")
    }
    val plan = entityDao.get(classOf[PersonalPlan], planId)
    val group = entityDao.get(classOf[PersonalPlanCourseGroup], groupId)
    new Pair[PersonalPlan, PersonalPlanCourseGroup](plan, group)
  }

  def setPersonalPlanCourseService(personalPlanCourseService: PersonalPlanCourseService) {
    this.personalPlanCourseService = personalPlanCourseService
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }
}
