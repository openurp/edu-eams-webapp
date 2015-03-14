package org.openurp.edu.eams.teach.lesson.service


import scala.collection.JavaConversions._

trait LessonFilterStrategyFactory {

  def getLessonFilterCategory(strategyName: String): LessonFilterStrategy
}
