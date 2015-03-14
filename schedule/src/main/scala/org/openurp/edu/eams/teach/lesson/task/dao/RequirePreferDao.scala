package org.openurp.edu.eams.teach.lesson.task.dao

import org.openurp.edu.eams.teach.lesson.task.model.RequirePrefer

import scala.collection.JavaConversions._

trait RequirePreferDao {

  def getPreference(preferenceId: java.lang.Long): RequirePrefer

  def savePreference(preference: RequirePrefer): Unit

  def updatePreference(preference: RequirePrefer): Unit
}
