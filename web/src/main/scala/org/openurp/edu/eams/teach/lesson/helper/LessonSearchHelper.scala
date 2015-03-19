package org.openurp.edu.eams.teach.lesson.helper

import java.sql.Timestamp
import java.util.Date


import org.apache.commons.lang3.StringUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.BitStrings
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.lesson.ArrangeSuggest
import org.openurp.edu.teach.exam.ExamActivity.ExamAuditState
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.util.SemesterUtil
import org.openurp.edu.eams.web.helper.SearchHelper



class LessonSearchHelper extends SearchHelper {

  def buildQuery(): OqlBuilder[Lesson] = buildQuery(true)

  def buildQuery(applyRestriction: Boolean): OqlBuilder[Lesson] = {
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    if (applyRestriction) {
      restrictionHelper.applyRestriction(query)
    }
    val lessonIdSeq = Params.get("splitIds")
    if (Strings.isNotEmpty(lessonIdSeq)) {
      query.where("lesson.id in (:lessonIds)", Strings.splitToLong(lessonIdSeq))
      query.limit(QueryHelper.getPageLimit)
      return query
    }
    QueryHelper.populateConditions(query, "lesson.courseSchedule.status,lesson.id")
    val courseTypeName = Params.get("lesson.courseType.name")
    val courseTypeNameNotLike = Params.getBool("fake.courseType.name.notLike")
    if (Strings.isNotBlank(Strings.trim(courseTypeName))) {
      if (courseTypeNameNotLike) {
        query.where("lesson.courseType.name not like :courseTypeName", "%" + courseTypeName + "%")
      } else {
        query.where("lesson.courseType.name like :courseTypeName", "%" + courseTypeName + "%")
      }
    }
    val teacherIsNull = Params.getBoolean("fake.teacher.null")
    if (teacherIsNull != null) {
      if (true == teacherIsNull) {
        query.where("size(lesson.teachers) = 0")
      } else {
        query.where("size(lesson.teachers) > 0")
      }
    }
    val teacherDepart = Params.getInt("fake.teacher.department.id")
    if (teacherDepart != null) {
      query.join("lesson.teachers", "teacher")
      query.where("teacher.department = :teacherDepartment", entityDao.get(classOf[Department], teacherDepart))
    }
    if (Strings.isNotBlank(Params.get("teacher.name"))) {
      query.where("exists (from lesson.teachers _innner_teacher where _innner_teacher.name like :_t_name)", 
        '%' + Params.get("teacher.name") + '%')
    }
    val status = Params.get("fake.auditStatus")
    if (Strings.isNotBlank(status)) {
      query.where("lesson.auditStatus = :status", CommonAuditState.valueOf(status.toUpperCase()))
    }
    val buildingId = Params.getLong("fack.building.id")
    val weekday = Params.getInt("fake.time.day")
    var courseUnit = Params.getInt("courseActivity.time.startUnit")
    var activityWeekStart = Params.getInt("fake.time.weekstart")
    var activityWeekEnd = Params.getInt("fake.time.weekend")
    var activityWeekState: java.lang.Long = null
    if (null != activityWeekStart || null != activityWeekEnd) {
      if (null == activityWeekStart) activityWeekStart = activityWeekEnd
      if (null == activityWeekEnd) activityWeekEnd = activityWeekStart
      if (activityWeekEnd >= activityWeekStart && activityWeekEnd < 52 && 
        activityWeekStart > 0) {
        val sb = new StringBuilder(Strings.repeat("0", 53))
        var i = activityWeekStart
        while (i <= activityWeekEnd) {sb.setCharAt(i, '1')i += 1
        }
        activityWeekState = BitStrings.binValueOf(sb.toString)
      }
    }
    if (null == courseUnit) courseUnit = Params.getInt("fake.time.unit")
    if (null != buildingId || null != courseUnit || null != weekday || 
      null != activityWeekState) {
      val activityQuery = new StringBuilder("exists( from lesson.courseSchedule.activities as courseActivity where 1=1")
      if (null != courseUnit) activityQuery.append("and courseActivity.time.startUnit <= " + courseUnit + 
        " and courseActivity.time.endUnit >= " + 
        courseUnit)
      if (null != weekday) activityQuery.append("and courseActivity.time.day=" + weekday)
      if (null != activityWeekState) activityQuery.append(" and bitand(courseActivity.time.state," + activityWeekState + 
        ")>0")
      if (null != buildingId) activityQuery.append(" and exists(from courseActivity.rooms as cr where cr.building.id=" + 
        buildingId + 
        ")")
      activityQuery.append(")")
      query.where(activityQuery.toString)
    }
    val courseScheduleStatus = Params.get("lesson.courseSchedule.status")
    if (Strings.isNotEmpty(courseScheduleStatus)) {
      query.where("lesson.courseSchedule.status = :scheduleStatus", CourseScheduleBean.CourseStatusEnum.valueOf(courseScheduleStatus))
    }
    val weeks = Params.getInt("fake.weeks")
    if (null != weeks) {
      query.where("week_state_weeks(lesson.courseSchedule.weekState.value,1)=:weeks", weeks)
    }
    val weekHour = Params.getFloat("fake.weekHour")
    if (null != weekHour) {
      query.where("floor(lesson.course.period / (lesson.courseSchedule.endWeek - lesson.courseSchedule.startWeek  + 1 )) = :weekHour", 
        weekHour.intValue())
    }
    val startWeek = Params.getInt("fake.week.start")
    if (null != startWeek) {
      query.where("lesson.courseSchedule.startWeek = :startWeek", startWeek)
    }
    val endWeek = Params.getInt("fake.week.end")
    if (null != endWeek) {
      query.where("lesson.courseSchedule.endWeek = :endWeek", endWeek)
    }
    val lessThan_StartWeek = Params.getInt("lessThanStartWeek")
    if (null != lessThan_StartWeek) {
      query.where("lesson.courseSchedule.startWeek >= :lessThanStartWeek", lessThan_StartWeek)
    }
    val lessThan_EndWeek = Params.getInt("lessThanEndWeek")
    if (null != lessThan_EndWeek) {
      query.where("lesson.courseSchedule.endWeek <= :lessThanEndWeek", lessThan_EndWeek)
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
    val adminclassName = Params.get("fake.adminclass.name")
    if (Strings.isNotBlank(adminclassName)) {
      val adminclassQuery = OqlBuilder.from(classOf[Adminclass], "adminclass")
      adminclassQuery.where("adminclass.name like :name", "%" + adminclassName + "%")
      val adminclasses = entityDao.search(adminclassQuery)
      if (CollectUtils.isNotEmpty(adminclasses)) {
        val adminclassCondition = new StringBuilder()
        adminclassCondition.append("exists (").append("select litem.id from lesson.teachClass.limitGroups lgroup join lgroup.items litem where")
          .append(" litem.meta.id=")
          .append(CourseLimitMetaEnum.ADMINCLASS.getMetaId)
          .append(" and ")
          .append(" str(litem.operator) in ('IN', 'EQUAL')")
          .append(" and (")
        var iter = adminclasses.iterator()
        while (iter.hasNext) {
          val adminclass = iter.next()
          adminclassCondition.append("instr(litem.content, ',")
            .append(adminclass.id.toString)
            .append(",') > 0")
            .append(" or litem.content ='")
            .append(adminclass.id.toString)
            .append("' ")
          if (iter.hasNext) {
            adminclassCondition.append(" or ")
          }
        }
        adminclassCondition.append(")").append(")")
        query.where(adminclassCondition.toString)
      }
    }
    val compare = Params.getInt("electInfo.electCountCompare")
    if (null != compare) {
      var op = ""
      op = if (compare.intValue() == 0) "=" else if (compare.intValue() < 0) "<" else ">"
      query.where("lesson.teachClass.stdCount " + op + " lesson.teachClass.limitCount")
    }
    var isExamArrangeComplete = Params.getInt("isExamArrangeComplete")
    if (isExamArrangeComplete == null) {
      isExamArrangeComplete = 3
    }
    val examTypeId = Params.getInt("examType.id")
    if (examTypeId != null && examTypeId == ExamType.DELAY) query.where("1=0")
    if (examTypeId != null && examTypeId == ExamType.MAKEUP) {
      var semesterId = Params.getInt("lesson.semester.id")
      if (semesterId == null) semesterId = Params.getInt("semesterId")
      query.where("exists (from org.openurp.edu.teach.exam.ExamTake examTake where examTake.examType.id in (:examTypeIds)" + 
        " and examTake.lesson.project.id = :projectId and examTake.semester.id = :semesterId and examTake.lesson = lesson)", 
        Array(ExamType.MAKEUP, ExamType.DELAY), Params.getInt("lesson.project.id"), semesterId)
    }
    if (isExamArrangeComplete == 1) {
      var activitySubQuery = "exists(from org.openurp.edu.teach.exam.ExamActivity " + 
        "examActivity left join examActivity.examRooms examRoom where examActivity.lesson=lesson and " + 
        "examActivity.examType.id=:examTypeId"
      val activityParams = CollectUtils.newArrayList()
      activityParams.add(examTypeId)
      val examState = Params.get("exam.state")
      if (Strings.isNotEmpty(examState)) {
        activitySubQuery += " and examActivity.state =:state"
        activityParams.add(ExamAuditState.valueOf(examState))
      }
      val roomIsNull = Params.getBoolean("roomIsNull")
      if (null != roomIsNull) {
        if (roomIsNull) {
          activitySubQuery += " and size(examActivity.examRooms) = 0"
        } else {
          activitySubQuery += " and size(examActivity.examRooms) > 0"
        }
      }
      val examRoom = Params.get("exam.room.name")
      if (Strings.isNotEmpty(examRoom)) {
        activitySubQuery += " and examRoom.room.name like :roomName"
        activityParams.add("%" + examRoom + "%")
      }
      val departId = Params.get("exam.examiner.department.id")
      if (Strings.isNotEmpty(departId)) {
        activitySubQuery += " and examRoom.examiner.department.id = :departId"
        activityParams.add(java.lang.Long.valueOf(departId))
      }
      val examStartTime = Params.get("exam.startTime")
      if (Strings.isNotBlank(examStartTime)) {
        val startTime = Timestamp.valueOf(examStartTime + ":00")
        activitySubQuery += " and examActivity.startAt >= :examStartTime"
        activityParams.add(startTime)
      }
      val examEndTime = Params.get("exam.endTime")
      if (Strings.isNotBlank(examEndTime)) {
        val endTime = Timestamp.valueOf(examEndTime + ":00")
        activitySubQuery += " and examActivity.endAt <= :examEndTime"
        activityParams.add(endTime)
      }
      val fromWeek = Params.getInt("fromWeek")
      if (fromWeek != null) {
        val semester = entityDao.get(classOf[Semester], Params.getInt("lesson.semester.id"))
        val dates = SemesterUtil.getWeekTime(semester, fromWeek)
        activitySubQuery += " and examActivity.startAt >= :examStartTime1 and examActivity.endAt <= :examEndTime1"
        activityParams.add(dates(0))
        activityParams.add(dates(1))
      }
      activitySubQuery += ")"
      val activityCondition = new Condition(activitySubQuery)
      activityCondition.params(activityParams)
      query.where(activityCondition)
    } else if (isExamArrangeComplete == 0) {
      query.where("not exists (from org.openurp.edu.teach.exam.ExamActivity exam " + 
        "where exam.lesson=lesson and exam.examType.id=:examTypeId)", examTypeId)
    }
    val queryGrouped = Params.getBoolean("arrangeInfo.examGrouped")
    if (null == queryGrouped) {
      val groupId = Params.getLong("examGroup.id")
      if (groupId != null) {
        query.where("not exists (select 1 from org.openurp.edu.eams.teach.exam.ExamGroup examGroup " + 
          "where lesson in elements(examGroup.lessons) and examGroup.examType.id =:groupExamTypeId) ", 
          examTypeId)
      }
      val mygroupId = Params.getLong("myExamGroup.id")
      if (mygroupId != null) {
        query.where("exists (select 1 from org.openurp.edu.eams.teach.exam.ExamGroup examGroup " + 
          "where lesson in elements(examGroup.lessons) and examGroup.id =:mygroupId) ", mygroupId)
      }
    } else {
      var hql = "exists (from lesson.courseSchedule.examGroups examGroup where examGroup.id = :examGroupId and exists (from examGroup.lessons lesson1 where lesson1 = lesson))"
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
        query.orderBy("(lesson.course.period / (lesson.courseSchedule.endWeek + 1 - lesson.courseSchedule.startWeek)) asc")
      } else if ("fake.weekHour desc" == Params.get("orderBy")) {
        query.orderBy("(lesson.course.period / (lesson.courseSchedule.endWeek + 1 - lesson.courseSchedule.startWeek)) desc")
      } else if ("lesson.courseSchedule.weeks asc" == Params.get("orderBy")) {
        query.orderBy("(lesson.courseSchedule.endWeek - lesson.courseSchedule.startWeek + 1) asc")
      } else if ("lesson.courseSchedule.weeks desc" == Params.get("orderBy")) {
        query.orderBy("(lesson.courseSchedule.endWeek - lesson.courseSchedule.startWeek + 1) desc")
      } else if ("lesson.fake.auditStatus asc" == Params.get("orderBy")) {
        query.orderBy("str(lesson.auditStatus) asc")
      } else if ("lesson.fake.auditStatus desc" == Params.get("orderBy")) {
        query.orderBy("str(lesson.auditStatus) desc")
      } else {
        query.orderBy(Order.parse(Params.get("orderBy")))
      }
    }
    val isPreScheduled = Params.getBoolean("fake.arrangeSuggest.status")
    if (isPreScheduled != null) {
      if (isPreScheduled) {
        query.where("exists(from " + classOf[ArrangeSuggest].getName + " suggest where suggest.lesson = lesson)")
      } else {
        query.where("not exists(from " + classOf[ArrangeSuggest].getName + 
          " suggest where suggest.lesson = lesson)")
      }
    }
    val limitItemConditions = CollectUtils.newArrayList()
    val educationId = Params.getInt("limitGroup.education.id")
    if (null != educationId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.EDUCATION, educationId))
    }
    val stdTypeId = Params.getInt("limitGroup.stdType.id")
    if (null != stdTypeId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.STDTYPE, stdTypeId))
    }
    val departId = Params.getInt("limitGroup.depart.id")
    if (null != departId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.DEPARTMENT, departId))
    }
    val majorId = Params.getInt("limitGroup.major.id")
    if (null != majorId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.MAJOR, majorId))
    }
    val directionId = Params.getInt("limitGroup.direction.id")
    if (null != directionId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.DIRECTION, directionId))
    }
    val genderId = Params.getInt("limitGroup.gender.id")
    if (null != genderId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.GENDER, genderId))
    }
    val programId = Params.getInt("limitGroup.program.id")
    if (null != programId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.PROGRAM, programId))
    }
    val stdLabelId = Params.getInt("limitGroup.stdLabel.id")
    if (null != stdLabelId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.STDLABEL, stdLabelId))
    }
    val normalClassId = Params.getInt("limitGroup.normalClass.id")
    if (null != normalClassId) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.NORMALCLASS, normalClassId))
    }
    val grade = Params.get("limitGroup.grade")
    if (StringUtils.isNotBlank(grade)) {
      limitItemConditions.add(new Pair[CourseLimitMetaEnum, Any](CourseLimitMetaEnum.GRADE, grade))
    }
    if (CollectUtils.isNotEmpty(limitItemConditions)) {
      query.where(limitGroupCondition(limitItemConditions))
    }
    query
  }

  def searchLesson(): List[Lesson] = entityDao.search(buildQuery())

  private def limitGroupCondition(limitItemConditions: List[Pair[CourseLimitMetaEnum, Any]]): String = {
    val condition = new StringBuilder()
    condition.append("exists (").append("select lgroup.id from lesson.teachClass.limitGroups lgroup where ")
    for (i <- 0 until limitItemConditions.size) {
      val limitItemCondition = limitItemConditions.get(i)
      val field = limitItemCondition._1
      val value = limitItemCondition._2
      condition.append(" exists ( from lgroup.items litem where")
        .append(" litem.meta.id=")
        .append(field.getMetaId)
        .append(" and ")
        .append(" litem.operator in ('IN', 'EQUAL')")
        .append(" and ")
        .append(" instr(',' || litem.content || ',', ',")
        .append(value.toString)
        .append(",') > 0")
        .append(")")
      if (i < limitItemConditions.size - 1) {
        condition.append(" and ")
      }
    }
    condition.append(")")
    condition.toString
  }
}
