package org.openurp.edu.eams.teach.lesson.service

import org.hibernate.Query
import org.hibernate.Session
import LessonFilterStrategy._

import scala.collection.JavaConversions._

object LessonFilterStrategy {

  val ADMINCLASS = "adminclass"

  val COURSE_TYPE = "courseType"

  val DIRECTION = "direction"

  val MAJOR = "major"

  val STD = "std"

  val STD_TYPE = "stdType"

  val TEACH_DEPART = "teachDepart"

  val TEACHER = "teacher"

  val TEACHCLASS_DEPART = "depart"
}

trait LessonFilterStrategy {

  def getName(): String

  def getPostfix(): String

  def getPrefix(): String

  def getFilterString(): String

  def createQuery(session: Session): Query

  def getQueryString(): String

  def createQuery(session: Session, prefix: String, postfix: String): Query

  def getQueryString(prefix: String, postfix: String): String
}
