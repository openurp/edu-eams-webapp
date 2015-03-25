package org.openurp.edu.base.Course.web.action

import java.io.IOException
import java.io.Writer
import java.sql.Date

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.eams.teach.code.school.CourseCategory
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.service.CourseService
import org.openurp.edu.eams.teach.service.impl.CoursePropertyExtractor
import org.openurp.edu.eams.web.action.common.ProjectSupportAction
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper



class CourseSearchAction extends ProjectSupportAction {

  var baseInfoSearchHelper: BaseInfoSearchHelper = _

  var courseService: CourseService = _

  def index(): String = {
    put("departments", getDeparts)
    put("educations", getEducations)
    put("courseTypes", entityDao.getAll(classOf[CourseType]))
    put("examModes", baseCodeService.getCodes(classOf[ExamMode]))
    put("categorys", baseCodeService.getCodes(classOf[CourseCategory]))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def search(): String = {
    put("courses", entityDao.search(buildCourseQuery()))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def buildCourseQuery(): OqlBuilder[Course] = {
    val builder = OqlBuilder.from(classOf[Course], "course")
    QueryHelper.populateConditions(builder)
    val courseHourTypeId = getInt("courseHourTypeId")
    if (courseHourTypeId != null) {
      val courseHourType = entityDao.get(classOf[CourseHourType], courseHourTypeId)
      val hours = getFloat("courseHourType_hours")
      if (null != hours && hours.longValue() > 0) {
        builder.where("hours[" + courseHourType.id.toString + "] =:hours", hours)
      }
    }
    val startDate = getDate("course.beginTime")
    val endDate = getDate("course.endTime")
    if (null != startDate) {
      builder.where("course.establishOn >= :startDate", startDate)
    }
    if (null != endDate) {
      builder.where("course.establishOn <= :endDate", endDate)
    }
    if (Strings.isNotEmpty(get("ifNotUsed")) && !getBoolean("ifNotUsed")) {
      builder.where("not exists (select m.id from org.openurp.edu.teach.plan.MajorPlanCourse m where m.course.id=course.id)")
      builder.where("not exists (select p.id from org.openurp.edu.teach.plan.StdPlanCourse p where p.course.id=course.id)")
      builder.where("not exists (select s.id from org.openurp.edu.eams.teach.program.share.SharePlanCourse s where s.course.id=course.id)")
      builder.where("not exists (select o.id from org.openurp.edu.eams.teach.program.original.OriginalPlanCourse o where o.course.id=course.id)")
      builder.where("not exists (select l.id from org.openurp.edu.teach.lesson.Lesson l where l.course.id=course.id)")
      builder.where("not exists (select c.id from org.openurp.edu.teach.grade.CourseGrade c where c.course.id=course.id)")
    } else if (Strings.isNotEmpty(get("ifNotUsed")) && getBoolean("ifNotUsed")) {
      builder.where("exists (select m.id from org.openurp.edu.teach.plan.MajorPlanCourse m where m.course.id=course.id) " + 
        "or exists (select p.id from org.openurp.edu.teach.plan.StdPlanCourse p where p.course.id=course.id) " + 
        "or exists (select s.id from org.openurp.edu.eams.teach.program.share.SharePlanCourse s where s.course.id=course.id) " + 
        "or exists (select o.id from org.openurp.edu.eams.teach.program.original.OriginalPlanCourse o where o.course.id=course.id) " + 
        "or exists (select l.id from org.openurp.edu.teach.lesson.Lesson l where l.course.id=course.id) " + 
        "or exists (select c.id from org.openurp.edu.teach.grade.CourseGrade c where c.course.id=course.id)")
    }
    builder.limit(getPageLimit)
    var orderByPras = get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "course.code"
    }
    builder.orderBy(Order.parse(orderByPras))
    builder
  }

  def validateCourseByCode() {
    val code = get("code")
    val id = getLong("id")
    var result = false
    if (Strings.isNotBlank(code)) {
      result = entityDao.duplicate(classOf[Course], id, "code", code)
    }
    var writer: Writer = null
    val response = getResponse
    val request = getRequest
    try {
      response.setContentType(request.getContentType)
      writer = response.getWriter
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

  def searchByCodeOrNameAjax(): String = {
    val codeOrName = get("term")
    val query = OqlBuilder.from(classOf[Course], "course")
    query.where("course.enabled = true").orderBy("course.code")
    val excludeCodes = get("excludeCodes")
    if (Strings.isNotBlank(excludeCodes)) {
      query.where("course.code not in (:excludeIds)", Strings.split(excludeCodes))
    }
    if (getSession.get("projectId") != null) {
      query.where("course.project = :project", getProject)
    }
    populateConditions(query)
    if (Strings.isNotEmpty(codeOrName)) {
      query.where("(course.name like :name or course.code like :code)", '%' + codeOrName + '%', '%' + codeOrName + '%')
    }
    query.limit(getPageLimit)
    put("courses", entityDao.search(query))
    forward("coursesJSON")
  }

  protected def getExportDatas(): Iterable[_] = {
    val courseIds = get("courseIds")
    if (Strings.isNotBlank(courseIds)) {
      entityDao.get(classOf[Course], Strings.splitToLong(courseIds))
    } else {
      val builder = buildCourseQuery()
      builder.limit(null)
      entityDao.search(builder)
    }
  }

  protected def getPropertyExtractor(): PropertyExtractor = {
    val pe = new CoursePropertyExtractor(getTextResource)
    pe.setCourseService(courseService)
    pe
  }

  def info(): String = {
    put("extInfo", courseService.getCourseExtInfo(getLongId(getShortName)))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    super.info()
  }

  def getEntityName(): String = classOf[Course].getName
}
