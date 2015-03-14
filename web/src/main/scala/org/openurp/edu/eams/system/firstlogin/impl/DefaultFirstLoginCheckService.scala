package org.openurp.edu.eams.system.firstlogin.impl

import java.util.Map
import java.util.Set
import org.beangle.commons.bean.Initializing
import org.beangle.commons.collection.CollectUtils
import org.beangle.security.blueprint.User
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.openurp.edu.eams.system.firstlogin.FirstLoginCheckService
import org.openurp.edu.eams.system.firstlogin.FirstLoginChecker

import scala.collection.JavaConversions._

class DefaultFirstLoginCheckService extends FirstLoginCheckService with ApplicationContextAware with Initializing {

  val checkers = CollectUtils.newHashMap()

  var context: ApplicationContext = null

  def check(user: User): Boolean = {
    checkers.values.find(!_.check(user)).map(_ => false)
      .getOrElse(true)
  }

  def getCheckerNames(): Set[String] = checkers.keySet

  def init() {
    if (null == context) return
    val names = context.getBeanNamesForType(classOf[FirstLoginChecker])
    if (null != names && names.length > 0) {
      for (name <- names) {
        checkers.put(name, context.getBean(name).asInstanceOf[FirstLoginChecker])
      }
    }
  }

  def setApplicationContext(context: ApplicationContext) {
    this.context = context
  }
}
