package org.openurp.eams.grade.setting.web.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.eams.grade.model.GradeRateConfig
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.core.Project
import org.openurp.teach.core.model.ProjectBean
import org.beangle.commons.collection.Order
import org.beangle.webmvc.api.view.View
import org.beangle.commons.lang.Strings
import org.openurp.eams.grade.model.GradeRateItem
import java.util.HashMap
import org.openurp.eams.grade.model.GradeRateItem
import java.util.ArrayList
import scala.collection.mutable.ListBuffer
import scala.collection.Seq
import scala.collection.immutable.HashMap

class RateAction extends RestfulAction[GradeRateConfig] {

  override def search(): String = {
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "grc")
    val project = entityDao.get(classOf[Project], new Integer(1))
    builder.where("grc.project=:project", project)
    populateConditions(builder)
    builder.limit(getPageLimit())
    //builder.orderBy(Order.parse("orderBy"))
    val gradeRateConfigs = entityDao.search(builder)
    put("gradeRateConfigs", gradeRateConfigs)
    forward()
  }

  def addConfig(): String = {

    val builder = OqlBuilder.from(classOf[ScoreMarkStyle], "markStyle")
    populateConditions(builder)
    val project = entityDao.get(classOf[Project], new Integer(1))
    //	  val project =getProject()
    builder.where("not exists(from " + classOf[GradeRateConfig].getName()
      + " cfg where cfg.scoreMarkStyle=markStyle and cfg.project=:project)", project)
    builder.orderBy("markStyle.code")
    val markStyles = entityDao.search(builder)
    put("markStyles", markStyles)
    put("project", project)

    forward()
  }
  //	protected def getProject():Project ={
  //	  val project = new ProjectBean()
  //	  project.id=1
  //	  project
  //	}

  override def save(): View = {
    var gradeRateConfig = populateEntity(classOf[GradeRateConfig], "gradeRateConfig")
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "gradeRate")
    val project = entityDao.get(classOf[Project], new Integer(1))
    builder.where("gradeRate.project =:project", project)
    builder.where("gradeRate.scoreMarkStyle =:scoreMarkStyle", gradeRateConfig.scoreMarkStyle)
    val gradeRateConfigs = entityDao.search(builder)
    if (!gradeRateConfigs.isEmpty) {
      gradeRateConfig = gradeRateConfigs(0)
      //gradeRateConfig
    }
    entityDao.saveOrUpdate(gradeRateConfig)
    redirect("search", "info.save.success")
  }

  def remove(): View = {
    val configs = entityDao.find(classOf[GradeRateConfig], getIntIds("gradeRateConfig"))
    entityDao.remove(configs)
    redirect("search", "info.action.success")
  }

  /**
   * 对某项成绩记录方式其分数显示详细设置
   */

  def setting(): String = {
    put("gradeRateConfig", entityDao.get(classOf[GradeRateConfig], getIntId("gradeRateConfig.id")))
    forward()
  }

  /**
   * 保存详细配置
   */

//  def saveConfigSetting(): View = {
//    val gradeRateConfig = entityDao.get(classOf[GradeRateConfig], getIntId("gradeRateConfig.id"))
//    val configItemIds = Strings.splitToInt(get("configItemIds", ""))
//    // 添加配置项
//    if (null == configItemIds || configItemIds.length == 0) {
//      var converters = gradeRateConfig.items
//      if (converters.isEmpty) {
//        converters = new ListBuffer()
//      }
//      // 此类中没有id
//      val configItem: GradeRateItem = populateEntity(classOf[GradeRateItem], "configItem")
//      gradeRateConfig.items = configItem
//      configItem.config = gradeRateConfig
//      try {
//        entityDao.saveOrUpdate(gradeRateConfig)
//      } catch (Exception e) {
//        redirect("setting", "info.action.failure", "&gradeRateConfig.id=" + gradeRateConfig.id)
//      }
//      redirect("setting", "info.action.success", "&gradeRateConfig.id=" + gradeRateConfig.id)
//    } // 修改配置项
//    else {
//      val itemMap: Map[Int, GradeRateItem] = new HashMap()
//      for (configItem <- gradeRateConfig.items) {
//        itemMap.put(configItem.id, configItem)
//      }
//      for (i <- 0 to configItemIds.length) {
//        var configItem: GradeRateItem = itemMap.get(configItemIds[i])
//        configItem.grade = get("scoreName" + configItemIds[i])
//        configItem.maxScore = getFloat("maxScore" + configItemIds[i])
//        configItem.minScore = getFloat("minScore" + configItemIds[i])
//        configItem.defaultScore = getFloat("defaultScore" + configItemIds[i])
//        configItem.gpExp = get("gpExp" + configItemIds[i])
//      }
//      try {
//        entityDao.saveOrUpdate(gradeRateConfig);
//      } catch (Exception e) {
//        redirect("search", "info.action.failure");
//      }
//      redirect("search", "info.action.success");
//    }
//  }

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
//	/**
//	 * 保存详细配置
//	 */
//	public String saveConfigSettng() {
//		GradeRateConfig gradeRateConfig = (GradeRateConfig) entityDao.get(GradeRateConfig.class,
//		        getLong("gradeRateConfig.id"));
//		Long[] configItemIds = Strings.splitToLong(get("configItemIds"));
//
//		// 添加配置项
//		if (null == configItemIds || configItemIds.length == 0) {
//			List<GradeRateItem> converters = gradeRateConfig.getItems();
//			if (CollectUtils.isEmpty(converters)) {
//				converters = CollectUtils.newArrayList();
//			}
//			// 此类中没有id
//			GradeRateItem configItem = populateEntity(GradeRateItem.class, "configItem");
//			gradeRateConfig.getItems().add(configItem);
//			configItem.setConfig(gradeRateConfig);
//			try {
//				entityDao.saveOrUpdate(gradeRateConfig);
//			} catch (Exception e) {
//				return redirect("setting", "info.action.failure",
//				        "&gradeRateConfig.id=" + gradeRateConfig.getId());
//			}
//			return redirect("setting", "info.action.success",
//			        "&gradeRateConfig.id=" + gradeRateConfig.getId());
//		}
//		// 修改配置项
//		else {
//			Map<Long, GradeRateItem> itemMap = CollectUtils.newHashMap();
//			for (GradeRateItem configItem : gradeRateConfig.getItems()) {
//				itemMap.put(configItem.getId(), configItem);
//			}
//			for (int i = 0; i < configItemIds.length; i++) {
//				GradeRateItem configItem = itemMap.get(configItemIds[i]);
//				configItem.setGrade(get("scoreName" + configItemIds[i]));
//				configItem.setMaxScore(getFloat("maxScore" + configItemIds[i]));
//				configItem.setMinScore(getFloat("minScore" + configItemIds[i]));
//				configItem.setDefaultScore(getFloat("defaultScore" + configItemIds[i]));
//				configItem.setGpExp(get("gpExp" + configItemIds[i]));
//			}
//			try {
//				entityDao.saveOrUpdate(gradeRateConfig);
//			} catch (Exception e) {
//				return redirect("search", "info.action.failure");
//			}
//			return redirect("search", "info.action.success");
//		}
//	}
//
//}