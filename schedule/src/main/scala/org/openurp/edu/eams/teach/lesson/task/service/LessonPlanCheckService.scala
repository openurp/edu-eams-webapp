package org.openurp.edu.eams.teach.lesson.task.service

import java.util.List
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.task.biz.PlanPackage

import scala.collection.JavaConversions._

trait LessonPlanCheckService {

  def makePackages(relations: List[LessonPlanRelation]): List[PlanPackage]
}
