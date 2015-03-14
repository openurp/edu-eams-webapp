package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import java.util.HashMap
import java.util.Map
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategyFactory

import scala.collection.JavaConversions._

class DefaultLessonFilterStrategyFactory extends LessonFilterStrategyFactory {

  private var lessonFilterStrategies: Map[String, LessonFilterStrategy] = new HashMap[String, LessonFilterStrategy]()

  def getLessonFilterCategory(strategyName: String): LessonFilterStrategy = {
    lessonFilterStrategies.get(strategyName)
  }

  def setLessonFilterStrategies(lessonFilterCategories: Map[String, LessonFilterStrategy]) {
    this.lessonFilterStrategies = lessonFilterCategories
  }
}
