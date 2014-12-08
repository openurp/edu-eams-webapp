package org.openurp.eams.grade.setting.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.teach.grade.model.GradeRateConfig
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.core.Project
import org.openurp.teach.core.model.ProjectBean
import org.beangle.commons.collection.Order
import org.beangle.webmvc.api.view.View
import org.beangle.commons.lang.Strings
import org.openurp.teach.grade.model.GradeRateItem
import java.util.HashMap
import org.openurp.teach.grade.model.GradeRateItem
import java.util.ArrayList
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import scala.collection.immutable.HashMap
/**
 * 成绩记录方式设置
 */
class RateAction extends RestfulAction[GradeRateConfig] {

  override def search(): String = {
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "grc")
    val project = entityDao.get(classOf[Project], new Integer(1))
    builder.where("grc.project=:project", project)
    populateConditions(builder)
    builder.limit(getPageLimit())
    get("orderBy").map(orderBy => {
      builder.orderBy(Order.parse(orderBy))
    })
    val gradeRateConfigs = entityDao.search(builder)
    put("gradeRateConfigs", gradeRateConfigs)
    forward()
  }

  def addConfig(): String = {

    val builder = OqlBuilder.from(classOf[ScoreMarkStyle], "markStyle")
    populateConditions(builder)
    val project = entityDao.get(classOf[Project], new Integer(1))
    builder.where("not exists(from " + classOf[GradeRateConfig].getName()
      + " cfg where cfg.scoreMarkStyle=markStyle and cfg.project=:project)", project)
    builder.orderBy("markStyle.code")
    val markStyles = entityDao.search(builder)
    put("markStyles", markStyles)
    put("project", project)

    forward()
  }

  override def save(): View = {
    var gradeRateConfig = populateEntity(classOf[GradeRateConfig], "gradeRateConfig")
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "gradeRate")
    val project = entityDao.get(classOf[Project], new Integer(1))
    builder.where("gradeRate.project =:project", project)
    builder.where("gradeRate.scoreMarkStyle =:scoreMarkStyle", gradeRateConfig.scoreMarkStyle)
    val gradeRateConfigs = entityDao.search(builder)
    if (!gradeRateConfigs.isEmpty) {
      gradeRateConfig = gradeRateConfigs(0)
    }
    entityDao.saveOrUpdate(gradeRateConfig)
    redirect("search", "info.save.success")
  }

  override def remove(): View = {
    val configs = entityDao.find(classOf[GradeRateConfig], getIntIds("gradeRateConfig"))
    entityDao.remove(configs)
    redirect("search", "info.action.success")
  }

  /**
   * 对某项成绩记录方式其分数显示详细设置
   */

  def setting(): String = {
    val gradeRateConfig = entityDao.get(classOf[GradeRateConfig], getIntId("gradeRateConfig"))
    put("gradeRateConfig", gradeRateConfig)
    forward()
  }

  /**
   * 保存详细配置
   */

  def saveConfigSetting(): View = {
    val gradeRateConfig = entityDao.get(classOf[GradeRateConfig], getIntId("gradeRateConfig"))
    val configItemIds = Strings.splitToInt(get("configItemIds", ""))
    // 添加配置项
    if (null == configItemIds || configItemIds.length == 0) {
      val converters = gradeRateConfig.items
      // 此类中没有id
      val configItem: GradeRateItem = populateEntity(classOf[GradeRateItem], "configItem")
      gradeRateConfig.items.append(configItem)
      configItem.config = gradeRateConfig
      try {
        entityDao.saveOrUpdate(gradeRateConfig)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          redirect("setting", "gradeRateConfig.id=" + gradeRateConfig.id, "info.action.failure")
        }
      }
      redirect("setting", "gradeRateConfig.id=" + gradeRateConfig.id, "info.action.success")
    } // 修改配置项
    else {
      val itemMap = new scala.collection.mutable.HashMap[Int, GradeRateItem]()
      for (configItem <- gradeRateConfig.items) {
        itemMap.put(configItem.id, configItem)
      }
      for (i <- 0 until configItemIds.length) {
        val configItem: GradeRateItem = itemMap.get(configItemIds(i)).get
        configItem.grade = get("scoreName" + configItemIds(i)).get
        configItem.maxScore = getFloat("maxScore" + configItemIds(i)).get
        configItem.minScore = getFloat("minScore" + configItemIds(i)).get
        configItem.defaultScore = getFloat("defaultScore" + configItemIds(i)).get
        configItem.gpExp = get("gpExp" + configItemIds(i)).get
      }
      try {
        entityDao.saveOrUpdate(gradeRateConfig);
      } catch {
        case e: Exception =>
          redirect("search", "info.action.failure");
      }
      redirect("search", "info.action.success");
    }
  }

  /**
   * 删除配置项
   */
  def removeConfigSettng(): View = {
    entityDao.remove(entityDao.find(classOf[GradeRateItem], getIntIds("configItemIds")))
    redirect("setting", "info.action.success")
  }

  /**
   * 查看详细配置
   */
  def info(): String = {
    put("gradeRateConfig", entityDao.get(classOf[GradeRateConfig], getIntId("gradeRateConfig")))
    forward()
  }
}