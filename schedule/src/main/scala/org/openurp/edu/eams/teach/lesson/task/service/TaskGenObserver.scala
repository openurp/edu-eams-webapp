package org.openurp.edu.eams.teach.lesson.task.service

import java.text.MessageFormat
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.web.util.OutputMessage
import org.openurp.edu.eams.web.util.OutputWebObserver

import scala.collection.JavaConversions._

class TaskGenObserver extends OutputWebObserver() {

  private var totalLessons: Int = _

  def notifyFinish() {
    outputNotify(OutputWebObserver.good, new OutputMessage("任务生成工作结束", ""), false)
  }

  def notifyGenResult(planCount: Int) {
    val message = new OutputMessage("", MessageFormat.format("{0}个计划 生成{1}个教学任务", planCount, totalLessons))
    outputNotify(OutputWebObserver.good, message, false)
  }

  def message(msgObj: AnyRef): String = {
    val message = msgObj.asInstanceOf[OutputMessage]
    messageOf(message.getKey) + message.getMessage
  }

  def outputNotify(term: Int, lessonCount: Int, plan: MajorPlan) {
    val message = new OutputMessage("", MessageFormat.format("生成教学任务 {0} 第{1}学期 {2}条", planString(plan), 
      term, lessonCount))
    totalLessons += lessonCount
    outputNotify(OutputWebObserver.good, message, true)
  }

  def outputNotifyRemove(term: Int, 
      plan: MajorPlan, 
      messageKey: String, 
      increaceProcess: Boolean) {
    val message = new OutputMessage("", messageOf(messageKey) + " " + planString(plan))
    outputNotify(OutputWebObserver.good, message, increaceProcess)
  }

  private def planString(plan: MajorPlan): String = {
    MessageFormat.format("{0}年级 {1} {2}", plan.getProgram.grade, plan.getProgram.major.getName + "专业", 
      if (plan.getProgram.direction == null) "" else plan.getProgram.direction.getName + "方向")
  }
}
