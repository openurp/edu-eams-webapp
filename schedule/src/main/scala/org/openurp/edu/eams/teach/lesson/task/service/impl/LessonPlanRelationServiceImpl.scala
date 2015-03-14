package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.util.List
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.dao.LessonPlanRelationDao
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.teach.plan.MajorPlan

import scala.collection.JavaConversions._

class LessonPlanRelationServiceImpl extends BaseServiceImpl with LessonPlanRelationService {

  private var lessonPlanRelationDao: LessonPlanRelationDao = _

  def relatedLessons(plan: MajorPlan): List[Lesson] = {
    lessonPlanRelationDao.relatedLessons(plan)
  }

  def relatedLessons(plan: MajorPlan, semester: Semester): List[Lesson] = {
    lessonPlanRelationDao.relatedLessons(plan, semester)
  }

  def relatedPlans(lesson: Lesson): List[MajorPlan] = {
    lessonPlanRelationDao.relatedPlans(lesson)
  }

  def relations(plan: MajorPlan): List[LessonPlanRelation] = lessonPlanRelationDao.relations(plan)

  def relations(plan: MajorPlan, semester: Semester): List[LessonPlanRelation] = {
    lessonPlanRelationDao.relations(plan, semester)
  }

  def relations(lesson: Lesson): List[LessonPlanRelation] = lessonPlanRelationDao.relations(lesson)

  def setLessonPlanRelationDao(lessonPlanRelationDao: LessonPlanRelationDao) {
    this.lessonPlanRelationDao = lessonPlanRelationDao
  }

  def possibleRelatePlans(lesson: Lesson): List[MajorPlan] = {
    lessonPlanRelationDao.possibleRelatePlans(lesson)
  }
}
