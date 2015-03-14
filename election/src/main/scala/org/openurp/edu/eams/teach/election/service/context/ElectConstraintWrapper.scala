package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable

import scala.collection.JavaConversions._

trait ElectConstraintWrapper[T] extends Serializable {

  def subElectedItem(item: T): T

  def addElectedItem(item: T): T

  def isOverMax(item: T): Boolean
}
