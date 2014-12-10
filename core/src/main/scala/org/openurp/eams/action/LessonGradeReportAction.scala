package org.openurp.eams.action

import scala.collection.mutable.ListBuffer
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.{ AbstractEntityAction, RestfulAction }
import org.openurp.teach.code.GradeType
import org.openurp.teach.core.Student
import org.openurp.teach.grade.CourseGrade
import org.openurp.teach.lesson.Lesson

class LessonGradeReportAction extends AbstractEntityAction {
  def index(): String = {
    val lessonNo = "1958"
    val lesson = entityDao.findBy(classOf[Lesson], "no", List(lessonNo))
    val grades = entityDao.findBy(classOf[CourseGrade], "lessonNo", List(lessonNo))
    val gradeTypes = entityDao.findBy(classOf[GradeType], "code", List("0003", "0002", "0007"))
    put("lessonGradeReports", List(LessonReport(lesson.head, grades, gradeTypes)))
    val lglist = new ListBuffer[LevelGrade]()
    lglist.append(new LevelGrade("优", 90, 100))
    lglist.append(new LevelGrade("良", 80, 89.9F))
    lglist.append(new LevelGrade("中", 70, 79.9F))
    lglist.append(new LevelGrade("及格", 60, 69.9F))
    lglist.append(new LevelGrade("不及格", 0, 59.9F))

    val gradeType = gradeTypes.find(gt => { "0007".equals(gt.code) }).get

    grades.foreach(grade => {
      lglist.foreach(lg => {
        val score = grade.getGrade(gradeType).score
        if (lg.min <= score && score <= lg.max) {
          lg.count = lg.count + 1
        }
      })
    })
    put("lglist", lglist)
    forward("index_" + ContextHolder.context.locale.getLanguage)
  }

  def statTask(): String = {
    val lessonNo = "1958"
    val lesson = entityDao.findBy(classOf[Lesson], "no", List(lessonNo)).head
    val grades = entityDao.findBy(classOf[CourseGrade], "lessonNo", List(lessonNo))
    val gradeTypes = entityDao.findBy(classOf[GradeType], "code", List("0002", "0007"))

    val courseStat = new GradeSegStats(0)
    courseStat.courseGrades ++= grades
    courseStat.lesson = lesson
    courseStat.scoreSegments.append(new FloatSegment(90, 100))
    courseStat.scoreSegments.append(new FloatSegment(80, 89))
    courseStat.scoreSegments.append(new FloatSegment(70, 79))
    courseStat.scoreSegments.append(new FloatSegment(60, 69))
    courseStat.scoreSegments.append(new FloatSegment(50, 59))
    courseStat.scoreSegments.append(new FloatSegment(0, 49))

    courseStat.stat(gradeTypes)
    put("courseStats", List(courseStat))
    forward()
  }

  def reportForExam(): String = {
    val lessonNo = "1958"
    val lesson = entityDao.findBy(classOf[Lesson], "no", List(lessonNo)).head
    val grades = entityDao.findBy(classOf[CourseGrade], "lessonNo", List(lessonNo))
    val gradeTypes = entityDao.findBy(classOf[GradeType], "code", List("0002"))

    val courseStat = new GradeSegStats(0)
    courseStat.courseGrades ++= grades
    courseStat.lesson = lesson
    courseStat.scoreSegments.append(new FloatSegment(90, 100))
    courseStat.scoreSegments.append(new FloatSegment(80, 89))
    courseStat.scoreSegments.append(new FloatSegment(70, 79))
    courseStat.scoreSegments.append(new FloatSegment(60, 69))
    courseStat.scoreSegments.append(new FloatSegment(50, 59))
    courseStat.scoreSegments.append(new FloatSegment(0, 49))

    courseStat.stat(gradeTypes)
    put("courseStats", List(courseStat))
    forward()
  }

}
//case class CourseStat(lesson: Lesson, grades:Seq[CourseGrade],gradeType: Seq[GradeType],scoreSegments: ListBuffer[FloatSegment], gradeSegStat: ListBuffer[GradeSegStat]) {
//	
//	
//}

case class LessonReport(lesson: Lesson, grades: Seq[CourseGrade], gradeTypes: Seq[GradeType]) {

}

class LevelGrade(val name: String, val min: Float, val max: Float) {
  var count: Int = _
}
 