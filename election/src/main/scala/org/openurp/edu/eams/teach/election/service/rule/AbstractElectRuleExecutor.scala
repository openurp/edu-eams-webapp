package org.openurp.edu.eams.teach.election.service.rule

import java.util.Collection
import java.util.Collections
import java.util.Iterator
import java.util.Set
import org.beangle.commons.dao.EntityDao
import org.beangle.ems.rule.engine.RuleExecutor
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.edu.eams.teach.election.dao.ElectionDao
import AbstractElectRuleExecutor._

import scala.collection.JavaConversions._

object AbstractElectRuleExecutor {

  object Priority extends Enumeration {

    val FIRST = new Priority()

    val SECOND = new Priority()

    val THIRD = new Priority()

    val FOURTH = new Priority()

    val FIFTH = new Priority()

    class Priority extends Val

    implicit def convertValue(v: Value): Priority = v.asInstanceOf[Priority]
  }
}

abstract class AbstractElectRuleExecutor extends RuleExecutor with Comparable[AbstractElectRuleExecutor] {

  protected var order: Int = Priority.THIRD.ordinal()

  protected var entityDao: EntityDao = _

  protected var electionDao: ElectionDao = _

  protected var retakeService: RetakeServiceImpl = _

  def setElectionDao(electionDao: ElectionDao) {
    this.electionDao = electionDao
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def getOrder(): Int = order

  def compareTo(abstractElectRuleExecutor: AbstractElectRuleExecutor): Int = {
    this.order - abstractElectRuleExecutor.getOrder
  }

  def setRetakeServiceImpl(retakeService: RetakeServiceImpl) {
    this.retakeService = retakeService
  }

  protected def getParams(configs: Collection[_ <: RuleConfig]): Set[RuleConfigParam] = {
    val serviceName = this.getClass.getSimpleName.toLowerCase()
    for (config <- configs if config.getRule.getServiceName.toLowerCase() == serviceName) {
      return config.getParams
    }
    Collections.emptySet()
  }

  protected def iteratorParams(configs: Collection[_ <: RuleConfig]): Iterator[RuleConfigParam] = getParams(configs).iterator()

  protected def uniqueParam(configs: Collection[_ <: RuleConfig]): RuleConfigParam = {
    val it = iteratorParams(configs)
    if (it.hasNext) it.next() else null
  }
}
