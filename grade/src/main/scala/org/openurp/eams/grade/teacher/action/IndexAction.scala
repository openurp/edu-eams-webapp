package org.openurp.eams.grade.teacher.action

import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.teach.grade.CourseGrade
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.teach.lesson.Lesson
import org.beangle.data.model.dao.EntityDao
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.annotation.param
import org.openurp.teach.grade.model.CourseGradeState
import scala.collection.mutable.ListBuffer
import org.openurp.teach.code.GradeType
import org.openurp.base.Teacher
import org.openurp.teach.grade.model.GradeInputSwitch
import org.openurp.teach.core.Project
import org.openurp.base.Semester
import org.openurp.teach.grade.service.GradeInputSwitchService
import scala.collection.mutable.HashSet

class IndexAction extends AbstractTeacherAction {
  /**
   * 查看教师的课程信息
   */
  def index(): String = {
    val teacher = entityDao.get(classOf[Teacher], new Integer(13006))
    val builder = OqlBuilder.from(classOf[Lesson], "ls")
    builder.join("ls.teachers", "t")
    builder.where("t.id=:teacherId", teacher.id)
    val lessons = entityDao.search(builder)
    put("lessons", lessons)
    forward()
  }

  /**
   * 查看单个教学任务所有成绩信息
   */
  @mapping(value = "{id}")
  def info(@param("id") id: String): String = {
    val lesson = entityDao.get(classOf[Lesson], Integer.valueOf(id))
    put("lesson", lesson)

    val query = OqlBuilder.from(classOf[CourseGrade])
    query.where("courseGrade.lesson=:lesson", lesson)
    val grades = entityDao.search(query)
    put("grades", grades)

    //    val query2 = OqlBuilder.from(classOf[CourseGradeState])
    //    query2.where("courseGradeState.lesson=:lesson", lesson)
    //entityDao.search(query2)
    //gradeState.get(0)之后再put
    val gradeState = new CourseGradeState

    //put("gradeState", gradeState)
    val gradeTypes = new ListBuffer[GradeType]
    if (null != gradeState) {
      gradeState.examStates.foreach(es => gradeTypes += es.gradeType)
      gradeState.gaStates.foreach(es => gradeTypes += es.gradeType)
    }
    put("gradeTypes", gradeTypes)
    forward()
  }

  /**
   * 录入单个教学任务成绩
   *
   * @return @
   */
  def inputTask(): String = {
    val lesson = entityDao.get(classOf[Lesson], get("lesson.id",classOf[Integer]).get)
    checkLessonPermission(lesson)
    val gradeInputSwitch = getGradeInputSwitch(lesson)
    put("gradeInputSwitch", gradeInputSwitch)
    put("gradeState", getOrCreateState (lesson))
    val putSomeParams =new HashSet[String]
    putSomeParams.add("MAKEUP")
    putSomeParams.add("EndGa")
    putSomeParams.add("isTeacher")
    put("markStyles", gradeRateService.getMarkStyles(lesson.project))
    buildSomeParams(lesson, putSomeParams.toSet)
    put("DELAY_ID", GradeType.Delay )
    val gaGradeTypes = settings.getSetting(getProject).endGaElements 
    val gaGradeTypeParams = new ListBuffer[GradeType]
    for (gradeType <- gaGradeTypes) {
//      gradeType = entityDao.get(classOf[GradeType], gradeType.id)
      if (gradeInputSwitch.types.contains(gradeType)) gaGradeTypeParams.append(gradeType)
    }
    put("gaGradeTypes", gaGradeTypeParams)
    put("lesson", lesson)
    forward()
  }

}