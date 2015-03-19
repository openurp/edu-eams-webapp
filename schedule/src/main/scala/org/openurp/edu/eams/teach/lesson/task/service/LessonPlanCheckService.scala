package org.openurp.edu.eams.teach.lesson.task.service


import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.task.biz.PlanPackage



trait LessonPlanCheckService {

  def makePackages(relations: List[LessonPlanRelation]): List[PlanPackage]
}
