package org.openurp.edu.eams.teach.election.service.rule.withdraw

import java.util.Date

import org.beangle.commons.collection.CollectUtils
import org.beangle.ems.rule.Context
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import WithdrawTimePrepare._



object WithdrawTimePrepare {

  private val ERROR_WITHDRAW_TIME = "error.elect.WithdrawTime"
}

class WithdrawTimePrepare extends AbstractElectRuleExecutor with ElectRulePrepare {

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val unsuitableLessonIds = electContext.getState.getParams.get("unsuitableLessonIds").asInstanceOf[Set[Long]]
    val lesson = electContext.getLesson
    val result = !unsuitableLessonIds.contains(lesson.id)
    if (!result) {
      electContext.addMessage(new ElectMessage(ERROR_WITHDRAW_TIME, ElectRuleType.WITHDRAW, false, lesson))
    }
    result
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.ERROR_WITHDRAW_TIME)) {
      val profile = context.getState.getProfile(entityDao)
      val unsuitableLessonIds = CollectUtils.newHashSet()
      for (take <- context.getTakes) {
        val date = take.getCreatedAt
        var electedInProfileOpenTime = true
        electedInProfileOpenTime = if (null != date) date.after(profile.getBeginAt) && date.before(profile.getEndAt) else false
        if (!electedInProfileOpenTime) {
          unsuitableLessonIds.add(take.getLesson.getCourse.id)
          context.getState.getUnWithdrawableLessonIds.put(take.getLesson.id, ERROR_WITHDRAW_TIME)
        }
      }
      context.addPreparedDataName(PreparedDataName.ERROR_WITHDRAW_TIME)
      context.getState.getParams.put("unsuitableLessonIds", unsuitableLessonIds)
    }
  }
}
