package org.openurp.edu.eams.teach.lesson.service




trait LessonFilterStrategyFactory {

  def getLessonFilterCategory(strategyName: String): LessonFilterStrategy
}
