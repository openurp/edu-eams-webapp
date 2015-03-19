package org.openurp.edu.eams.teach.lesson.task.service


import org.openurp.base.Semester
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.teach.plan.MajorPlan



trait LessonPlanRelationService {

  def relations(plan: MajorPlan): List[LessonPlanRelation]

  def relations(lesson: Lesson): List[LessonPlanRelation]

  def relations(plan: MajorPlan, semester: Semester): List[LessonPlanRelation]

  def relatedLessons(plan: MajorPlan): List[Lesson]

  def relatedLessons(plan: MajorPlan, semester: Semester): List[Lesson]

  def relatedPlans(lesson: Lesson): List[MajorPlan]

  def possibleRelatePlans(lesson: Lesson): List[MajorPlan]
}
