package org.openurp.edu.eams.teach.schedule.web.action

import java.sql.Timestamp

import java.util.Date


import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Department
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.schedule.model.LessonForDepart



class ManualArrangeForDepartAction extends ManualArrangeAction {

  def taskList(): String = {
    val query = getLessonBuilder
    query.where("lesson.project.id = :projectId1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    if (Strings.isEmpty(get(Order.ORDER_STR))) {
      query.orderBy("lesson.no")
    }
    val isArrangeCompleted = get("status")
    put("teacherIsNull", getBool("fake.teacher.null"))
    if (Strings.isNotEmpty(isArrangeCompleted)) {
      if (isArrangeCompleted == CourseStatusEnum.NEED_ARRANGE.toString) {
        query.where("lesson.schedule.status = :status", CourseStatusEnum.NEED_ARRANGE)
        put("courseStatusEnum", CourseStatusEnum.NEED_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.DONT_ARRANGE.toString) {
        query.where("lesson.schedule.status = :status", CourseStatusEnum.DONT_ARRANGE)
        put("courseStatusEnum", CourseStatusEnum.DONT_ARRANGE)
      } else if (isArrangeCompleted == CourseStatusEnum.ARRANGED.toString) {
        query.where("lesson.schedule.status = :status", CourseStatusEnum.ARRANGED)
        put("courseStatusEnum", CourseStatusEnum.ARRANGED)
      }
    }
    put("project", getProject)
    put("semester", putSemester(null))
    val departments = getDeparts
    if (departments.isEmpty) {
      query.where("1=2")
    } else {
      query.where("exists (from org.openurp.edu.eams.teach.schedule.model.LessonForDepart " + 
        "lfd join lfd.lessonIds lessonId where lesson.id = lessonId and lfd.project=lesson.project and lfd.semester = lesson.semester " + 
        "and lfd.department in (:departments))", departments)
    }
    val lfdBuilder = OqlBuilder.from(classOf[LessonForDepart], "lessonForDepart")
    lfdBuilder.where("lessonForDepart.project = :project", getProject)
    lfdBuilder.where("lessonForDepart.semester = :semester", putSemester(null))
    if (departments.isEmpty) {
      lfdBuilder.where("1=2")
    } else {
      lfdBuilder.where("lessonForDepart.department in (:departments)", departments)
    }
    val lessonForDeparts = entityDao.search(lfdBuilder)
    var collegeManual = false
    val date = new Date()
    if (!lessonForDeparts.isEmpty) {
      for (lessonForDepart <- lessonForDeparts if (lessonForDepart.getBeginAt == null || date.after(lessonForDepart.getBeginAt)) && 
        (lessonForDepart.getEndAt == null || date.before(lessonForDepart.getEndAt))) {
        collegeManual = true
        //break
      }
    }
    put("collegeManual", collegeManual)
    val lessons = entityDao.search(query)
    put("lessons", lessons)
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    val arrangeInfo = Collections.newMap[Any]
    for (oneTask <- lessons) {
      arrangeInfo.put(oneTask.id.toString, digestor.digest(getTextResource, oneTask, ":teacher+ :day :units :weeks :room"))
    }
    put("arrangeInfo", arrangeInfo)
    put("weekStates", new WeekStates())
    forward()
  }

  private def getLessonBuilder(): OqlBuilder[Lesson] = {
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    val lessonIdSeq = Params.get("splitIds")
    if (Strings.isNotEmpty(lessonIdSeq)) {
      query.where("lesson.id in (:lessonIds)", Strings.splitToLong(lessonIdSeq))
      query.limit(QueryHelper.getPageLimit)
      return query
    }
    QueryHelper.populateConditions(query, "lesson.schedule.status")
    val courseTypeName = Params.get("lesson.courseType.name")
    val courseTypeNameNotLike = Params.getBool("fake.courseType.name.notLike")
    if (Strings.isNotBlank(Strings.trim(courseTypeName))) {
      if (courseTypeNameNotLike) {
        query.where("lesson.courseType.name not like :courseTypeName", "%" + courseTypeName + "%")
      } else {
        query.where("lesson.courseType.name like :courseTypeName", "%" + courseTypeName + "%")
      }
    }
    val teacherConditions = QueryHelper.extractConditions(classOf[Teacher], "teacher", null)
    val teacherIsNull = Params.getBool("fake.teacher.null")
    if (teacherIsNull) {
      query.where("size(lesson.teachers) = 0")
    }
    if (Collections.isNotEmpty(teacherConditions)) {
      query.join("left outer", "lesson.teachers", "teacher1")
      query.join("lesson.teachers", "teacher")
      query.where(teacherConditions)
    }
    val status = Params.get("fake.auditStatus")
    if (Strings.isNotEmpty(status)) {
      query.where("str(lesson.auditStatus)=:status", status)
    }
    val courseActiviyConditions = QueryHelper.extractConditions(classOf[CourseActivity], "courseActivity", 
      "courseActivity.time.startUnit")
    val startUnit = Params.getInt("courseActivity.time.startUnit")
    if (Collections.isNotEmpty(courseActiviyConditions) || null != startUnit) {
      query.join("lesson.schedule.activities", "courseActivity")
      if (Collections.isNotEmpty(courseActiviyConditions)) {
        query.where(courseActiviyConditions)
      }
      if (null != startUnit) {
        query.where("courseActivity.time.startUnit <= :startUnit", startUnit)
        query.where("courseActivity.time.endUnit >= :endUnit", startUnit)
      }
    }
    val courseScheduleStatus = Params.get("lesson.schedule.status")
    if (Strings.isNotEmpty(courseScheduleStatus)) {
      query.where("str(lesson.schedule.status) = :status", courseScheduleStatus)
    }
    val weeks = Params.getInt("fake.weeks")
    if (null != weeks) {
      query.where("(lesson.schedule.endWeek - lesson.schedule.startWeek  + 1 )= :weeks", 
        weeks)
    }
    val weekHour = getInt("fake.weekHour")
    if (null != weekHour) {
      query.where("lesson.course.period / (lesson.schedule.endWeek - lesson.schedule.startWeek  + 1 )= :weekHour", 
        weekHour)
    }
    val startWeek = Params.getInt("fake.week.start")
    if (null != startWeek) {
      query.where("lesson.schedule.startWeek = :startWeek", startWeek)
    }
    val endWeek = Params.getInt("fake.week.end")
    if (null != endWeek) {
      query.where("lesson.schedule.endWeek = :endWeek", endWeek)
    }
    val lessThan_EndWeek = Params.getInt("lessThanEndWeek")
    if (null != lessThan_EndWeek) {
      query.where("lesson.schedule.endWeek <= :lessThanEndWeek", lessThan_EndWeek)
    }
    val limitCountStart = Params.getInt("fake.limitCount.start")
    if (null != limitCountStart) {
      query.where("lesson.teachClass.limitCount >= :limitCountStart", limitCountStart)
    }
    val limitCountEnd = Params.getInt("fake.limitCount.end")
    if (null != limitCountEnd) {
      query.where("lesson.teachClass.limitCount <= :limitCountEnd", limitCountEnd)
    }
    val stdCountStart = Params.getInt("fake.stdCount.start")
    if (null != stdCountStart) {
      query.where("lesson.teachClass.stdCount >= :stdCountStart", stdCountStart)
    }
    val stdCountEnd = Params.getInt("fake.stdCount.end")
    if (null != stdCountEnd) {
      query.where("lesson.teachClass.stdCount <= :stdCountEnd", stdCountEnd)
    }
    val crossdepart = Params.getBoolean("fake.crossdepart")
    if (true == crossdepart) {
      query.where("(lesson.teachClass.depart != lesson.teachDepart or lesson.teachClass.depart is null)")
    } else if (false == crossdepart) {
      query.where("lesson.teachClass.depart = lesson.teachDepart")
    }
    val guapai = Params.getBoolean("fake.guapai")
    if (true == guapai) {
      query.where("exists (select tag.id from lesson.tags tag where tag.id=:guaPai)", LessonTag.PredefinedTags.GUAPAI.id)
    } else if (false == guapai) {
      query.where("not exists (select tag.id from lesson.tags tag where tag.id=:guaPai)", LessonTag.PredefinedTags.GUAPAI.id)
    }
    val compare = Params.getInt("electInfo.electCountCompare")
    if (null != compare) {
      var op = ""
      op = if (compare.intValue() == 0) "=" else if (compare.intValue() < 0) "<" else ">"
      query.where("lesson.teachClass.stdCount " + op + " lesson.teachClass.maxStdCount")
    }
    val weekday = Params.getInt("fake.time.day")
    if (null != weekday) {
      query.where("exists (select activity.id from lesson.schedule.activities activity where activity.time.day=:weekday)", 
        weekday)
    }
    val unit = Params.getInt("fake.time.unit")
    if (null != unit) {
      query.where("exists (select activity.id from lesson.schedule.activities activity where activity.time.startUnit <= :unit and :unit <= activity.time.endUnit)", 
        unit)
    }
    var isExamArrangeComplete = Params.getInt("isExamArrangeComplete")
    if (isExamArrangeComplete == null) {
      isExamArrangeComplete = 3
    }
    val examTypeId = Params.getInt("examType.id")
    if (examTypeId != null && examTypeId == ExamType.MAKEUP) {
      query.where("lesson.id in(select distinct take.lesson.id from org.openurp.edu.teach.exam.ExamTake take where take.examType.id = :examTypeId)", 
        ExamType.MAKEUP)
    }
    if (isExamArrangeComplete == 1) {
      var activitySubQuery = "exists( from org.openurp.edu.teach.exam.ExamActivity examActivity left join examActivity.lessons activityLesson" + 
        " where activityLesson=lesson and examActivity.examType.id=:examTypeId"
      val activityParams = Collections.newBuffer[Any]
      activityParams.add(examTypeId)
      val examRoom = Params.get("exam.room.name")
      if (Strings.isNotEmpty(examRoom)) {
        activitySubQuery += " and examActivity.room.name like :roomName"
        activityParams.add("%" + examRoom + "%")
      }
      val departId = Params.get("exam.examiner.department.id")
      if (Strings.isNotEmpty(departId)) {
        activitySubQuery += " and examActivity.examiner.department.id = :departId"
        activityParams.add(java.lang.Long.valueOf(departId))
      }
      val examStartTime = Params.get("exam.startTime")
      if (Strings.isNotBlank(examStartTime)) {
        val startTime = Timestamp.valueOf(examStartTime + ":00")
        activitySubQuery += " and examActivity.startAt = :examStartTime"
        activityParams.add(startTime)
      }
      val examEndTime = Params.get("exam.endTime")
      if (Strings.isNotBlank(examEndTime)) {
        val endTime = Timestamp.valueOf(examEndTime + ":00")
        activitySubQuery += " and examActivity.endAt = :examEndTime"
        activityParams.add(endTime)
      }
      activitySubQuery += ")"
      val activityCondition = new Condition(activitySubQuery)
      activityCondition.params(activityParams)
      query.where(activityCondition)
    } else if (isExamArrangeComplete == 0) {
      query.where("not exists (from org.openurp.edu.teach.exam.ExamActivity exam " + 
        "left join exam.lessons e_lesson where e_lesson=lesson and exam.examType.id=:examTypeId)", examTypeId)
    }
    val queryGrouped = Params.getBoolean("arrangeInfo.examGrouped")
    if (null == queryGrouped) {
      val groupId = Params.getLong("examGroup.id")
      if (groupId != null) {
        query.where("not exists (select 1 from org.openurp.edu.eams.teach.exam.ExamGroup examGroup " + 
          "where lesson in elements(examGroup.lessons) and examGroup.id =:examGroupId) ", groupId)
      }
      val mygroupId = Params.getLong("myExamGroup.id")
      if (mygroupId != null) {
        query.where("exists (select 1 from org.openurp.edu.eams.teach.exam.ExamGroup examGroup " + 
          "where lesson in elements(examGroup.lessons) and examGroup.id =:mygroupId) ", mygroupId)
      }
    } else {
      var hql = "exists (from lesson.schedule.examGroups examGroup where examGroup.id = :examGroupId and exists (from examGroup.lessons lesson1 where lesson1 = lesson))"
      if (false == queryGrouped) {
        hql = "not " + hql
      }
      query.where(hql, examTypeId)
    }
    query.limit(QueryHelper.getPageLimit)
    if (Strings.isEmpty(Params.get("orderBy"))) {
      query.orderBy("lesson.no")
    } else {
      if ("fake.weekHour asc" == Params.get("orderBy")) {
        query.orderBy("(lesson.course.period / (lesson.schedule.endWeek + 1 - lesson.schedule.startWeek)) asc")
      } else if ("fake.weekHour desc" == Params.get("orderBy")) {
        query.orderBy("(lesson.course.period / (lesson.schedule.endWeek + 1 - lesson.schedule.startWeek)) desc")
      } else {
        query.orderBy(Order.parse(Params.get("orderBy")))
      }
    }
    query
  }
}
