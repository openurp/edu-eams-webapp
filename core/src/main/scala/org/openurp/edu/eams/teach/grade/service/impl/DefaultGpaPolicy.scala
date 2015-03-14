package org.openurp.edu.eams.teach.grade.service.impl

import java.util.List
import org.openurp.edu.teach.grade.CourseGrade
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class DefaultGpaPolicy extends GpaPolicy {

  @BeanProperty
  val precision = 2

  def calcGa(grades: List[CourseGrade]): java.lang.Float = {
    var credits = 0
    var creditGas = 0
    for (grade <- grades) {
      var score = grade.getScore
      if (null == score) {
        score = 0f
        if (grade.isPassed) //continue
      }
      val credit = grade.getCourse.getCredits
      credits += credit
      creditGas += credit * score
    }
    round(if ((credits == 0)) null else new java.lang.Float(creditGas / credits))
  }

  def calcGpa(grades: List[CourseGrade]): java.lang.Float = {
    var credits = 0
    var creditGps = 0
    for (grade <- grades if null != grade.getGp) {
      val credit = grade.getCourse.getCredits
      credits += credit
      creditGps += credit * (grade.getGp.doubleValue())
    }
    round(if ((credits == 0)) null else new java.lang.Float(creditGps / credits))
  }

  def round(score: java.lang.Float): java.lang.Float = {
    if (null == score) return null
    val mutilply = Math.pow(10, precision).toInt
    score *= mutilply
    if (score % 1 >= 0.5) {
      score += 1
    }
    score -= score % 1
    (new java.lang.Float(score / mutilply))
  }
}
