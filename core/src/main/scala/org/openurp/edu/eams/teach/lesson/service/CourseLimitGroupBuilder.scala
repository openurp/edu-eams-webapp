package org.openurp.edu.eams.teach.lesson.service

import org.beangle.commons.entity.Entity
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitMeta

import scala.collection.JavaConversions._

trait CourseLimitGroupBuilder {

  def inGrades(grades: String*): CourseLimitGroupBuilder

  def notInGrades(grades: String*): CourseLimitGroupBuilder

  def in[T <: Entity[_]](entities: T*): CourseLimitGroupBuilder

  def notIn[T <: Entity[_]](entities: T*): CourseLimitGroupBuilder

  def clear(meta: CourseLimitMeta): CourseLimitGroupBuilder

  def build(): CourseLimitGroup
}
