package org.openurp.edu.eams.teach.election


import org.beangle.data.model.Entity
import org.beangle.commons.entity.TimeEntity
import org.beangle.ems.rule.model.RuleConfig



trait ElectPlan extends Entity[Long] with TimeEntity {

  def getName(): String

  def setName(name: String): Unit

  def getDescription(): String

  def setDescription(description: String): Unit

  def getRuleConfigs(): Set[RuleConfig]

  def setRuleConfigs(ruleConfigs: Set[RuleConfig]): Unit
}
