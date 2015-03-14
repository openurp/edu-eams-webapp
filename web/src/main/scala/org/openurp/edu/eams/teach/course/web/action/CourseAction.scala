package org.openurp.edu.teach.Course.web.action

import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.metadata.EntityType
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.entity.metadata.ObjectAndType
import org.beangle.commons.entity.metadata.Populator
import org.beangle.commons.entity.metadata.Type
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.importer.EntityImporter
import org.beangle.commons.transfer.importer.listener.ItemImporterListener
import org.beangle.commons.web.util.RequestUtils
import org.beangle.struts2.convention.route.Action
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.CourseCodeStandard
import org.openurp.edu.teach.CourseExtInfo
import org.openurp.edu.teach.CourseHour
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.eams.teach.code.school.CourseAbilityRate
import org.openurp.edu.eams.teach.code.school.CourseCategory
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.model.CourseHourBean
import org.openurp.edu.eams.teach.service.impl.CourseImportListener
import org.openurp.edu.eams.web.util.DownloadHelper
import com.opensymphony.xwork2.util.ClassLoaderUtil

import scala.collection.JavaConversions._

class CourseAction extends CourseSearchAction {

  def edit(): String = {
    val course = getEntity(classOf[Course], "course")
    if (null == course.getProject) {
      course.setProject(getProject)
    }
    put("extInfo", if (course.getId == null) null else courseService.getCourseExtInfo(course.getId))
    put("departments", getDeparts)
    put("educations", getEducations)
    put("courseCategories", baseCodeService.getCodes(classOf[CourseCategory]))
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    put("examModes", baseCodeService.getCodes(classOf[ExamMode]))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("abilityRates", baseCodeService.getCodes(classOf[CourseAbilityRate]))
    put("majors", baseInfoService.getBaseInfos(classOf[Major]))
    put("course", course)
    put("currentDate", new Date())
    put("codeStandards", entityDao.get(classOf[CourseCodeStandard], "project", getProject))
    forward()
  }

  def save(): String = {
    val course = populateEntity(classOf[Course], "course").asInstanceOf[Course]
    if (entityDao.duplicate(classOf[Course], course.getId, "code", course.getCode)) {
      return forward(new Action("", "edit"), "error.code.existed")
    }
    course.setUpdatedAt(new Date())
    if (course.isTransient) {
      course.setCreatedAt(course.getUpdatedAt)
    }
    try {
      populatePeriod(course)
      course.getXmajors.clear()
      course.getXmajors.addAll(entityDao.get(classOf[Major], getAll("selectMajor.id", classOf[Integer])))
      course.getAbilityRates.clear()
      course.getAbilityRates.addAll(entityDao.get(classOf[CourseAbilityRate], getAll("selectAbilityRate.id", 
        classOf[Integer])))
      courseService.saveOrUpdate(course, populateEntity(classOf[CourseExtInfo], "extInfo").asInstanceOf[CourseExtInfo])
      if (null != get("addAnother")) {
        redirect("edit", "info.save.success")
      } else {
        redirect("search", "info.save.success")
      }
    } catch {
      case e: Exception => redirect("search", "info.save.failure")
    }
  }

  protected def populatePeriod(course: Course): Course = {
    course.getHours.clear()
    val courseHourTypes = baseCodeService.getCodes(classOf[CourseHourType])
    for (i <- 0 until courseHourTypes.size) {
      val courseHourType = courseHourTypes.get(i).asInstanceOf[CourseHourType]
      val hours = getInt("hours_" + courseHourType.getCode)
      if (null != hours && hours.longValue() > 0) {
        var courseHour: CourseHour = null
        try {
          courseHour = classOf[CourseHourBean].newInstance()
          courseHour.setType(courseHourType)
          courseHour.setPeriod(hours)
          courseHour.setCourse(course)
          course.getHours.add(courseHour)
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
    course
  }

  def getEntityName(): String = classOf[Course].getName

  protected def buildEntityImporter(): EntityImporter = {
    val importer = super.buildEntityImporter()
    val defaultPopulator = Model.getPopulator
    importer.setPopulator(new Populator() {

      private var populator: Populator = defaultPopulator

      def populateValue(target: AnyRef, 
          `type`: EntityType, 
          attr: String, 
          value: AnyRef): Boolean = {
        return attr.startsWith("period_hours_") || 
          populator.populateValue(target, `type`, attr, value)
      }

      def populate(target: AnyRef, `type`: EntityType, params: Map[String, Any]): AnyRef = {
        return populator.populate(target, `type`, params)
      }

      def initProperty(target: AnyRef, `type`: Type, attr: String): ObjectAndType = {
        return populator.initProperty(target, `type`, attr)
      }
    })
    importer
  }

  protected def getImporterListeners(): List[ItemImporterListener] = {
    val listeners = CollectUtils.newArrayList()
    listeners.add(new CourseImportListener(courseService, "code", baseCodeService.getCodes(classOf[CourseHourType]), 
      entityDao, entityDao.get(classOf[Project], getSession.get("projectId").asInstanceOf[java.lang.Integer])))
    listeners
  }

  def downloadTemplate(): String = {
    val template = get("template")
    val url = ClassLoaderUtil.getResource(template, this.getClass)
    val fileName = url.getFile
    val workbook = new HSSFWorkbook(url.openStream())
    val sheet = workbook.getSheetAt(0)
    val titles = sheet.getRow(0)
    val keys = sheet.getRow(1)
    val courseHourTypes = baseCodeService.getCodes(classOf[CourseHourType])
    for (courseHourType <- courseHourTypes) {
      val hourTitleCell = titles.createCell(titles.getLastCellNum, Cell.CELL_TYPE_STRING)
      hourTitleCell.setCellValue(courseHourType.getName)
      val hourKeyCell = keys.createCell(keys.getLastCellNum, Cell.CELL_TYPE_STRING)
      hourKeyCell.setCellValue("period_hours_" + courseHourType.getCode)
    }
    val response = getResponse
    val request = getRequest
    response.reset()
    var contentType = response.getContentType
    val attch_name = DownloadHelper.getAttachName(fileName)
    if (null == contentType) {
      contentType = "application/x-msdownload"
      response.setContentType(contentType)
      logger.debug("set content type {} for {}", contentType, attch_name)
    }
    response.addHeader("Content-Disposition", "attachment; filename=\"" + RequestUtils.encodeAttachName(request, 
      attch_name) + 
      "\"")
    workbook.write(response.getOutputStream)
    null
  }

  override def remove(): String = {
    try {
      val entityIds = getLongIds("course")
      val extInfos = entityDao.get(classOf[CourseExtInfo], "course.id", entityIds)
      entityDao.remove(extInfos)
      entityDao.remove(entityDao.get(classOf[Course], entityIds))
    } catch {
      case e: DataIntegrityViolationException => return redirect("search", "删除失败！课程信息与其他信息关联")
      case e: Exception => return redirect("search", "info.delete.failure")
    }
    redirect("search", "info.action.success")
  }

  def activate(): String = {
    val courses = getModels(classOf[Course], getLongIds("course"))
    val enabled = getBool("enabled")
    for (course <- courses) {
      course.setEnabled(enabled)
    }
    try {
      entityDao.saveOrUpdate(courses)
    } catch {
      case e: Exception => return redirect("search", "info.save.failure")
    }
    redirect("search", "info.save.success")
  }

  def listCodeStandard(): String = {
    put("codeStandards", entityDao.get(classOf[CourseCodeStandard], "project", getProject))
    forward()
  }

  def removeCodeStandard(): String = {
    entityDao.remove(entityDao.get(classOf[CourseCodeStandard], getLong("standard.id")))
    redirect("listCodeStandard", "info.action.success")
  }

  def saveCodeStandard(): String = {
    val standard = populateEntity(classOf[CourseCodeStandard], "standard")
    standard.setProject(getProject)
    entityDao.save(standard)
    redirect("listCodeStandard", "info.action.success")
  }

  def codeMe(): String = {
    val standard = entityDao.get(classOf[CourseCodeStandard], getLong("standard.id"))
    val prefix = standard.getPrefix
    val query = OqlBuilder.from(classOf[Course], "course")
    query.select("course.code").where("locate(:prefix, course.code) = 1", prefix)
      .where("length(course.code) = :leng", prefix.length + standard.getSeqLength)
      .orderBy("course.code")
    val codes = entityDao.search(query)
    var newCode = prefix + Strings.leftPad("1", standard.getSeqLength, '0')
    if (CollectUtils.isNotEmpty(codes)) {
      var newNo = 0
      for (oldCode <- codes) {
        val oldNo = oldCode.substring(prefix.length)
        if (oldNo.matches(".*[^\\d]+.*")) {
          //continue
        }
        if (Numbers.toInt(oldNo) - newNo >= 2) {
          //break
        } else {
          newNo = Numbers.toInt(oldNo)
        }
      }
      newNo += 1
      newCode = prefix + 
        (Strings.repeat("0", standard.getSeqLength - String.valueOf(newNo).length) + 
        newNo)
    }
    put("code", newCode)
    forward()
  }

  override def importForm(): String = forward()

  protected def onSave(course: Course) {
    courseService.saveOrUpdate(course)
  }

  protected override def getExportDatas(): Collection[Course] = {
    val courseIds = Strings.transformToLong(Strings.split(get("courseIds")))
    if (courseIds.length > 0) {
      val builder = OqlBuilder.from(classOf[Course], "course")
      builder.where("course.id in (:courseIds)", courseIds)
      entityDao.search(builder)
    } else {
      entityDao.search(buildCourseQuery().limit(null))
    }
  }
}
