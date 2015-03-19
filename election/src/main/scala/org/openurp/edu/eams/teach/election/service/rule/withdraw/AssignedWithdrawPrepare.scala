package org.openurp.edu.eams.teach.election.service.rule.withdraw


import org.beangle.commons.collection.CollectUtils
import org.beangle.ems.rule.Context
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.AbstractElectRuleExecutor
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.lesson.CourseTake
import AssignedWithdrawPrepare._



object AssignedWithdrawPrepare {

  val ERROR_WITHDRAW_ASSIGNED = "error.elect.withdrawAssigned"
}

class AssignedWithdrawPrepare extends AbstractElectRuleExecutor with ElectRulePrepare {

  def execute(context: Context): Boolean = {
    val electContext = context.asInstanceOf[ElectionCourseContext]
    val assignedLessonIds = electContext.getState.getParams.get("assignedLessonIds").asInstanceOf[Set[Long]]
    val result = !assignedLessonIds.contains(electContext.getLesson.id)
    if (!result) {
      electContext.addMessage(new ElectMessage(ERROR_WITHDRAW_ASSIGNED, ElectRuleType.WITHDRAW, false, 
        electContext.getLesson))
    }
    result
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.ASSIGNED_LESSON_IDS)) {
      val assignedLessonIds = CollectUtils.newHashSet()
      for (take <- context.getTakes if ElectionMode.ASSIGEND == take.getElectionMode.id) {
        assignedLessonIds.add(take.getLesson.id)
        context.getState.getUnWithdrawableLessonIds.put(take.getLesson.id, ERROR_WITHDRAW_ASSIGNED)
      }
      context.getState.getParams.put("assignedLessonIds", assignedLessonIds)
      context.addPreparedDataName(PreparedDataName.ASSIGNED_LESSON_IDS)
    }
  }
}
