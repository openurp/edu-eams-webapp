package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Date
import java.util.List
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.CourseLimitUtils
import org.openurp.edu.eams.teach.schedule.util.PropertyCollectionComparator.ArrangeOrder

import scala.collection.JavaConversions._

class MultiManualArrangeForDepartAction extends MultiManualArrangeAction {

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.project=:project1", getProject)
    populateConditions(builder, "lesson.id")
    val teacherId = getLong("teacher.id")
    var arrangeOrder: ArrangeOrder = null
    try {
      arrangeOrder = ArrangeOrder.valueOf(get("orderRule"))
    } catch {
      case e: Exception => arrangeOrder = ArrangeOrder.TEACHER
    }
    if (null != teacherId) {
      builder.join("lesson.teachers", "teacher")
      builder.where("teacher.id =:teacherId", teacherId)
    }
    builder.where("exists (from org.openurp.edu.eams.teach.schedule.model.LessonForDepart " + 
      "lfd join lfd.lessonIds lessonId where lesson.id = lessonId and lfd.project=lesson.project and lfd.semester = lesson.semester " + 
      "and lfd.department in (:departments) and (lfd.beginAt is null or lfd.beginAt <= :now) and (lfd.endAt is null or lfd.endAt >= :now))", 
      getDeparts, new Date())
    val startWeekToEndWeek = get("startWeekToEndWeek")
    if (Strings.isNotEmpty(startWeekToEndWeek)) {
      val weeks = startWeekToEndWeek.split("-")
      builder.where("lesson.courseSchedule.startWeek = :startWeek", java.lang.Integer.valueOf(weeks(0)))
        .where("lesson.courseSchedule.endWeek = :endWeek", java.lang.Integer.valueOf(weeks(1)))
    }
    val occupancied = getBoolean("occupancied")
    if (null != occupancied) {
      if (true == occupancied) {
        builder.where("exists(from lesson.courseSchedule.activities activity where size(activity.rooms)>0)")
      } else {
        builder.where("exists(from lesson.courseSchedule.activities activity where size(activity.rooms)=0)")
      }
    }
    val adminclassId = getInt("adminclassId")
    if (null != adminclassId) {
      val con = CourseLimitUtils.build(entityDao.get(classOf[Adminclass], adminclassId), "lgi")
      val params = con.getParams
      builder.where("exists(from lesson.teachClass.limitGroups lg join lg.items as lgi where" + 
        con.getContent + 
        ")", params.get(0), params.get(1), params.get(2))
    }
    builder.where("lesson.project = :project", getProject)
    val isArrangeCompleted = get("status")
    if (Strings.isNotEmpty(isArrangeCompleted)) {
      if (isArrangeCompleted == CourseStatusEnum.NEED_ARRANGE.toString) {
        builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.NEED_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.ARRANGED.toString) {
        builder.where("lesson.courseSchedule.status = :status", CourseStatusEnum.ARRANGED)
      }
    } else {
      builder.where("lesson.courseSchedule.status <> :status", CourseStatusEnum.DONT_ARRANGE)
    }
    val guapai = getBoolean("guapai")
    if (null != guapai) {
      if (true == guapai) {
        builder.join("lesson.tags", "tag")
        builder.where("tag.id = :guapaiId", LessonTag.PredefinedTags.GUAPAI.getId)
      } else {
        builder.where("not exists (from lesson.tags tag where tag.id = :guapaiId)", LessonTag.PredefinedTags.GUAPAI.getId)
      }
    }
    builder.where("lesson.auditStatus = :auditStatus", CommonAuditState.ACCEPTED)
    builder
  }

  protected override def getStartWeekToEndWeeks(project: Project, semester: Semester): List[_] = {
    val builder = OqlBuilder.from(classOf[Lesson], "lesson").where("lesson.project =:project", project)
      .where("lesson.semester = :semester", semester)
      .select("distinct lesson.courseSchedule.startWeek,lesson.courseSchedule.endWeek")
      .where("exists (from org.openurp.edu.eams.teach.schedule.model.LessonForDepart lfd join lfd.lessonIds lessonId where lesson.id = lessonId and lfd.department in (:departments))", 
      getDeparts)
      .orderBy("lesson.courseSchedule.startWeek,lesson.courseSchedule.endWeek")
    entityDao.search(builder)
  }
}
