package org.openurp.edu.eams.teach.schedule.util



import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.beangle.commons.collection.Collections



class TableTaskGroup {

  def this(`type`: CourseType) {
    this()
    this.`type` = `type`
    credit = new java.lang.Float(0)
    actualCredit = new java.lang.Float(0)
  }

  var `type`: CourseType = _

  var tasks: collection.mutable.Buffer[Lesson] = Collections.newBuffer[Lesson]

  var credit: java.lang.Float = _

  var actualCredit: java.lang.Float = _

  def addTask(task: Lesson) {
    tasks += task
    actualCredit = if (null == actualCredit) new java.lang.Float(task.course.credits) else new java.lang.Float(actualCredit.floatValue() + task.course.credits)
  }
}
