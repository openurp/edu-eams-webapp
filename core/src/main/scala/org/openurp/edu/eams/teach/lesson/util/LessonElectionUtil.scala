package org.openurp.edu.eams.teach.lesson.util

import java.util.Collection
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass

import scala.collection.JavaConversions._

object LessonElectionUtil {

  def normalizeTeachClass(lesson: Lesson) {
    lesson.getTeachClass.setLesson(lesson)
    for (take <- lesson.getTeachClass.getCourseTakes) {
      take.setLesson(lesson)
    }
    for (take <- lesson.getTeachClass.getExamTakes) {
      take.setLesson(lesson)
    }
    for (group <- lesson.getTeachClass.getLimitGroups) {
      group.setLesson(lesson)
      for (item <- group.getItems) {
        item.setGroup(group)
      }
    }
  }

  def addCourseTake(teachClass: TeachClass, take: CourseTake) {
    teachClass.getCourseTakes.add(take)
    take.setLesson(teachClass.getLesson)
    teachClass.setStdCount(teachClass.getCourseTakes.size)
  }

  def addCourseTakes(teachClass: TeachClass, takes: Collection[CourseTake]) {
    for (take <- takes) {
      addCourseTake(teachClass, take)
    }
  }
}
