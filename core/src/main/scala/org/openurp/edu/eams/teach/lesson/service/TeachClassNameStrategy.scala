package org.openurp.edu.eams.teach.lesson.service


import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.TeachClass



trait TeachClassNameStrategy {

  def genName(groups: List[LessonLimitGroup]): String

  def genName(teachClass: TeachClass): String

  def genName(fullname: String): String

  def abbreviateName(teachClass: TeachClass): Unit

  def genFullname(groups: List[LessonLimitGroup]): String

  def genFullname(teachClass: TeachClass): String

  def autoName(teachClass: TeachClass): Unit
}
