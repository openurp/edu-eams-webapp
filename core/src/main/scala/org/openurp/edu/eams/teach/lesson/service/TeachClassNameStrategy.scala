package org.openurp.edu.eams.teach.lesson.service

import java.util.List
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.TeachClass

import scala.collection.JavaConversions._

trait TeachClassNameStrategy {

  def genName(groups: List[CourseLimitGroup]): String

  def genName(teachClass: TeachClass): String

  def genName(fullname: String): String

  def abbreviateName(teachClass: TeachClass): Unit

  def genFullname(groups: List[CourseLimitGroup]): String

  def genFullname(teachClass: TeachClass): String

  def autoName(teachClass: TeachClass): Unit
}
