package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.sql.Date


import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
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

  var semesterService: SemesterService = _

  var lessonLimitService: LessonLimitService = _

  var lessonPlanRelationService: LessonPlanRelationService = _

  def makePackages(relations: Seq[LessonPlanRelation]): Seq[PlanPackage] = {
    val packages = Collections.newBuffer[PlanPackage]
    for (relation <- relations) {
      val plan = relation.getPlan
      val lesson = relation.getLesson
      var planPackage = searchPlanPackage(packages, plan)
      if (planPackage == null) {
        planPackage = newPlanPackage(plan, lesson.getSemester)
        packages += planPackage
      }
      if (lesson.isTransient) {
        //continue
      }
      var lessonBelongToAdminclass = false
      for (classPackage <- planPackage.classPackages) {
        if (classPackage.adminclass == null) {
          lessonBelongToAdminclass = true
          addLesson(classPackage, lesson)
        } else {
          val classesInLesson = lessonLimitService.extractAdminclasses(lesson.getTeachClass)
          if (classesInLesson.contains(classPackage.getAdminclass)) {
            lessonBelongToAdminclass = true
            addLesson(classPackage, lesson)
          }
        }
      }
      if (!lessonBelongToAdminclass) {
        addLesson(planPackage.otherClassPackage, lesson)
      }
    }
    packages
  }

  private def addLesson(classPackage: AdminclassPackage, lesson: Lesson) {
    val packages = classPackage.courseTypePackages
    var courseTypePackage = searchCourseTypePackage(packages, lesson.courseType)
    if (courseTypePackage == null) {
      courseTypePackage = new CourseTypePackage()
      courseTypePackage.courseType = lesson.courseType
      classPackage.courseTypePackages += courseTypePackage
    }
    courseTypePackage.lessons += lesson
  }

  private def searchCourseTypePackage(packages: Seq[CourseTypePackage], courseType: CourseType): CourseTypePackage = {
    if (packages.size != 0 && 
      packages(packages.size - 1).courseType == courseType) {
      packages(packages.size - 1)
    }
    for (courseTypePackage <- packages if courseTypePackage.courseType == courseType) {
      courseTypePackage
    }
    null
  }

  private def searchPlanPackage(packages: Seq[PlanPackage], plan: MajorPlan): PlanPackage = {
    if (packages.size != 0 && packages(packages.size - 1).plan == plan) {
      return packages(packages.size - 1)
    }
    for (planPackage <- packages if planPackage.plan == plan) {
      return planPackage
    }
    null
  }

  private def newPlanPackage(plan: MajorPlan, semester: Semester): PlanPackage = {
    val adminclasses = entityDao.search(AdminclassQueryBuilder.build(plan).cacheable())
    val planPackage = new PlanPackage()
    planPackage.plan = plan
    if (Collections.isEmpty(adminclasses)) {
      planPackage.classPackages += new AdminclassPackage
    } else {
      for (adminclass <- adminclasses) {
        planPackage.classPackages += new AdminclassPackage(adminclass)
      }
    }
    val termCalc = new TermCalculator(semesterService, semester)
    var term = -1
    term = if (plan.program.endOn != null) termCalc.getTerm(plan.program.beginOn, 
      plan.program.endOn, true) else termCalc.getTerm(plan.program.beginOn, Date.valueOf("2099-09-09"), 
      true)
    planPackage.term = term
    for (group <- plan.groups) {
      val cgPackage = makeCourseGroupPackage(group, term)
      if (Collections.isEmpty(cgPackage.planCourses) && PlanUtils.getGroupCredits(group, term) == 0f) {
        //continue
      }
      planPackage.groupPackages += cgPackage
    }
    planPackage
  }

  private def makeCourseGroupPackage(group: CourseGroup, term: Int): CourseGroupPackage = {
    val pkg = new CourseGroupPackage()
    pkg.courseGroup = group
    pkg.planCourses ++= PlanUtils.getPlanCourses(group, term)
    pkg.credits = PlanUtils.getGroupCredits(group, term)
    pkg
  }
}
