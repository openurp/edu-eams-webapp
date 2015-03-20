package org.openurp.edu.eams.classroom.service.wrapper


import org.openurp.base.Room
import org.openurp.base.code.RoomUsage
import org.beangle.commons.lang.time.YearWeekTime

class OccupyUnit( val rooms: Iterable[Room], 
     val units: Array[YearWeekTime], 
    protected val usage: RoomUsage, 
    protected val userid: java.lang.Long) {

  protected var comment: String = _

  def getComment(): String = comment

  def setComment(comment: String) {
    this.comment = comment
  }

  def getUsage(): RoomUsage = usage

  def getUserid(): java.lang.Long = userid
}
