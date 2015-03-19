package org.openurp.edu.eams.teach.election.service.context

import org.beangle.commons.lang.Assert
import org.openurp.edu.teach.lesson.Lesson




class CourseTakeStat[T] {

  
  var statBy: T = _

  
  var id: Long = _

  
  var count: Long = _

  
  var lesson: Lesson = _

  def this(id: Long, count: Long, statBy: T) {
    this()
    this.id = id
    this.count = count
    this.statBy = statBy
  }

  def setLesson(lesson: Lesson) {
    lesson.id == this.id
    this.lesson = lesson
  }
}
