package org.openurp.edu.eams.teach.election.service.rule.general

import java.util.Date
import java.util.Iterator
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.ems.rule.Context
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.teach.lesson.CourseTake

import scala.collection.JavaConversions._

class EvaluationChecker extends AbstractElectRuleExecutor {

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    var isEvaluted = electContext.getState.getParams.get("isEvaluted").asInstanceOf[java.lang.Boolean]
    if (null != isEvaluted) return isEvaluted.booleanValue()
    var query = OqlBuilder.from("org.openurp.edu.eams.quality.evaluate.course.model.EvaluateSwitch es")
    query.where("es.project.id=:projectId", electContext.getState.getStd.getProjectId)
    query.where(":date between es.openAt and es.closeAt", new Date())
    query.where("es.isOpen=true")
    query.select("es.semester")
    var semesters = entityDao.search(query)
    if (semesters.isEmpty) {
      isEvaluted = true
    } else {
      query = OqlBuilder.from(classOf[CourseTake], "courseTake")
      query.where("courseTake.std =:std", electContext.getStudent)
      query.where("courseTake.lesson.semester in (:semesters)", semesters)
      query.where("not exists(from org.openurp.edu.eams.quality.evaluate.course.model.EvaluateResult er  where er.student=courseTake.std and er.lesson = courseTake.lesson) ")
      query.where("exists(from org.openurp.edu.eams.quality.evaluate.course.model.QuestionnaireLesson ql where ql.lesson=courseTake.lesson)")
      query.select("distinct courseTake.lesson.semester")
      semesters = entityDao.search(query)
      isEvaluted = CollectUtils.isEmpty(semesters)
    }
    if (isEvaluted) {
      electContext.getState.getParams.put("isEvaluted", isEvaluted)
    }
    if (!isEvaluted) {
      val sb = new StringBuilder()
      var iterator = semesters.iterator()
      while (iterator.hasNext) {
        val semester = iterator.next().asInstanceOf[Semester]
        sb.append(semester.getSchoolYear).append(" 第").append(semester.getName)
          .append("学期")
        if (iterator.hasNext) {
          sb.append(',')
        }
      }
      context.addMessage(new ElectMessage("请先完成评教：" + sb.toString, ElectRuleType.GENERAL, false, null))
      return false
    }
    true
  }
}
