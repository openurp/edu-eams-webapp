package org.openurp.edu.eams.teach.election.web.action.rule

import java.io.IOException
import java.io.Writer
import java.util.Date



import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.beangle.ems.rule.Rule
import org.beangle.ems.rule.RuleParameter
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.web.action.BaseAction



class ElectRuleConfigAction extends BaseAction {

  protected override def getEntityName(): String = classOf[RuleConfig].getName

  protected def indexSetting() {
    put("electRuleTypes", ElectRuleType.valueMap())
    put("ELECTION", ElectRuleType.ELECTION)
  }

  def search(): String = {
    put("electRuleTypes", ElectRuleType.valueMap())
    put("ELECTION", ElectRuleType.ELECTION)
    put(getShortName + "s", search(getQueryBuilder.asInstanceOf[OqlBuilder[_]].where(getShortName + ".rule.business in (:electBiz)", 
      ElectRuleType.strValues())))
    forward()
  }

  protected def editSetting(entity: Entity[_]) {
    put("electRuleTypes", ElectRuleType.valueMap())
    put("ELECTION", ElectRuleType.ELECTION)
    val config = entity.asInstanceOf[RuleConfig]
    val builder = OqlBuilder.from(classOf[Rule], "rule")
    builder.where("rule.enabled = true")
    builder.where("rule.business in (:businesses)", ElectRuleType.strValues())
    val rules = entityDao.search(builder)
    val configParams = config.getParams
    var ruleParameters = CollectUtils.newArrayList()
    val paramBuilder = OqlBuilder.from(classOf[RuleParameter], "ruleParam")
    if (config.isPersisted) {
      paramBuilder.where("ruleParam.rule=:rule", config.getRule)
      if (!configParams.isEmpty) {
        val params = CollectUtils.newArrayList()
        for (configParam <- configParams) {
          params.add(configParam.getParam)
        }
        paramBuilder.where("ruleParam not in(:params)", params)
      }
    } else if (!rules.isEmpty) {
      paramBuilder.where("ruleParam.rule=:rule", rules.get(0))
    } else {
      paramBuilder.where("1=2")
    }
    ruleParameters = entityDao.search(paramBuilder)
    put("rules", rules)
    put("ruleParameters", ruleParameters)
  }

  def ajaxQueryRuleDetail(): String = {
    val ruleId = getInt("ruleId")
    put("electRuleTypes", ElectRuleType.valueMap())
    put("rule", entityDao.get(classOf[Rule], ruleId))
    forward()
  }

  def save(): String = {
    val now = new Date()
    val config = populateEntity().asInstanceOf[RuleConfig]
    config.setUpdatedAt(now)
    if (config.isTransient) {
      config.setCreatedAt(now)
      val rule = populateEntity(classOf[Rule], "ruleConfig.rule")
      config.setRule(rule)
    }
    val oldParams = config.getParams
    var iter = oldParams.iterator()
    while (iter.hasNext) {
      val param = iter.next()
      iter.remove()
      param.setConfig(null)
      entityDao.remove(param)
      iter = oldParams.iterator()
    }
    var i = 0
    while (true) {
      val ruleParamId = getLong("ruleConfigParam" + i + ".param.id")
      if (null == ruleParamId) {
        //break
      }
      val configParam = populate(classOf[RuleConfigParam], "ruleConfigParam" + i)
      configParam.setId(null)
      configParam.setConfig(config)
      config.getParams.add(configParam)
      i += 1
    }
    try {
      entityDao.saveOrUpdate(config)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        return redirect("search", "info.save.failure")
      }
    }
    redirect("search", "info.save.success")
  }

  def validateRuleByName() {
    val code = get("name")
    val id = getLong("id")
    var result = false
    if (Strings.isNotBlank(code)) {
      result = entityDao.duplicate(classOf[Rule], id, "name", code)
    }
    var writer: Writer = null
    try {
      getResponse.setContentType(getRequest.getContentType)
      writer = getResponse.getWriter
      writer.write(result + "")
      writer.flush()
    } catch {
      case e: IOException => 
    } finally {
      if (null != writer) {
        try {
          writer.close()
        } catch {
          case e: IOException => 
        }
      }
    }
  }
}
