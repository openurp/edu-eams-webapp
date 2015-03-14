package org.openurp.edu.eams.teach.election

import java.util.Set
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.TimeEntity
import org.beangle.ems.rule.model.RuleConfig

import scala.collection.JavaConversions._

trait ElectPlan extends Entity[Long] with TimeEntity {

  def getName(): String

  def setName(name: String): Unit

  def getDescription(): String

  def setDescription(description: String): Unit

  def getRuleConfigs(): Set[RuleConfig]

  def setRuleConfigs(ruleConfigs: Set[RuleConfig]): Unit
}
