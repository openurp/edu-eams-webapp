package org.openurp.edu.eams.teach.program.log


import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


abstract class PhaseChain(protected var informations: Map[String, String]) {

  protected def getTeachPlanName(plan: MajorPlan): String = {
    val sb = new StringBuilder()
    sb.append(plan.getProgram.getGrade).append(' ').append(plan.getProgram.getMajor.getName)
      .append(' ')
      .append(if (plan.getProgram.getDirection == null) "" else plan.getProgram.getDirection.getName)
      .append(" 培养计划")
    sb.toString
  }

  protected def initialLogInfo(plan: MajorPlan): Map[String, String] = {
    informations.put(MajorPlanLogHelper.PLAN_ID, String.valueOf(plan.id))
    informations.put(MajorPlanLogHelper.PLAN_NAME, getTeachPlanName(plan))
    informations.put(MajorPlanLogHelper.OBJECT_ID, plan.id.toString)
    informations.put(MajorPlanLogHelper.OBJECT_NAME, getTeachPlanName(plan))
    informations
  }

  protected def initialLogInfo(courseGroup: MajorCourseGroup): Map[String, String] = {
    val plan = courseGroup.getPlan.asInstanceOf[MajorPlan]
    informations.put(MajorPlanLogHelper.PLAN_ID, String.valueOf(plan.id))
    informations.put(MajorPlanLogHelper.PLAN_NAME, getTeachPlanName(plan))
    informations.put(MajorPlanLogHelper.OBJECT_ID, courseGroup.id.toString)
    informations.put(MajorPlanLogHelper.OBJECT_NAME, courseGroup.getName)
    informations.put(MajorPlanLogHelper.OBJECT_CODE, courseGroup.getCourseType.getCode)
    informations
  }

  protected def initialLogInfo(planCourse: MajorPlanCourse): Map[String, String] = {
    val plan = planCourse.getCourseGroup.getPlan.asInstanceOf[MajorPlan]
    informations.put(MajorPlanLogHelper.PLAN_ID, String.valueOf(plan.id))
    informations.put(MajorPlanLogHelper.PLAN_NAME, getTeachPlanName(plan))
    informations.put(MajorPlanLogHelper.OBJECT_ID, planCourse.id.toString)
    informations.put(MajorPlanLogHelper.OBJECT_NAME, planCourse.getCourse.getName)
    informations.put(MajorPlanLogHelper.OBJECT_CODE, planCourse.getCourse.getCode)
    informations
  }
}
