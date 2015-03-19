package org.openurp.edu.eams.teach.grade.course.service



import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson



trait MakeupStdStrategy {

  def getLessonCondition(gradeTypeId: java.lang.Integer): String

  def getCourseTakes(lesson: Lesson): List[CourseTake]

  def getCourseTakeCounts(lessons: List[Lesson]): Map[Lesson, Number]
}
