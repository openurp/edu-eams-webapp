package org.openurp.edu.eams.teach.lesson.service

import org.beangle.data.model.Entity
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta.LimitMeta



trait LessonLimitGroupBuilder {

  def inGrades(grades: String*): LessonLimitGroupBuilder

  def notInGrades(grades: String*): LessonLimitGroupBuilder

  def in[T <: Entity[_]](entities: T*): LessonLimitGroupBuilder

  def notIn[T <: Entity[_]](entities: T*): LessonLimitGroupBuilder

  def clear(meta: LimitMeta): LessonLimitGroupBuilder

  def build(): LessonLimitGroup
}
