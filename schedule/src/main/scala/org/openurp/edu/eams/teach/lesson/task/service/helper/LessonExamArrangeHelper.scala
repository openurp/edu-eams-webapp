package org.openurp.edu.eams.teach.lesson.task.service.helper

import java.text.SimpleDateFormat
import java.util.Date
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Semester
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.base.code.ExamStatus
import org.openurp.edu.teach.exam.model.ExamTakeBean
import org.openurp.edu.teach.code.ExamType
import org.openurp.edu.teach.exam.model.ExamActivityBean
import org.beangle.data.model.dao.EntityDao



class LessonExamArrangeHelper extends BaseServiceImpl {

  var entityDao : EntityDao
  def buildExamActivity(lesson: Lesson, 
      examTypeId: java.lang.Integer, 
      weeks: Int, 
      weekDay: Int, 
      turnId: java.lang.Long) {
//    val examTurn = entityDao.get(classOf[ExamTurn], turnId)
    val semester = entityDao.get(classOf[Semester], lesson.semester.id)
//    val beginTimes = ExamYearWeekTimeUtil.convertTime(examTurn.getBeginTime)
//    val endTimes = ExamYearWeekTimeUtil.convertTime(examTurn.end)
    val beginAt = ExamYearWeekTimeUtil.getDate(semester, weeks, weekDay, beginTimes)
    val endAt = ExamYearWeekTimeUtil.getDate(semester, weeks, weekDay, endTimes)
    val examType = Model.newInstance(classOf[ExamType], examTypeId)
//    var activity = lesson.getExamSchedule.getActivity(examType)
    var activity = lesson.schedule.activities
    if (activity != null && activity.state.isTimePublished) {
      return
    }
    if (activity == null) {
      activity = new ExamActivityBean
      activity.examType = examType
      activity.lesson = lesson
      activity.semester = semester
    }
    activity.startAt = beginAt
    activity.endAt = endAt
    entityDao.saveOrUpdate(activity)
  }

  def buildExamTake(courseTake: CourseTake, activity: ExamActivity, examStatus: ExamStatus): ExamTake = {
    val examTake = new ExamTakeBean
    examTake.examStatus = examStatus
    examTake.examType = activity.examType
    examTake.lesson = courseTake.lesson
//    examTake.semester = courseTake.getLesson.getSemester
    examTake.std = courseTake.std
    examTake
  }

//  def getExamTurnByActivity(activity: ExamActivity): ExamTurn = {
//    val sdf = new SimpleDateFormat("HHmm")
//    val query = OqlBuilder.from(classOf[ExamTurn], "examTurn")
//    query.where("examTurn.beginTime =:beginTime", java.lang.Integer.valueOf(sdf.format(activity.getStartAt)))
//    query.where("examTurn.endTime =:endTime", java.lang.Integer.valueOf(sdf.format(activity.getEndAt)))
//    val examTurns = entityDao.search(query)
//    if (Collections.isNotEmpty(examTurns)) examTurns.get(0) else null
//  }

  def getExamActivityByLesson(lesson: Lesson): ExamActivity = {
    val query = OqlBuilder.from(classOf[ExamActivity], "activity")
    query.where("activity.examType.id =:examTypeId", ExamType.Final)
    query.where("activity.lesson =:lesson", lesson)
    val activities = entityDao.search(query)
    if (Collections.isNotEmpty(activities)) activities(0) else null
  }
}
