package org.openurp.edu.eams.teach.program.major.web.action

import javax.persistence.EntityNotFoundException
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.teach.Course
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseBean
import org.openurp.edu.eams.teach.program.major.service.MajorPlanCourseService
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import com.ekingstar.eams.web.action.BaseAction
//remove if not needed


class MajorPlanCourseAction extends BaseAction {

  private var majorPlanCourseService: MajorPlanCourseService = _

  private var majorPlanService: MajorPlanService = _

  def save(): String = {
    val o = extractMajorPlanAndGroup()
    val plan = o.getLeft
    val group = o.getRight
    val planCourse = populateEntity(classOf[MajorPlanCourse], "planCourse")
    val terms = get("planCourse.terms")
    planCourse.setTerms(PlanTermCreditTool.normalizeTerms(terms))
    val extra = "&courseGroup.id=" + group.id + "&planId=" + plan.id
    if (planCourse.isPersisted) {
      if (majorPlanService.hasCourse(group, planCourse.getCourse, planCourse)) {
        getFlash.put("params", get("params"))
        return redirect(new Action(classOf[MajorCourseGroupAction], "arrangeGroupCourses", extra), 
          "teachPlan.duplicate.course")
      }
      majorPlanCourseService.updatePlanCourse(planCourse, plan)
    } else {
      if (majorPlanService.hasCourse(group, planCourse.getCourse)) {
        getFlash.put("params", get("params"))
        return redirect(new Action(classOf[MajorCourseGroupAction], "arrangeGroupCourses", extra), 
          "teachPlan.duplicate.course")
      }
      majorPlanCourseService.addPlanCourse(planCourse, plan)
    }
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorCourseGroupAction], "arrangeGroupCourses", extra), "info.save.success")
  }

  def batchAddCourses(): String = {
    val courseIds = getLongIds("course")
    val o = extractMajorPlanAndGroup()
    val plan = o.getLeft
    val group = o.getRight
    val extra = "&courseGroup.id=" + group.id + "&planId=" + plan.id
    val msg = new StringBuffer()
    var errorNum = 0
    for (courseId <- courseIds) {
      val planCourse = new MajorPlanCourseBean()
      val terms = get("course." + courseId + ".terms")
      planCourse.setTerms(PlanTermCreditTool.normalizeTerms(terms))
      planCourse.setCourseGroup(group)
      val course = entityDao.get(classOf[Course], courseId)
      planCourse.setCourse(course)
      planCourse.setCompulsory(getBool("course." + courseId + ".compulsory"))
      planCourse.setDepartment(entityDao.get(classOf[Department], getInt("course." + courseId + ".department.id")))
      if (majorPlanService.hasCourse(group, planCourse.getCourse)) {
        getFlash.put("params", get("params"))
        errorNum += 1
        msg.append("\n失败：").append(course.getCode).append(" ")
          .append(course.getName)
        //continue
      }
      majorPlanCourseService.addPlanCourse(planCourse, plan)
      msg.append("\n成功：").append(course.getCode).append(" ")
        .append(course.getName)
    }
    getFlash.put("params", get("params"))
    getFlash.put("message", ("\n添加 " + courseIds.length + ";成功 " + (courseIds.length - errorNum) + 
      ";失败 " + 
      errorNum) + 
      msg)
    redirect(new Action(classOf[MajorCourseGroupAction], "arrangeGroupCourses", extra), "")
  }

  def batchEditCourses(): String = {
    val planCourseIds = Strings.splitToLong(get("planCourse_Ids"))
    val o = extractMajorPlanAndGroup()
    val plan = o.getLeft
    val group = o.getRight
    val extra = "&courseGroup.id=" + group.id + "&planId=" + plan.id
    for (planCourseId <- planCourseIds) {
      val planCourse = entityDao.get(classOf[MajorPlanCourse], planCourseId)
      planCourse.setTerms(PlanTermCreditTool.normalizeTerms(get("planCourse_." + planCourseId + ".terms")))
      planCourse.setCompulsory(getBool("planCourse_." + planCourseId + ".compulsory"))
      planCourse.setDepartment(entityDao.get(classOf[Department], getInt("planCourse_." + planCourseId + ".department.id")))
      majorPlanCourseService.updatePlanCourse(planCourse, plan)
    }
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorCourseGroupAction], "arrangeGroupCourses", extra), "info.save.success")
  }

  def remove(): String = {
    val pair = extractMajorPlanAndGroup()
    val plan = pair.getLeft
    val group = pair.getRight
    val planCourseIds = getLongIds("planCourse")
    for (i <- 0 until planCourseIds.length) {
      val planCourse = entityDao.get(classOf[MajorPlanCourse], planCourseIds(i))
      majorPlanCourseService.removePlanCourse(planCourse, plan)
    }
    getFlash.put("params", get("params"))
    val extra = "&courseGroup.id=" + group.id + "&planId=" + plan.id
    redirect(new Action(classOf[MajorCourseGroupAction], "arrangeGroupCourses", extra), "info.delete.success")
  }

  private def extractMajorPlanAndGroup(): Pair[MajorPlan, MajorCourseGroup] = {
    val planId = getLong("planId")
    var groupId = getLong("planCourse.courseGroup.id")
    if (null == groupId) {
      groupId = getLong("courseGroup.id")
    }
    if (null == planId || null == groupId) {
      throw new EntityNotFoundException("plan id or planCourse id")
    }
    val plan = entityDao.get(classOf[MajorPlan], planId)
    val group = entityDao.get(classOf[MajorCourseGroup], groupId)
    new Pair[MajorPlan, MajorCourseGroup](plan, group)
  }

  def setMajorPlanCourseService(majorPlanCourseService: MajorPlanCourseService) {
    this.majorPlanCourseService = majorPlanCourseService
  }

  def setMajorPlanService(majorPlanService: MajorPlanService) {
    this.majorPlanService = majorPlanService
  }

  def getEntityName(): String = classOf[MajorPlanCourse].getName
}
