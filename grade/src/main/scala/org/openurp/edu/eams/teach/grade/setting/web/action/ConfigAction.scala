package org.openurp.edu.eams.teach.grade.setting.web.action

import java.util.Date
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.ProjectConfig
import org.openurp.edu.base.ProjectProperty
import org.openurp.edu.eams.core.model.ProjectConfigBean
import org.openurp.edu.eams.core.model.ProjectPropertyBean
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.setting.ExamStatusJsonAdapter
import org.openurp.edu.eams.teach.grade.setting.GradeTypeJsonAdapter
import org.openurp.edu.eams.web.action.common.ProjectSupportAction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ConfigAction._



object ConfigAction {

  private val COURSEGRADESETTING = "course.grade.setting"
}

class ConfigAction extends ProjectSupportAction {

  def index(): String = {
    val setting = getSetting
    put("setting", setting)
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType]))
    put("examStatues", baseCodeService.getCodes(classOf[ExamStatus]))
    forward()
  }

  def edit(): String = {
    val setting = getSetting
    put("setting", setting)
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType]))
    put("examStatues", baseCodeService.getCodes(classOf[ExamStatus]))
    forward()
  }

  def save(): String = {
    val setting = getSetting
    var config = entityDao.uniqueResult(OqlBuilder.from(classOf[ProjectConfig], "config").where("config.project=:project", 
      getProject))
    if (null == config) {
      config = new ProjectConfigBean()
      config.setProject(getProject)
      config.setCreatedAt(new Date())
      config.setUpdatedAt(new Date())
      entityDao.saveOrUpdate(config)
    }
    val setting_finalCandinateTypes = Strings.splitToInt(get("setting.finalCandinateTypes"))
    setting.setFinalCandinateTypes(entityDao.get(classOf[GradeType], setting_finalCandinateTypes))
    setting.setGaElementTypes(entityDao.get(classOf[GradeType], Strings.splitToInt(get("setting.gaElementTypes"))))
    setting.setAllowExamStatuses(CollectUtils.newHashSet(entityDao.get(classOf[ExamStatus], Strings.splitToInt(get("setting.allowExamStatuses")))))
    setting.setEmptyScoreStatuses(CollectUtils.newHashSet(entityDao.get(classOf[ExamStatus], Strings.splitToInt(get("setting.emptyScoreStatuses")))))
    setting.setPublishableTypes(entityDao.get(classOf[GradeType], Strings.splitToInt(get("setting.publishableTypes"))))
    setting.setCalcGaExamStatus(getBool("setting.calcGaExamStatus"))
    setting.setSubmitIsPublish(getBool("setting.submitIsPublish"))
    var property = config.getProperties.get(COURSEGRADESETTING).asInstanceOf[ProjectPropertyBean]
    if (null == property) property = new ProjectPropertyBean()
    property.setConfig(config)
    property.setName(COURSEGRADESETTING)
    val gsonBuilder = new GsonBuilder()
    gsonBuilder.registerTypeAdapter(classOf[GradeType], new GradeTypeJsonAdapter())
    gsonBuilder.registerTypeAdapter(classOf[ExamStatus], new ExamStatusJsonAdapter())
    val gson = gsonBuilder.create()
    property.setValue(gson.toJson(setting))
    config.getProperties.put(COURSEGRADESETTING, property)
    entityDao.saveOrUpdate(property)
    redirect("index", "info.save.success")
  }

  private def getSetting(): CourseGradeSetting = {
    var setting = new CourseGradeSetting()
    val config = entityDao.uniqueResult(OqlBuilder.from(classOf[ProjectConfig], "config").where("config.project=:project", 
      getProject))
    if (null != config) {
      val property = config.getProperties.get(COURSEGRADESETTING)
      if (null != property) setting = property.toClass(classOf[CourseGradeSetting])
    }
    setting
  }
}
