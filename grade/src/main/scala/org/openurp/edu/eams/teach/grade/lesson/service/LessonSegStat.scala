package org.openurp.edu.eams.teach.grade.lesson.service


import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.Lesson



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
