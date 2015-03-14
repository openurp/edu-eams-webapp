package org.openurp.edu.eams.teach.election.web.action.rule

import java.util.Collections
import java.util.Date
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.ems.rule.RuleParameter
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.edu.eams.teach.election.ElectPlan
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

class ElectPlanAction extends BaseAction {

  protected override def getEntityName(): String = classOf[ElectPlan].getName

  override def edit(): String = {
    val entityId = getLongId(getShortName)
    var entity: Entity[_] = null
    entity = if (null == entityId) populateEntity() else getModel(getEntityName, entityId)
    if (null == entity) {
      return redirect("search")
    }
    put(getShortName, entity)
    val builder = OqlBuilder.from(classOf[RuleConfig], "config")
    builder.where("config.enabled = true")
    builder.where("config.rule.business in (:businesses)", ElectRuleType.strValues())
    val configs = entityDao.search(builder)
    val paramsMap = CollectUtils.newHashMap()
    for (ruleConfig <- configs; configParam <- ruleConfig.getParams) {
      paramsMap.put(configParam.getParam, configParam)
    }
    put("paramsMap", paramsMap)
    put("configs", configs)
    put("ELECTION", ElectRuleType.ELECTION)
    put("electRuleTypes", ElectRuleType.valueMap())
    forward()
  }

  protected override def saveAndForward(entity: Entity[_]): String = {
    val plan = entity.asInstanceOf[ElectPlan]
    try {
      plan.setUpdatedAt(new Date())
      if (!plan.isPersisted) {
        plan.setCreatedAt(plan.getUpdatedAt)
      }
      val configIds = getIntIds("config")
      plan.getRuleConfigs.clear()
      for (configId <- configIds) {
        plan.getRuleConfigs.add(Model.newInstance(classOf[RuleConfig], configId))
      }
      saveOrUpdate(Collections.singletonList(entity))
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
      }
    }
  }
}
