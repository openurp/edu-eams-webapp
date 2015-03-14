package org.openurp.edu.eams.teach.grade.lesson.service

import java.util.List
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class LessonSegStat(var lesson: Lesson, var teacher: Teacher, courseGrades: List[CourseGrade])
    extends GradeSegStats {

  this.courseGrades = courseGrades

  def this(segs: Int) {
    super(segs)
  }

  def getLesson(): Lesson = lesson

  def setLesson(lesson: Lesson) {
    this.lesson = lesson
  }

  def getTeacher(): Teacher = teacher

  def setTeacher(teacher: Teacher) {
    this.teacher = teacher
  }
}
