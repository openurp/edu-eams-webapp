package org.openurp.edu.eams.teach.election.service.impl

import java.io.Writer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.List
import java.util.Map
import org.apache.commons.io.output.StringBuilderWriter
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.ems.dictionary.service.impl.BaseCodeServiceImpl
import org.beangle.struts2.freemarker.BeangleClassTemplateLoader
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import org.openurp.edu.eams.teach.election.service.RetakeFeeConfigService
import org.openurp.edu.eams.teach.election.service.event.RetakeFeeConfigSaveEvent
import org.openurp.edu.teach.lesson.CourseTake
import freemarker.template.Configuration
import freemarker.template.Template

import scala.collection.JavaConversions._

class RetakeFeeConfigServiceImpl extends BaseCodeServiceImpl with RetakeFeeConfigService {

  private var retakeFeeRuleScript: String = _

  def getFeeRuleScript(): String = {
    if (null != retakeFeeRuleScript) return retakeFeeRuleScript
    val cfg = new Configuration()
    cfg.setLocalizedLookup(false)
    cfg.setNumberFormat("0.##")
    cfg.setDefaultEncoding("UTF-8")
    cfg.setTemplateLoader(new BeangleClassTemplateLoader(null))
    var out: Writer = null
    try {
      val template = cfg.getTemplate("template/feeConfig/feeRuleScripts/retakeFeeRuleScript.ftl")
      out = new StringBuilderWriter()
      template.process(Collections.emptyMap(), out)
      out.flush()
      return out.toString
    } catch {
      case e: Exception => logger.info("info.getRetakeFeeRuleScript.error", e)
    } finally {
      if (null != out) {
        try {
          out.close()
        } catch {
          case e2: Exception => 
        }
      }
    }
    null
  }

  def getCurrOpenConfigs(): List[RetakeFeeConfig] = {
    val date = new Date()
    val builder = OqlBuilder.from(classOf[RetakeFeeConfig], "config")
    builder.where("exists(from " + classOf[Semester].getName + " semester " + 
      "where config.semester=semester " + 
      "and semester.beginOn <= :beginOn and semester.endOn >= :beginOn)", date)
    builder.where("config.opened is true")
    builder.where("config.openAt is null or config.openAt <=:openAt", date)
    builder.where("config.closeAt is null or config.closeAt >= :closeAt", date)
    entityDao.search(builder)
  }

  def getOpenConfigs(semesters: Semester*): List[RetakeFeeConfig] = {
    entityDao.search(getOpenConfigBuilder(null, semesters))
  }

  def getOpenConfigs(project: Project, semesters: Semester*): List[RetakeFeeConfig] = {
    entityDao.search(getOpenConfigBuilder(project, semesters))
  }

  def getOpenConfigBuilder(project: Project, semesters: Semester*): OqlBuilder[RetakeFeeConfig] = {
    val date = new Date()
    val builder = OqlBuilder.from(classOf[RetakeFeeConfig], "config")
    if (null == project) {
      builder.where("config.project = :project", project)
    }
    builder.where("config.semester in (:semesters)", semesters)
    builder.where("config.opened is true")
    builder.where("config.openAt is null or config.openAt <=:date", date)
    builder.where("config.closeAt is null or config.closeAt >=:date", date)
    builder
  }

  def getConfig(config: RetakeFeeConfig): RetakeFeeConfig = {
    val now = new Date()
    var openAt = config.getOpenAt
    val closeAt = config.getCloseAt
    val builder = OqlBuilder.from(classOf[RetakeFeeConfig], "config")
    val params = CollectUtils.newHashMap()
    builder.where("config.project = :project and config.semester = :semester and config.feeType=:feeType")
    if (!(null == openAt && null == closeAt)) {
      if (null == openAt && null != closeAt) {
        if (now.getTime <= closeAt.getTime) {
          openAt = now
        } else {
          try {
            openAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("0000-01-01 00:00:00")
          } catch {
            case e: ParseException => 
          }
        }
      }
      if (null == closeAt && null != openAt) {
        if (now.getTime <= openAt.getTime) {
          openAt = now
        } else {
          try {
            openAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2099-01-01 00:00:00")
          } catch {
            case e: ParseException => 
          }
        }
      }
      params.put("openAt", openAt)
      params.put("closeAt", closeAt)
      builder.where("config.openAt is null or config.openAt <=:openAt")
      builder.where("config.closeAt is null or config.closeAt >=:openAt")
      builder.where("config.openAt is null or config.openAt <=:closeAt")
      builder.where("config.closeAt is null or config.closeAt >=:closeAt")
    }
    params.put("project", config.getProject)
    params.put("semester", config.getSemester)
    params.put("feeType", config.getFeeType)
    entityDao.uniqueResult(builder.params(params))
  }

  def getConfigs(project: Project, semesters: Semester*): List[RetakeFeeConfig] = {
    val builder = OqlBuilder.from(classOf[RetakeFeeConfig], "config")
    builder.where("config.project = :project", project)
    builder.where("config.semester in (:semesters)", semesters)
    entityDao.search(builder)
  }

  def doCheck(project: Project, semesters: Semester*): Boolean = {
    val date = new Date()
    val builder = OqlBuilder.from(classOf[RetakeFeeConfig], "config")
    builder.where("config.project = :project", project)
    builder.where("config.semester in (:semesters)", semesters)
    builder.where("config.opened is true")
    builder.where("config.openAt is null or config.openAt <=:date", date)
    builder.where("config.closeAt is null or config.closeAt >=:date", date)
    !entityDao.search(builder).isEmpty
  }

  def doCheck(config: RetakeFeeConfig): Boolean = {
    val date = new Date()
    if (config.isOpened && config.getOpenAt == null) true else if (config.getOpenAt.before(date) && config.getCloseAt == null) true else config.getCloseAt.after(date)
  }

  def getRetakeCourseTakes(student: Student, semesters: Semester*): List[CourseTake] = {
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    builder.where("courseTake.lesson.semester in(:semesters)", semesters)
    builder.where("courseTake.std=:std", student)
    builder.where("courseTake.courseTakeType.id = :courseTakeTypeId", CourseTakeType.RESTUDY)
    val orderBy = Params.get(Order.ORDER_STR)
    builder.orderBy(if (Strings.isEmpty(orderBy)) "courseTake.lesson.no" else orderBy)
    entityDao.search(builder)
  }

  def saveOrUpdate(config: RetakeFeeConfig) {
    entityDao.saveOrUpdate(config)
    publish(new RetakeFeeConfigSaveEvent(config))
  }
}
