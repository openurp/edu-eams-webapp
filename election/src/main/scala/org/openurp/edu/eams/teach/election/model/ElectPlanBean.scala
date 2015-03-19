package org.openurp.edu.eams.teach.election.model

import java.util.Date

import javax.persistence.Entity
import javax.persistence.ManyToMany
import javax.validation.constraints.NotNull
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.pojo.NumberIdTimeObject
import org.beangle.ems.rule.model.RuleConfig
import org.openurp.edu.eams.teach.election.ElectPlan
import ElectPlanBean._




object ElectPlanBean {

  def create(name: String, 
      description: String, 
      ruleConfigs: Set[RuleConfig], 
      createdAt: Date): ElectPlan = {
    val plan = new ElectPlanBean(name, description, ruleConfigs)
    plan.setCreatedAt(createdAt)
    plan.setUpdatedAt(createdAt)
    plan
  }
}

@SerialVersionUID(-979938480073949863L)
@Entity(name = "org.openurp.edu.eams.teach.election.ElectPlan")
class ElectPlanBean extends NumberIdTimeObject[Long]() with ElectPlan {

  @NotNull
  
  var name: String = _

  
  var description: String = _

  @ManyToMany
  
  var ruleConfigs: Set[RuleConfig] = CollectUtils.newHashSet()

  private def this(name: String, description: String, ruleConfigs: Set[RuleConfig]) {
    this()
    setName(name)
    setDescription(description)
    setRuleConfigs(ruleConfigs)
  }
}
