package org.openurp.edu.eams.teach.lesson.task.service.helper

import java.text.SimpleDateFormat
import java.util.Date

import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Semester
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.exam.ExamTurn
import org.openurp.edu.eams.teach.exam.service.ExamYearWeekTimeUtil
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.ExamActivityBean
import org.openurp.edu.eams.teach.lesson.model.ExamTakeBean



class LessonExamArrangeHelper extends BaseServiceImpl {

  def buildExamActivity(lesson: Lesson, 
      examTypeId: java.lang.Integer, 
      weeks: Int, 
      weekDay: Int, 
      turnId: java.lang.Long) {
    val examTurn = entityDao.get(classOf[ExamTurn], turnId)
    val semester = entityDao.get(classOf[Semester], lesson.getSemester.id)
    val beginTimes = ExamYearWeekTimeUtil.convertTime(examTurn.getBeginTime)
    val endTimes = ExamYearWeekTimeUtil.convertTime(examTurn.end)
    val beginAt = ExamYearWeekTimeUtil.getDate(semester, weeks, weekDay, beginTimes)
    val endAt = ExamYearWeekTimeUtil.getDate(semester, weeks, weekDay, endTimes)
    val examType = Model.newInstance(classOf[ExamType], examTypeId)
    var activity = lesson.getExamSchedule.getActivity(examType)
    if (activity != null && activity.getState.isTimePublished) {
      return
    }
    if (activity == null) {
      activity = new ExamActivityBean()
      activity.setExamType(examType)
      activity.setLesson(lesson)
      activity.setSemester(semester)
    }
    activity.setStartAt(beginAt)
    activity.setEndAt(endAt)
    entityDao.saveOrUpdate(activity)
  }

  def buildExamTake(courseTake: CourseTake, activity: ExamActivity, examStatus: ExamStatus): ExamTake = {
    val examTake = new ExamTakeBean()
    examTake.setExamStatus(examStatus)
    examTake.setExamType(activity.getExamType)
    examTake.setLesson(courseTake.getLesson)
    examTake.setSemester(courseTake.getLesson.getSemester)
    examTake.setStd(courseTake.getStd)
    examTake
  }

  def getExamTurnByActivity(activity: ExamActivity): ExamTurn = {
    val sdf = new SimpleDateFormat("HHmm")
    val query = OqlBuilder.from(classOf[ExamTurn], "examTurn")
    query.where("examTurn.beginTime =:beginTime", java.lang.Integer.valueOf(sdf.format(activity.getStartAt)))
    query.where("examTurn.endTime =:endTime", java.lang.Integer.valueOf(sdf.format(activity.getEndAt)))
    val examTurns = entityDao.search(query)
    if (Collections.isNotEmpty(examTurns)) examTurns.get(0) else null
  }

  def getExamActivityByLesson(lesson: Lesson): ExamActivity = {
    val query = OqlBuilder.from(classOf[ExamActivity], "activity")
    query.where("activity.examType.id =:examTypeId", ExamType.FINAL)
    query.where("activity.lesson =:lesson", lesson)
    val activities = entityDao.search(query)
    if (Collections.isNotEmpty(activities)) activities.get(0) else null
  }
}
