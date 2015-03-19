package org.openurp.edu.eams.teach.election.service.rule.election.filter




import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.eams.teach.code.school.CourseAbilityRate
import org.openurp.edu.base.Course.StdCourseAbility
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.lesson.Lesson
import ElectableLessonByCourseAbilityRateFilter._



object ElectableLessonByCourseAbilityRateFilter {

  private val PARAM_NAME_STD = "ABILITIES"
}

class ElectableLessonByCourseAbilityRateFilter extends AbstractElectableLessonFilter with ElectRulePrepare {

  order = AbstractElectRuleExecutor.Priority.FIRST.ordinal()

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.COURSE_ABILITY_RATE)) {
      val builder = OqlBuilder.from(classOf[StdCourseAbility].getName + " ability")
      builder.where("ability.std.id=:stdId", context.getState.getStd.id)
        .where("ability.published=true")
      builder.select("ability.abilityRate.id")
      val abilityRateIds = entityDao.search(builder)
      context.getState.getParams.put(PARAM_NAME_STD, new HashSet[Long](abilityRateIds))
      context.addPreparedDataName(PreparedDataName.COURSE_ABILITY_RATE)
    }
  }

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    if (lesson.getCourse.getAbilityRates.isEmpty) {
      return true
    }
    for (abilityRate <- lesson.getCourse.getAbilityRates if state.getParams.get(PARAM_NAME_STD).asInstanceOf[Set[_]]
      .contains(abilityRate.id)) {
      return true
    }
    false
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("没有达到课程等级要求", ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }
}
