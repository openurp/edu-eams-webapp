package org.openurp.edu.eams.teach.election.service.rule.election

import org.beangle.ems.rule.Context
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.lesson.CourseLimitGroup



class LimitCountChecker extends AbstractElectRuleExecutor() with ElectRulePrepare {

  order = java.lang.Integer.MAX_VALUE

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val limitGroup = CourseLimitGroupHelper.getMatchCountCourseLimitGroup(electContext.getLesson, electContext.getState)
    if (limitGroup == null) {
    } else {
      val sql = "update t_course_limit_groups set cur_count = cur_count+1 where (cur_count<max_count or max_count=0) and id=?"
      val update = electionDao.updateStdCount(sql, limitGroup.id)
      if (update == 0) {
        context.addMessage(new ElectMessage("人数已满", ElectRuleType.ELECTION, false, electContext.getLesson))
        return false
      } else {
        electContext.getCourseTake.setLimitGroup(limitGroup)
      }
    }
    var sql = "update t_lessons set std_count=std_count+1 where std_count<(limit_count-reserved_count) and id=?"
    val update = electionDao.updateStdCount(sql, electContext.getLesson.id)
    if (update == 0) {
      context.addMessage(new ElectMessage("人数已满", ElectRuleType.ELECTION, false, electContext.getLesson))
      sql = "update t_course_limit_groups lg set lg.cur_count = (select count(*) from t_course_takes xk where xk.limit_group_id=lg.id) where lg.id=?"
      electionDao.updateStdCount(sql, limitGroup.id)
      return false
    }
    true
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.CHECK_MAX_LIMIT_COUNT)) {
      context.getState.setCheckMaxLimitCount(true)
      context.addPreparedDataName(PreparedDataName.CHECK_MAX_LIMIT_COUNT)
    }
  }
}
