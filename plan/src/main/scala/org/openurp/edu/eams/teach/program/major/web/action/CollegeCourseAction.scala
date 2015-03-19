package org.openurp.edu.eams.teach.program.major.web.action

import java.sql.Date

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.QueryHelper
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.CourseExtInfo
import com.ekingstar.eams.teach.code.school.CourseHourType
import com.ekingstar.eams.teach.course.web.action.CourseSearchAction
import com.ekingstar.eams.teach.model.CourseBean
//remove if not needed


class CollegeCourseAction extends CourseSearchAction {

  def buildCourseQuery(): OqlBuilder[Course] = {
    val builder = OqlBuilder.from(classOf[Course], "course")
    QueryHelper.populateConditions(builder)
    builder.where("course.project = :project", getProject)
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
      builder.where("not exists (select p.id from org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse p where p.course.id=course.id)")
      builder.where("not exists (select s.id from org.openurp.edu.eams.teach.program.share.SharePlanCourse s where s.course.id=course.id)")
      builder.where("not exists (select o.id from org.openurp.edu.eams.teach.program.original.OriginalPlanCourse o where o.course.id=course.id)")
      builder.where("not exists (select l.id from com.ekingstar.eams.teach.lesson.Lesson l where l.course.id=course.id)")
      builder.where("not exists (select c.id from com.ekingstar.eams.teach.lesson.CourseGrade c where c.course.id=course.id)")
    } else if (Strings.isNotEmpty(get("ifNotUsed")) && getBoolean("ifNotUsed")) {
      builder.where("exists (select m.id from org.openurp.edu.teach.plan.MajorPlanCourse m where m.course.id=course.id) " + 
        "or exists (select p.id from org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse p where p.course.id=course.id) " + 
        "or exists (select s.id from org.openurp.edu.eams.teach.program.share.SharePlanCourse s where s.course.id=course.id) " + 
        "or exists (select o.id from org.openurp.edu.eams.teach.program.original.OriginalPlanCourse o where o.course.id=course.id) " + 
        "or exists (select l.id from com.ekingstar.eams.teach.lesson.Lesson l where l.course.id=course.id) " + 
        "or exists (select c.id from com.ekingstar.eams.teach.lesson.CourseGrade c where c.course.id=course.id)")
    }
    builder.limit(getPageLimit)
    var orderByPras = get(Order.ORDER_STR)
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "course.code"
    }
    builder.orderBy(Order.parse(orderByPras))
    builder.where("course.department in(:departments)", getDeparts)
    builder
  }

  protected override def editSetting(entity: Entity[_]) {
    val course = entity.asInstanceOf[Course]
    val extInfos = entityDao.get(classOf[CourseExtInfo], "course", course)
    if (!extInfos.isEmpty) {
      put("extInfo", extInfos.get(0))
    }
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
  }

  protected override def saveAndForward(entity: Entity[_]): String = {
    val date = new java.util.Date()
    val course = entity.asInstanceOf[CourseBean]
    val types = baseCodeService.getCodes(classOf[CourseHourType])
    var total = 0
    for (courseHourType <- types) {
      val hour = getInt("hourTypeId" + courseHourType.id)
      if (hour != null) total += hour
    }
    if (total > 0) course.setPeriod(total)
    if (course.isPersisted) {
      course.setCreatedAt(date)
    }
    course.setUpdatedAt(date)
    val extInfo = populateEntity(classOf[CourseExtInfo], "extInfo")
    try {
      entityDao.saveOrUpdate(course, extInfo)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
      }
    }
  }
}
