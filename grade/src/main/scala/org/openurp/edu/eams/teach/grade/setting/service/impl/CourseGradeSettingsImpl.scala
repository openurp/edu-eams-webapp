package org.openurp.edu.eams.teach.grade.setting.service.impl

import java.util.List
import java.util.Map
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.ems.dictionary.service.BaseCodeService
import org.openurp.edu.base.Project
import org.openurp.edu.base.ProjectConfig
import org.openurp.edu.base.ProjectProperty
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting
import org.openurp.edu.eams.teach.grade.service.CourseGradeSettings
import com.google.gson.Gson
import CourseGradeSettingsImpl._

import scala.collection.JavaConversions._

object CourseGradeSettingsImpl {

  private val COURSEGRADESETTING = "course.grade.setting"
}

class CourseGradeSettingsImpl extends BaseServiceImpl with CourseGradeSettings {

  private var cache: Map[Integer, CourseGradeSetting] = CollectUtils.newHashMap()

  private var baseCodeService: BaseCodeService = _

  def getSetting(project: Project): CourseGradeSetting = {
    val config = entityDao.uniqueResult(OqlBuilder.from(classOf[ProjectConfig], "config").where("config.project=:project", 
      project))
    var settingStr: String = null
    if (null != config) {
      val property = config.getProperties.get(COURSEGRADESETTING)
      settingStr = if ((null == property)) null else property.getValue
    }
    var setting: CourseGradeSetting = null
    if (Strings.isNotBlank(settingStr)) {
      val gson = new Gson()
      try {
        setting = gson.fromJson(settingStr, classOf[CourseGradeSetting])
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    if (null == setting) {
      setting = cache.get(project.getId)
      if (setting == null) {
        setting = new CourseGradeSetting(project)
        val allTypes = baseCodeService.getCodes(classOf[GradeType])
        setting.getGaElementTypes.retainAll(allTypes)
        cache.put(project.getId, setting)
      }
    }
    setting
  }

  def setBaseCodeService(baseCodeService: BaseCodeService) {
    this.baseCodeService = baseCodeService
  }
}
