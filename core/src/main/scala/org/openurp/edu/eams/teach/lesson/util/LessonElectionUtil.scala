package org.openurp.edu.eams.teach.lesson.util

import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass

object LessonElectionUtil {

  def normalizeTeachClass(lesson: Lesson) {
    lesson.teachClass.lesson = lesson
    for (take <- lesson.teachClass.courseTakes) {
      take.lesson = lesson
    }
    for (take <- lesson.teachClass.examTakes) {
      take.lesson = lesson
    }
    for (group <- lesson.teachClass.limitGroups) {
      group.lesson = lesson
      for (item <- group.items) {
        item.group = group
      }
    }
  }

  def addCourseTake(teachClass: TeachClass, take: CourseTake) {
    teachClass.courseTakes += take
    take.lesson = teachClass.lesson
    teachClass.stdCount = teachClass.courseTakes.size
  }

  def addCourseTakes(teachClass: TeachClass, takes: Iterable[CourseTake]) {
    for (take <- takes) {
      addCourseTake(teachClass, take)
    }
  }
}
