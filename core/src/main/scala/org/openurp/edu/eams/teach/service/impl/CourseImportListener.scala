package org.openurp.edu.eams.teach.service.impl

import java.util.Date


import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.entity.util.ValidEntityKeyPredicate
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.TransferMessage
import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.importer.listener.ItemImporterListener
import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.Course
import org.openurp.edu.base.CourseExtInfo
import org.openurp.edu.base.CourseHour
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.eams.teach.code.school.CourseCategory
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.model.CourseBean
import org.openurp.edu.eams.teach.service.CourseService



class CourseImportListener(protected var courseService: CourseService, 
    private var primaryKey: String, 
    private var courseHourTypes: List[CourseHourType], 
    private var entityDao: EntityDao, 
    private var project: Project) extends ItemImporterListener {

  var errors: List[TransferMessage] = CollectUtils.newArrayList()

  def this(courseService: CourseService, 
      courseHourTypes: List[CourseHourType], 
      entityDao: EntityDao, 
      project: Project) {
    this(courseService, "code", courseHourTypes, entityDao, project)
  }

  override def onFinish(tr: TransferResult) {
  }

  override def onItemStart(tr: TransferResult) {
  }

  override def onItemFinish(tr: TransferResult) {
    val course = populateCourse(tr)
    if (course.credits == 0) {
      tr.addFailure("error.parameters.needed", "credits must not be zero")
    }
    if (!tr.hasErrors()) {
      try {
        val extInfo = populateCourseExtInfo(tr, course)
        courseService.saveOrUpdate(course, extInfo)
      } catch {
        case e: ConstraintViolationException => for (constraintViolation <- e.constraintViolations) {
          tr.addFailure(constraintViolation.propertyPath + constraintViolation.message, constraintViolation.invalidValue)
        }
      }
    }
  }

  private def getPropEntity[T <: Entity[_]](clazz: Class[T], 
      tr: TransferResult, 
      key: String, 
      notNull: Boolean): T = {
    val description = importer.descriptions.get(key)
    val value = importer.curData.get(key).asInstanceOf[String]
    if (Strings.isBlank(value)) {
      if (notNull) {
        tr.addFailure(description + "不能为空", value)
      } else {
        return null
      }
    }
    val nameList = entityDao.get(clazz, "name", value)
    if (nameList.size != 1) {
      val codeList = entityDao.get(clazz, "code", value)
      if (codeList.size == 1) {
        return codeList.get(0)
      } else if ((nameList.size + codeList.size) == 0) {
        tr.addFailure(importer.descriptions.get(key) + "不存在", value)
      } else {
        tr.addFailure(importer.descriptions.get(key) + "存在多条记录", value)
      }
      return null
    }
    nameList.get(0)
  }

  private def populateCourse(tr: TransferResult): Course = {
    val params = importer.curData
    val code = params.get(primaryKey).asInstanceOf[String]
    var course: Course = null
    if (ValidEntityKeyPredicate.Instance.apply(code)) {
      course = courseService.course(code)
    }
    if (null == course) {
      course = new CourseBean()
      course.code=code
    }
    course.project=project
    populatePeriod(course, tr)
    var key = "name"
    var value = importer.curData.get(key).asInstanceOf[String]
    if (Strings.isNotBlank(value)) {
      course.name=value
    } else if (course.isTransient) {
      tr.addFailure(importer.descriptions.get(key) + "不能为空", "")
    }
    key = "credits"
    value = importer.curData.get(key).asInstanceOf[String]
    if (Strings.isNotBlank(value)) {
      try {
        val credits = java.lang.Float.parseFloat(value)
        if (credits < 0) {
          tr.addFailure(importer.descriptions.get(key) + "必须是非负数值", value)
        } else {
          course.credits=credits
        }
      } catch {
        case e: Exception => tr.addFailure(importer.descriptions.get(key) + "必须是数值", value)
      }
    }
    val category = getPropEntity(classOf[CourseCategory], tr, "category", false)
    if (null != category) {
      course.category=category
    }
    val department = getPropEntity(classOf[Department], tr, "department", false)
    if (null != department) {
      course.department=department
    }
    val education = getPropEntity(classOf[Education], tr, "education", false)
    if (null != education) {
      course.education=education
    }
    val courseType = getPropEntity(classOf[CourseType], tr, "courseType", false)
    if (null != courseType) {
      course.courseType=courseType
    }
    val examMode = getPropEntity(classOf[ExamMode], tr, "examMode", false)
    if (null != examMode) {
      course.examMode=examMode
    }
    val enabledStr = importer.curData.get("enabled").asInstanceOf[String]
    course.enabled="是" == enabledStr || "y" == enabledStr || "Y" == enabledStr
    course.updatedAt=new Date()
    if (course.isTransient) {
      course.createdAt=new Date()
    }
    course
  }

  private def populateCourseExtInfo(tr: TransferResult, course: Course): CourseExtInfo = {
    var extInfo: CourseExtInfo = null
    val params = importer.curData
    if (course.isPersisted) {
      extInfo = courseService.courseExtInfo(course.id)
    }
    if (null == extInfo) {
      extInfo = Model.newInstance(classOf[CourseExtInfo])
      extInfo.course=course
    }
    val requirement = params.get("extInfo.requirement").asInstanceOf[String]
    if (Strings.isNotBlank(requirement)) {
      extInfo.requirement=requirement
    }
    val description = params.get("extInfo.description").asInstanceOf[String]
    if (Strings.isNotBlank(description)) {
      extInfo.description=description
    }
    extInfo
  }

  private def populatePeriod(course: Course, tr: TransferResult) {
    var key = "period"
    val periodStr = importer.curData.get(key).asInstanceOf[String]
    if (Strings.isNotBlank(periodStr)) {
      try {
        val periodInt = java.lang.Integer.parseInt(periodStr)
        if (periodInt > -1) {
          course.period=periodInt
        } else {
          tr.addFailure(importer.descriptions.get(key) + "必须为非负整数", periodStr)
        }
      } catch {
        case e: Exception => tr.addFailure(importer.descriptions.get(key) + "必须为非负整数", periodStr)
      }
    } else if (course.isTransient) {
      tr.addFailure(importer.descriptions.get(key) + "不能为空", "")
    }
    key = "weekHour"
    val weedHourFloatStr = importer.curData.get(key).asInstanceOf[String]
    if (Strings.isNotBlank(weedHourFloatStr)) {
      try {
        val weekHourFloat = java.lang.Integer.parseInt(weedHourFloatStr)
        if (weekHourFloat >= 0) {
          course.weekHour=weekHourFloat
        } else {
          tr.addFailure(importer.descriptions.get(key) + "必须为大于0的数值", weedHourFloatStr)
        }
      } catch {
        case e: Exception => tr.addFailure(importer.descriptions.get(key) + "必须为大于0的数值", weedHourFloatStr)
      }
    }
    for (courseHourType <- courseHourTypes) {
      key = "period_hours_" + courseHourType.code
      val valueStr = importer.curData.get(key).asInstanceOf[String]
      if (Strings.isNotBlank(valueStr)) {
        try {
          val value = java.lang.Integer.parseInt(valueStr)
          if (value > -1) {
            val courseHour = Model.newInstance(classOf[CourseHour])
            courseHour.type=courseHourType
            courseHour.period=value
            courseHour.course=course
            course.hours.add(courseHour)
          } else {
            tr.addFailure(importer.descriptions.get(key) + "必须为非负整数", valueStr)
          }
        } catch {
          case e: Exception => tr.addFailure(importer.descriptions.get(key) + "必须为非负整数", valueStr)
        }
      }
    }
  }
}
