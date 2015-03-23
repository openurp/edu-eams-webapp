package org.openurp.edu.eams.teach.lesson.service

import org.beangle.data.model.Entity
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitMeta



trait LessonLimitGroupBuilder {

  def inGrades(grades: String*): LessonLimitGroupBuilder

  def notInGrades(grades: String*): LessonLimitGroupBuilder

  def in[T <: Entity[_]](entities: T*): LessonLimitGroupBuilder

  def notIn[T <: Entity[_]](entities: T*): LessonLimitGroupBuilder

  def clear(meta: LessonLimitMeta): LessonLimitGroupBuilder

  def build(): LessonLimitGroup
}
