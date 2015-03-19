package org.openurp.edu.eams.teach.grade.lesson.web.action

import org.openurp.edu.base.Course




class RetakeCourseStat( val course: Course,  val unpassed: Number)
    {

  
  var freespace: Int = _

  private var newspace: Int = _

  def getNewspace(): Int = {
    if (this.newspace == 0) {
      this.newspace = if (unpassed.intValue() > freespace) unpassed.intValue() - freespace else 0
    }
    newspace
  }

  def setNewspace(newspace: Int) {
    this.newspace = newspace
  }
}
