package org.openurp.edu.eams.teach.lesson.service

import org.beangle.data.model.Entity
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitMeta



trait CourseLimitGroupBuilder {

  def inGrades(grades: String*): CourseLimitGroupBuilder

  def notInGrades(grades: String*): CourseLimitGroupBuilder

  def in[T <: Entity[_]](entities: T*): CourseLimitGroupBuilder

  def notIn[T <: Entity[_]](entities: T*): CourseLimitGroupBuilder

  def clear(meta: CourseLimitMeta): CourseLimitGroupBuilder

  def build(): CourseLimitGroup
}
