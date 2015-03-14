package org.openurp.edu.eams.teach.election.web.action.rule

import java.io.IOException
import java.util.Date
import java.util.Iterator
import java.util.Set
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.Strings
import org.beangle.ems.rule.Rule
import org.beangle.ems.rule.RuleParameter
import org.openurp.edu.eams.rule.RuleParameterPopulator
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class ElectRuleAction extends BaseAction {

  protected override def getEntityName(): String = classOf[Rule].getName

  protected override def indexSetting() {
    put("electRuleTypes", ElectRuleType.valueMap())
    put("ELECTION", ElectRuleType.ELECTION)
  }

  override def search(): String = {
    put("electRuleTypes", ElectRuleType.valueMap())
    put("ELECTION", ElectRuleType.ELECTION)
    put(getShortName + "s", search(getQueryBuilder.asInstanceOf[OqlBuilder[_]].where(getShortName + ".business in (:electBiz)", 
      ElectRuleType.strValues())))
    forward()
  }

  protected override def editSetting(entity: Entity[_]) {
    put("electRuleTypes", ElectRuleType.valueMap())
    put("ELECTION", ElectRuleType.ELECTION)
  }

  override def save(): String = {
    val now = new Date()
    val ruleId = getIntId("rule")
    var rule: Rule = null
    if (ruleId == null) {
      rule = populateEntity(classOf[Rule], "rule")
      rule.setCreatedAt(now)
    } else {
      rule = entityDao.get(classOf[Rule], ruleId)
      populate(rule, "rule")
    }
    rule.setUpdatedAt(now)
    val oldParams = rule.getParams
    var iter = oldParams.iterator()
    while (iter.hasNext) {
      val param = iter.next()
      iter.remove()
      param.setRule(null)
      entityDao.remove(param)
      iter = oldParams.iterator()
    }
    val newParams = RuleParameterPopulator.populateParams(rule, "ruleRoot")
    rule.getParams.addAll(newParams)
    entityDao.saveOrUpdate(rule)
    redirect("search", "info.save.success")
  }

  def validateRuleByName(): String = {
    val ruleId = getInt("id")
    val name = get("name")
    var isUniqueName = false
    if (Strings.isNotBlank(name)) {
      val builder = OqlBuilder.from(classOf[Rule], "rule")
      if (null != ruleId) {
        builder.where("rule.id<>:ruleId", ruleId)
      }
      builder.where("rule.name=:name", name)
      isUniqueName = entityDao.search(builder).size == 0
    }
    getResponse.getWriter.print(isUniqueName)
    null
  }
}
