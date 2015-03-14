package org.openurp.edu.eams.teach.lesson.task.dao.hibernate

import org.beangle.orm.hibernate.HibernateEntityDao
import org.openurp.edu.eams.teach.lesson.task.dao.RequirePreferDao
import org.openurp.edu.eams.teach.lesson.task.model.RequirePrefer

import scala.collection.JavaConversions._

class RequirePreferDaoHibernate extends HibernateEntityDao with RequirePreferDao {

  def getPreference(preferenceId: java.lang.Long): RequirePrefer = {
    get(classOf[RequirePrefer], preferenceId).asInstanceOf[RequirePrefer]
  }

  def savePreference(preference: RequirePrefer) {
    saveOrUpdate(preference)
  }

  def updatePreference(preference: RequirePrefer) {
    saveOrUpdate(preference)
  }
}
