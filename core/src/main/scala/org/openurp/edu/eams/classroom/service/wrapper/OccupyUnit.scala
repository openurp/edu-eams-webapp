package org.openurp.edu.eams.classroom.service.wrapper

import java.util.Collection
import org.openurp.base.Classroom
import org.openurp.edu.eams.classroom.TimeUnit
import org.openurp.edu.eams.classroom.code.industry.RoomUsage
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class OccupyUnit(@BeanProperty val rooms: Collection[Classroom], 
    @BeanProperty val units: Array[TimeUnit], 
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
