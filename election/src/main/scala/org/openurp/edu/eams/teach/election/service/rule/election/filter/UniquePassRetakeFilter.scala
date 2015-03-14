package org.openurp.edu.eams.teach.election.service.rule.election.filter

import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class UniquePassRetakeFilter extends AbstractElectableLessonFilter with ElectRulePrepare {

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    val courseId = lesson.getCourse.getId
    val retakeCourseId = state.getOriginCourseId(courseId)
    if (null != retakeCourseId) {
      val passedRetakedCourseIds = state.getParams.get("passedRetakedCourseIds").asInstanceOf[Set[Long]]
      return !passedRetakedCourseIds.contains(retakeCourseId)
    }
    true
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("你已经重修过该课程,及格重修只能申请一次", ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }

  def prepare(context: PrepareContext) {
    val builder = OqlBuilder.from(classOf[CourseGrade].getName + " grade")
    builder.where("grade.std = :std", context.getStudent)
    builder.where("grade.project = :project", context.getStudent.getProject)
    builder.where("grade.courseTakeType.id=:retakeId", CourseTakeType.RESTUDY)
    builder.where("exists(from " + classOf[CourseGrade].getName + " passedGrade " + 
      "where passedGrade.passed=true " + 
      "and passedGrade.course=grade.course " + 
      "and passedGrade.std=grade.std " + 
      "and passed.project=grade.project " + 
      "and passedGrade.courseTakeType!=grade.courseTakeType)")
    builder.select("grade.course.id")
    context.getState.getParams.put("passedRetakedCourseIds", CollectUtils.newHashSet(entityDao.search(builder)))
  }
}
