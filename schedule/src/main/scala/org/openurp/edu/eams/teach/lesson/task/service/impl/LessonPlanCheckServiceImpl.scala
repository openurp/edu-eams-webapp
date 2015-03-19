package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.sql.Date


import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.task.biz.AdminclassPackage
import org.openurp.edu.eams.teach.lesson.task.biz.CourseGroupPackage
import org.openurp.edu.eams.teach.lesson.task.biz.CourseTypePackage
import org.openurp.edu.eams.teach.lesson.task.biz.PlanPackage
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanCheckService
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.teach.util.AdminclassQueryBuilder



class LessonPlanCheckServiceImpl extends BaseServiceImpl with LessonPlanCheckService {

  protected var semesterService: SemesterService = _

  protected var courseLimitService: CourseLimitService = _

  protected var lessonPlanRelationService: LessonPlanRelationService = _

  def makePackages(relations: List[LessonPlanRelation]): List[PlanPackage] = {
    val packages = new ArrayList[PlanPackage]()
    for (relation <- relations) {
      val plan = relation.getPlan
      val lesson = relation.getLesson
      var planPackage = searchPlanPackage(packages, plan)
      if (planPackage == null) {
        planPackage = newPlanPackage(plan, lesson.getSemester)
        packages.add(planPackage)
      }
      if (lesson.isTransient) {
        //continue
      }
      var lessonBelongToAdminclass = false
      for (classPackage <- planPackage.getClassPackages) {
        if (classPackage.getAdminclass == null) {
          lessonBelongToAdminclass = true
          addLesson(classPackage, lesson)
        } else {
          val classesInLesson = courseLimitService.extractAdminclasses(lesson.getTeachClass)
          if (classesInLesson.contains(classPackage.getAdminclass)) {
            lessonBelongToAdminclass = true
            addLesson(classPackage, lesson)
          }
        }
      }
      if (!lessonBelongToAdminclass) {
        addLesson(planPackage.getOtherClassPackage, lesson)
      }
    }
    packages
  }

  private def addLesson(classPackage: AdminclassPackage, lesson: Lesson) {
    val packages = classPackage.getCourseTypePackages
    var courseTypePackage = searchCourseTypePackage(packages, lesson.getCourseType)
    if (courseTypePackage == null) {
      courseTypePackage = new CourseTypePackage()
      courseTypePackage.setCourseType(lesson.getCourseType)
      classPackage.getCourseTypePackages.add(courseTypePackage)
    }
    courseTypePackage.getLessons.add(lesson)
  }

  private def searchCourseTypePackage(packages: List[CourseTypePackage], courseType: CourseType): CourseTypePackage = {
    if (packages.size != 0 && 
      packages.get(packages.size - 1).getCourseType == courseType) {
      return packages.get(packages.size - 1)
    }
    for (courseTypePackage <- packages if courseTypePackage.getCourseType == courseType) {
      return courseTypePackage
    }
    null
  }

  private def searchPlanPackage(packages: List[PlanPackage], plan: MajorPlan): PlanPackage = {
    if (packages.size != 0 && packages.get(packages.size - 1).getPlan == plan) {
      return packages.get(packages.size - 1)
    }
    for (planPackage <- packages if planPackage.getPlan == plan) {
      return planPackage
    }
    null
  }

  private def newPlanPackage(plan: MajorPlan, semester: Semester): PlanPackage = {
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan).cacheable())
    val planPackage = new PlanPackage()
    planPackage.setPlan(plan)
    if (CollectUtils.isEmpty(adminclasses)) {
      planPackage.getClassPackages.add(new AdminclassPackage())
    } else {
      for (adminclass <- adminclasses) {
        planPackage.getClassPackages.add(new AdminclassPackage(adminclass))
      }
    }
    val termCalc = new TermCalculator(semesterService, semester)
    var term = -1
    term = if (plan.getProgram.getInvalidOn != null) termCalc.getTerm(plan.getProgram.getEffectiveOn, 
      plan.getProgram.getInvalidOn, true) else termCalc.getTerm(plan.getProgram.getEffectiveOn, Date.valueOf("2099-09-09"), 
      true)
    planPackage.setTerm(term)
    for (group <- plan.getGroups) {
      val cgPackage = makeCourseGroupPackage(group, term)
      if (CollectUtils.isEmpty(cgPackage.getPlanCourses) && PlanUtils.getGroupCredits(group, term) == 0f) {
        //continue
      }
      planPackage.getGroupPackages.add(cgPackage)
    }
    planPackage
  }

  private def makeCourseGroupPackage(group: CourseGroup, term: Int): CourseGroupPackage = {
    val pkg = new CourseGroupPackage()
    pkg.setCourseGroup(group)
    pkg.getPlanCourses.addAll(PlanUtils.getPlanCourses(group, term))
    pkg.setCredits(PlanUtils.getGroupCredits(group, term))
    pkg
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }
}
