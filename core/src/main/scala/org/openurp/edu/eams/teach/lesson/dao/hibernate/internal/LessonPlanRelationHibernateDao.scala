package org.openurp.edu.eams.teach.lesson.dao.hibernate.internal


import java.util.Date

import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.dao.LessonPlanRelationDao
import org.openurp.edu.eams.teach.lesson.model.LessonPlanRelationBean
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.base.Program
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.util.MajorPlanQueryBuilder



class LessonPlanRelationHibernateDao extends HibernateEntityDao with LessonPlanRelationDao {

  private var courseLimitService: CourseLimitService = _

  def relatedLessons(plan: MajorPlan): List[Lesson] = {
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.select("relation.lesson").where("relation.plan = :plan", plan)
    search(query)
  }

  def relatedLessons(plan: MajorPlan, semester: Semester): List[Lesson] = {
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.select("relation.lesson").where("relation.lesson.semester = :semester", semester)
      .where("relation.plan = :plan", plan)
    search(query)
  }

  def relatedPlans(lesson: Lesson): List[MajorPlan] = {
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.select("relation.plan").where("relation.lesson = :lesson", lesson)
    search(query)
  }

  def possibleRelatePlans(lesson: Lesson): List[MajorPlan] = {
    val plans = new ArrayList[MajorPlan]()
    val programs = courseLimitService.extractPrograms(lesson.teachClass)
    if (CollectUtils.isNotEmpty(programs)) {
      plans.addAll(get(classOf[MajorPlan], "program", programs))
    }
    val adminclasses = courseLimitService.extractAdminclasses(lesson.teachClass)
    if (CollectUtils.isNotEmpty(adminclasses)) {
      for (adminclass <- adminclasses) {
        plans.addAll(search(MajorPlanQueryBuilder.build(adminclass)))
      }
      return plans
    }
    val grade = courseLimitService.extractGrade(lesson.teachClass)
    if (Strings.isEmpty(grade)) {
      return CollectUtils.newArrayList()
    }
    val majors = courseLimitService.extractMajors(lesson.teachClass)
    if (CollectUtils.isEmpty(majors)) {
      return CollectUtils.newArrayList()
    }
    val stdTypes = courseLimitService.extractStdTypes(lesson.teachClass)
    if (CollectUtils.isEmpty(stdTypes)) {
      stdTypes.add(null)
    }
    val directions = courseLimitService.extractDirections(lesson.teachClass)
    if (CollectUtils.isEmpty(directions)) {
      directions.add(null)
    }
    for (major <- majors; stdType <- stdTypes; direction <- directions) {
      plans.addAll(search(MajorPlanQueryBuilder.build(grade, stdType, major, direction)))
    }
    plans
  }

  def relations(lesson: Lesson): List[LessonPlanRelation] = {
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.where("relation.lesson = :lesson", lesson)
    search(query)
  }

  def relations(plan: MajorPlan): List[LessonPlanRelation] = {
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.where("relation.plan = :plan", plan)
    search(query)
  }

  def relations(plan: MajorPlan, semester: Semester): List[LessonPlanRelation] = {
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.where("relation.plan = :plan", plan).where("relation.lesson.semester = :semester", semester)
    search(query)
  }

  def saveRelation(plan: MajorPlan, lesson: Lesson) {
    val relation = new LessonPlanRelationBean()
    relation.plan=plan
    relation.lesson=lesson
    relation.createdAt=new Date()
    relation.updatedAt=new Date()
    save(relation)
  }

  def removeRelation(lesson: Lesson) {
    val relations = relations(lesson)
    if (CollectUtils.isNotEmpty(relations)) {
      remove(relations)
    }
  }

  def removeRelation(plan: MajorPlan, semester: Semester) {
    remove(relations(plan, semester))
  }

  def updateRelation(lesson: Lesson) {
    removeRelation(lesson)
    val plans = possibleRelatePlans(lesson)
    if (CollectUtils.isNotEmpty(plans)) {
      for (plan <- plans) {
        saveRelation(plan, lesson)
      }
    } else {
      saveRelation(null, lesson)
    }
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }
}
