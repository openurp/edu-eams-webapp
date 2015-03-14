package org.openurp.edu.eams.teach.grade.lesson.web.action

import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.List
import java.util.Map
import org.beangle.commons.lang.Strings
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PagedList
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.base.StudentJournal
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.CourseSchedule
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.dao.LessonPlanRelationDao
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import org.openurp.edu.eams.teach.lesson.model.LessonBean
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class RetakeAction extends SemesterSupportAction {

  private var lessonSeqNoGenerator: LessonSeqNoGenerator = _

  private var lessonPlanRelationDao: LessonPlanRelationDao = _

  def search(): String = {
    val datas = stats(null)
    var orderBy = get(Order.ORDER_STR)
    if (Strings.isEmpty(orderBy)) {
      orderBy = "stat.newspace desc"
    }
    if (orderBy.startsWith("stat.")) orderBy = Strings.substringAfter(orderBy, "stat.")
    Collections.sort(datas, new PropertyComparator(orderBy))
    put("stats", new PagedList(datas, getPageLimit))
    forward()
  }

  private def stats(courseIds: Array[Long]): List[RetakeCourseStat] = {
    val courseCode = get("course.code")
    val courseName = get("course.name")
    val departId = getLong("course.department.id")
    var builder = OqlBuilder.from(classOf[CourseGrade], "cg")
    builder.select("new org.openurp.edu.eams.teach.grade.lesson.web.action.RetakeCourseStat(cg.course,count(distinct cg.std.id))")
      .groupBy("cg.course")
      .where("cg.passed=false")
      .where("cg.status=" + Grade.Status.PUBLISHED)
      .where("not exists(from " + classOf[CourseGrade].getName + 
      " cg2 where cg2.std=cg.std and cg2.course=cg.course and cg2.id!=cg.id and cg2.passed=true)")
    builder.where("exists( from " + classOf[StudentJournal].getName + 
      " sj where sj.std=cg.std and sj.beginOn<=:now and (sj.endOn=null or sj.endOn>=:now) )", new Date())
    if (null != courseIds && courseIds.length > 0) {
      builder.where("cg.course.id in (:courseIds)", courseIds)
    } else {
      if (Strings.isNotBlank(courseCode)) {
        builder.where("cg.course.code like :code", "%" + courseCode + "%")
      }
      if (Strings.isNotBlank(courseName)) {
        builder.where("cg.course.name like :name", "%" + courseName + "%")
      }
      if (null != departId) {
        builder.where("cg.course.department.id =:departmentId", departId)
      } else {
        builder.where("cg.course.department in(:departments)", getDeparts)
      }
      builder.where("cg.courseType.name not like :typeName", "%类")
    }
    val statList = entityDao.search(builder).asInstanceOf[List[RetakeCourseStat]]
    val stats = CollectUtils.newHashMap()
    for (stat <- statList) {
      stats.put(stat.getCourse.getId, stat)
    }
    val semesterId = getIntId("semester")
    builder = OqlBuilder.from(classOf[Lesson], "l")
    builder.select("l.course.id,sum(l.teachClass.limitCount-l.teachClass.stdCount)")
      .groupBy("l.course.id")
    builder.where("l.semester.id=:semesterId and l.project=:project", semesterId, getProject)
      .where("l.teachClass.limitCount>l.teachClass.stdCount")
    if (null != courseIds && courseIds.length > 0) {
      builder.where("l.course.id in (:courseIds)", courseIds)
    } else {
      if (Strings.isNotBlank(courseCode)) {
        builder.where("l.course.code like :code", "%" + courseCode + "%")
      }
      if (Strings.isNotBlank(courseName)) {
        builder.where("l.course.name like :name", "%" + courseName + "%")
      }
      if (null != departId) {
        builder.where("l.course.department.id =:departmentId", departId)
      } else {
        builder.where("l.course.department in(:departments)", getDeparts)
      }
    }
    val lessonCourses = entityDao.search(builder)
    for (data <- lessonCourses) {
      val datas = data.asInstanceOf[Array[Any]]
      val course = datas(0).asInstanceOf[java.lang.Long]
      val free = datas(1).asInstanceOf[Number]
      val stat = stats.get(course)
      if (null != stat && null != free) {
        stat.setFreespace(free.intValue())
      }
    }
    CollectUtils.newArrayList(stats.values)
  }

  def unpassed(): String = {
    val courseId = getLong("course.id")
    val course = entityDao.get(classOf[Course], courseId)
    val builder = OqlBuilder.from(classOf[CourseGrade], "grade")
    builder.where("grade.passed=false and grade.course.id=:courseId", courseId)
      .where("grade.status=" + Grade.Status.PUBLISHED)
      .where("not exists(from " + classOf[CourseGrade].getName + 
      " cg2 where cg2.std=grade.std and cg2.course=grade.course and cg2.id!=grade.id and cg2.passed=true)")
    builder.where("exists( from " + classOf[StudentJournal].getName + 
      " sj where sj.std=grade.std and sj.beginOn<=:now and (sj.endOn=null or sj.endOn>=:now) )", new Date())
    builder.select("count(distinct grade.std.id)")
    val rs = entityDao.search(builder)
    put("count", rs.get(0))
    builder.select(null)
    builder.orderBy(get(Order.ORDER_STR))
    builder.limit(getPageLimit)
    put("course", course)
    put("grades", entityDao.search(builder))
    forward()
  }

  def freespace(): String = {
    val courseId = getLongId("course")
    val semesterId = getIntId("semester")
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.semester.id=:semesterId and lesson.project=:project", semesterId, getProject)
      .where("lesson.teachClass.limitCount>lesson.teachClass.stdCount")
      .where("lesson.course.id=:courseId", courseId)
    var orderBy = get(Order.ORDER_STR)
    if (Strings.isEmpty(orderBy)) {
      orderBy = "lesson.teachClass.limitCount-lesson.teachClass.stdCount desc,lesson.no"
    }
    builder.limit(getPageLimit).orderBy(orderBy)
    put("lessons", entityDao.search(builder))
    forward()
  }

  def newLesson(): String = {
    val courseIds = getLongIds("stat")
    val semesterId = getIntId("semester")
    val semester = entityDao.get(classOf[Semester], semesterId)
    val stats = stats(courseIds)
    val lessons = CollectUtils.newArrayList()
    val project = getProject
    for (stat <- stats) {
      val course = stat.getCourse
      val lesson = LessonBean.getDefault.asInstanceOf[LessonBean]
      lesson.getTeachClass.setLimitCount(stat.getNewspace)
      lesson.setProject(project)
      lesson.setCourse(course)
      lesson.setTeachDepart(course.department)
      lesson.setCourseType(course.getCourseType)
      lesson.setSemester(semester)
      lesson.setExamMode(course.getExamMode)
      val courseSchedule = lesson.getCourseSchedule
      val startWeek = 1
      var endWeek = startWeek
      endWeek = if (course.getWeeks != null && course.getWeeks > 0) startWeek + course.getWeeks - 1 else if (course.getWeekHour != 0) startWeek + (course.getPeriod / course.getWeekHour).toInt - 
        1 else startWeek + semester.getWeeks - 1
      courseSchedule.setWeekState(WeekStates.build(startWeek + "-" + endWeek))
      val teachClass = lesson.getTeachClass.asInstanceOf[TeachClassBean]
      teachClass.setName("重修班")
      lesson.setCreatedAt(new Date(System.currentTimeMillis()))
      lesson.setUpdatedAt(new Date(System.currentTimeMillis()))
      lesson.setAuditStatus(CommonAuditState.UNSUBMITTED)
      lessons.add(lesson)
    }
    lessonSeqNoGenerator.genLessonSeqNos(new ArrayList[Lesson](lessons))
    for (lesson <- lessons) {
      entityDao.saveOrUpdate(lesson)
      lessonPlanRelationDao.saveRelation(null, lesson)
    }
    redirect("search", "info.action.success")
  }

  def setLessonSeqNoGenerator(lessonSeqNoGenerator: LessonSeqNoGenerator) {
    this.lessonSeqNoGenerator = lessonSeqNoGenerator
  }

  def setLessonPlanRelationDao(lessonPlanRelationDao: LessonPlanRelationDao) {
    this.lessonPlanRelationDao = lessonPlanRelationDao
  }
}
