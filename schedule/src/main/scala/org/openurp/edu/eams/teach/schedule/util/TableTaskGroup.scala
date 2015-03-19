package org.openurp.edu.eams.teach.schedule.util



import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson



class TableTaskGroup {

  def this(`type`: CourseType) {
    this()
    this.`type` = `type`
    credit = new java.lang.Float(0)
    actualCredit = new java.lang.Float(0)
  }

  var `type`: CourseType = _

  var tasks: List[Lesson] = new ArrayList[Lesson]()

  var credit: java.lang.Float = _

  var actualCredit: java.lang.Float = _

  def getType(): CourseType = `type`

  def setType(`type`: CourseType) {
    this.`type` = `type`
  }

  def getActualCredit(): java.lang.Float = actualCredit

  def setActualCredit(actualCredit: java.lang.Float) {
    this.actualCredit = actualCredit
  }

  def getCredit(): java.lang.Float = credit

  def setCredit(credit: java.lang.Float) {
    this.credit = credit
  }

  def getTasks(): List[Lesson] = tasks

  def setTasks(tasks: List[Lesson]) {
    this.tasks = tasks
  }

  def addTask(task: Lesson) {
    tasks.add(task)
    actualCredit = if (null == actualCredit) new java.lang.Float(task.getCourse.getCredits) else new java.lang.Float(actualCredit.floatValue() + task.getCourse.getCredits)
  }
}
