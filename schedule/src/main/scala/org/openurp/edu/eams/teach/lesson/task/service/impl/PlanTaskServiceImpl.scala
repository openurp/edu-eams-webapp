package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.util.ArrayList
import java.util.Collection
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.bean.transformers.PropertyTransformer
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.lesson.task.model.PlanTask
import org.openurp.edu.eams.teach.lesson.task.service.PlanTaskService
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator

import scala.collection.JavaConversions._

class PlanTaskServiceImpl extends BaseServiceImpl with PlanTaskService {

  var semesterService: SemesterService = _

  def checkIsAppropriateClose(planCourseList: List[PlanCourse], planTaskList: List[PlanTask], semester: Semester): List[Array[Any]] = {
    val res = findAllRelatedRequestClosePlanCourse(planCourseList, planTaskList, semester)
    res.addAll(planCourseList)
    checkNotAppropriateClosedNastyWork(res, semester)
  }

  def checkIsAppropriateOpen(planCourseList: List[PlanCourse], planTaskList: List[PlanTask], semester: Semester): List[Array[Any]] = {
    val requestedOpenPlanCourses = new ArrayList[PlanCourse](planCourseList)
    val termCalc = new TermCalculator(semesterService, semester)
    for (planTask <- planTaskList) {
      var term = 0
      term = termCalc.getTerm(planTask.majorPlan.getProgram.getEffectiveOn, true)
      val planCourseQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      planCourseQuery.select("select planCourse")
      planCourseQuery.join("plan.courseGroups", "courseGroup")
      planCourseQuery.join("courseGroup.planCourses", "planCourse")
      planCourseQuery.where("plan=:plan", planTask.majorPlan)
      planCourseQuery.where("planCourse.course=:course", planTask.getCourse)
      val termCondition = "(instr(planCourse.terms, '" + term + "') <> 0 and length(planCourse.terms)=length('" + 
        term + 
        "'))" + 
        "or (instr(planCourse.terms, '," + 
        term + 
        ",') <> 0 and length(planCourse.terms)<>length('" + 
        term + 
        "'))"
      planCourseQuery.where(termCondition)
      requestedOpenPlanCourses.addAll(entityDao.search(planCourseQuery))
    }
    checkNotAppropriateOpenNastyWork(findAllRelatedRequestClosePlanCourse(planCourseList, planTaskList, 
      semester), requestedOpenPlanCourses, semester)
  }

  def extractInappropriateTeachPlan(majorPlans: Collection[MajorPlan], semester: Semester): Map[MajorPlan, Map[CourseGroup, Array[Double]]] = {
    val result = new HashMap[MajorPlan, Map[CourseGroup, Array[Double]]]()
    val termCalc = new TermCalculator(semesterService, semester)
    for (plan <- majorPlans) {
      val term = termCalc.getTerm(plan.getProgram.getEffectiveOn, true)
      val singletonTermList = Collections.singletonList(term)
      for (group <- plan.getGroups) {
        val requiredCreditsOnTerm = PlanUtils.getGroupCredits(group, term).toDouble
        val planCourses = PlanUtils.getPlanCourses(group, term)
        if (planCourses.isEmpty) {
          //continue
        }
        val courses = CollectUtils.collect(planCourses, new PropertyTransformer("course"))
        val closedCreditsQry = OqlBuilder.from(classOf[PlanTask], "sq")
        closedCreditsQry.select("select nvl(sum(sq.course.credits),0)")
        closedCreditsQry.where("sq.teachPlan=:plan", plan)
        closedCreditsQry.where("sq.semester=:semester", semester)
        closedCreditsQry.where("sq.flag=:flag", PlanTask.REQ_CLOSE)
        closedCreditsQry.where("sq.course in (:courses)", courses)
        val closedCreditsOnTerm = entityDao.search(closedCreditsQry).get(0).asInstanceOf[java.lang.Double]
        val totalCreditsQry = OqlBuilder.from(classOf[CourseGroup], "courseGroup")
        totalCreditsQry.select("select nvl(sum(planCourse.course.credits),0)")
        totalCreditsQry.join("courseGroup.planCourses", "planCourse")
        totalCreditsQry.where("planCourse.course.required=:required", true)
        totalCreditsQry.where("courseGroup=:courseGroup", group)
        totalCreditsQry.where("(instr(planCourse.terms, '" + term + "') <> 0 and length(planCourse.terms)=length('" + 
          term + 
          "'))" + 
          "or (instr(planCourse.terms, '," + 
          term + 
          ",') <> 0 and length(planCourse.terms)<>length('" + 
          term + 
          "'))")
        val totalCreditsOnTerm = entityDao.search(totalCreditsQry).get(0).asInstanceOf[java.lang.Double]
        if (totalCreditsOnTerm - closedCreditsOnTerm != requiredCreditsOnTerm) {
          if (result.get(plan) == null) {
            result.put(plan, new HashMap[CourseGroup, Array[Double]]())
          }
          result.get(plan).put(group, Array(requiredCreditsOnTerm, totalCreditsOnTerm - closedCreditsOnTerm))
        }
      }
    }
    result
  }

  private def findAllRelatedRequestClosePlanCourse(planCourseList: List[PlanCourse], planTaskList: List[PlanTask], semester: Semester): List[PlanCourse] = {
    val requestedPlanCourses = new ArrayList[PlanCourse]()
    val termCalc = new TermCalculator(semesterService, semester)
    val allPlanTask = new HashSet[PlanTask](planTaskList)
    val usedTeachPlanId = new HashSet[Long]()
    for (i <- 0 until planTaskList.size) {
      if (usedTeachPlanId.contains(planTaskList.get(i).majorPlan.getId)) {
        //continue
      }
      usedTeachPlanId.add(planTaskList.get(i).majorPlan.getId)
      val planTaskQuery = OqlBuilder.from(classOf[PlanTask], "planTask")
      planTaskQuery.where("planTask.teachPlan=:plan", planTaskList.get(i).majorPlan)
      planTaskQuery.where("planTask.flag=:flag", PlanTask.REQ_CLOSE)
      allPlanTask.addAll(entityDao.search(planTaskQuery))
    }
    for (i <- 0 until planCourseList.size) {
      val planCourse = planCourseList.get(i)
      val teachPlanQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      teachPlanQuery.join("plan.courseGroups", "courseGroup")
      teachPlanQuery.join("courseGroup.planCourses", "planCourse")
      teachPlanQuery.where("planCourse=:planCourse", planCourse)
      val planTaskQuery = OqlBuilder.from(classOf[PlanTask], "planTask")
      planTaskQuery.where("planTask.teachPlan in (:plans)", entityDao.search(teachPlanQuery))
      planTaskQuery.where("planTask.flag=:flag", PlanTask.REQ_CLOSE)
      allPlanTask.addAll(entityDao.search(planTaskQuery))
    }
    for (planTask <- allPlanTask) {
      var term = 0
      term = termCalc.getTerm(planTask.majorPlan.getProgram.getEffectiveOn, true)
      val planCourseQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      planCourseQuery.select("select planCourse")
      planCourseQuery.join("plan.courseGroups", "courseGroup")
      planCourseQuery.join("courseGroup.planCourses", "planCourse")
      planCourseQuery.where("plan=:plan", planTask.majorPlan)
      planCourseQuery.where("planCourse.course=:course", planTask.getCourse)
      val termCondition = "(instr(planCourse.terms, '" + term + "') <> 0 and length(planCourse.terms)=length('" + 
        term + 
        "'))" + 
        "or (instr(planCourse.terms, '," + 
        term + 
        ",') <> 0 and length(planCourse.terms)<>length('" + 
        term + 
        "'))"
      planCourseQuery.where(termCondition)
      requestedPlanCourses.addAll(entityDao.search(planCourseQuery))
    }
    requestedPlanCourses
  }

  private def checkNotAppropriateClosedNastyWork(planCourses: List[PlanCourse], semester: Semester): List[Array[Any]] = {
    val result = new ArrayList[Array[Any]]()
    val termCalc = new TermCalculator(semesterService, semester)
    val requestedMap = new HashMap[MajorPlan, Map[CourseGroup, List[PlanCourse]]]()
    val originalMap = new HashMap[MajorPlan, Map[CourseGroup, List[PlanCourse]]]()
    for (i <- 0 until planCourses.size) {
      val planCourse = planCourses.get(i)
      val requestedMapQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      requestedMapQuery.select("plan, courseGroup")
      requestedMapQuery.join("plan.courseGroups", "courseGroup")
      requestedMapQuery.join("courseGroup.planCourses", "planCourse")
      requestedMapQuery.where("planCourse=:planCourse", planCourse)
      val res = entityDao.search(requestedMapQuery)
      for (j <- 0 until res.size) {
        val pl = res.get(j)(0).asInstanceOf[MajorPlan]
        val gr = res.get(j)(1).asInstanceOf[CourseGroup]
        val term = termCalc.getTerm(pl.getProgram.getEffectiveOn, true)
        if (requestedMap.get(pl) == null) {
          requestedMap.put(pl, new HashMap[CourseGroup, List[PlanCourse]]())
        }
        if (requestedMap.get(pl).get(gr) == null) {
          requestedMap.get(pl).put(gr, new ArrayList[PlanCourse]())
        }
        requestedMap.get(pl).get(gr).add(planCourse)
        if (originalMap.get(pl) == null) {
          originalMap.put(pl, new HashMap[CourseGroup, List[PlanCourse]]())
        }
        if (originalMap.get(pl).get(gr) == null) {
          originalMap.get(pl).put(gr, new ArrayList[PlanCourse](PlanUtils.getPlanCourses(gr, term)))
        }
      }
    }
    for (plan <- requestedMap.keySet) {
      val term = termCalc.getTerm(plan.getProgram.grade, true)
      for (group <- requestedMap.get(plan).keySet) {
        val requestedList = requestedMap.get(plan).get(group)
        val originalList = originalMap.get(plan).get(group)
        var requestedCreditsonThisTerm = 0f
        var totalCreditsOnThisTerm = 0f
        val requiredCreditsOnThisTerm = PlanUtils.getGroupCredits(group, term)
        for (i <- 0 until requestedList.size) {
          requestedCreditsonThisTerm += requestedList.get(i).getCourse.getCredits
        }
        for (i <- 0 until originalList.size) {
          totalCreditsOnThisTerm += originalList.get(i).getCourse.getCredits
        }
        if (totalCreditsOnThisTerm - requestedCreditsonThisTerm != 
          requiredCreditsOnThisTerm) {
          result.add(Array(plan, group, term, requiredCreditsOnThisTerm, totalCreditsOnThisTerm - requestedCreditsonThisTerm))
        }
      }
    }
    result
  }

  private def checkNotAppropriateOpenNastyWork(closedPlanCourses: List[PlanCourse], requestOpenPlanCourses: List[PlanCourse], semester: Semester): List[Array[Any]] = {
    val result = new ArrayList[Array[Any]]()
    val termCalc = new TermCalculator(semesterService, semester)
    val closedMap = new HashMap[MajorPlan, Map[CourseGroup, List[PlanCourse]]]()
    val originalMap = new HashMap[MajorPlan, Map[CourseGroup, List[PlanCourse]]]()
    for (i <- 0 until requestOpenPlanCourses.size) {
      val planCourse = requestOpenPlanCourses.get(i)
      val requestedMapQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      requestedMapQuery.select("plan, courseGroup")
      requestedMapQuery.join("plan.courseGroups", "courseGroup")
      requestedMapQuery.join("courseGroup.planCourses", "planCourse")
      requestedMapQuery.where("planCourse=:planCourse", planCourse)
      val res = entityDao.search(requestedMapQuery)
      for (j <- 0 until res.size) {
        val pl = res.get(j)(0).asInstanceOf[MajorPlan]
        val gr = res.get(j)(1).asInstanceOf[CourseGroup]
        val term = termCalc.getTerm(pl.getProgram.getEffectiveOn, true)
        if (closedMap.get(pl) == null) {
          closedMap.put(pl, new HashMap[CourseGroup, List[PlanCourse]]())
        }
        if (closedMap.get(pl).get(gr) == null) {
          closedMap.get(pl).put(gr, new ArrayList[PlanCourse]())
        }
        if (originalMap.get(pl) == null) {
          originalMap.put(pl, new HashMap[CourseGroup, List[PlanCourse]]())
        }
        if (originalMap.get(pl).get(gr) == null) {
          originalMap.get(pl).put(gr, new ArrayList[PlanCourse](PlanUtils.getPlanCourses(gr, term)))
        }
      }
    }
    for (i <- 0 until closedPlanCourses.size) {
      val planCourse = closedPlanCourses.get(i)
      val requestedMapQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      requestedMapQuery.select("plan, courseGroup")
      requestedMapQuery.join("plan.courseGroups", "courseGroup")
      requestedMapQuery.join("courseGroup.planCourses", "planCourse")
      requestedMapQuery.where("planCourse=:planCourse", planCourse)
      val res = entityDao.search(requestedMapQuery)
      for (j <- 0 until res.size) {
        val pl = res.get(j)(0).asInstanceOf[MajorPlan]
        val gr = res.get(j)(1).asInstanceOf[CourseGroup]
        val term = termCalc.getTerm(pl.getProgram.grade, true)
        if (closedMap.get(pl) == null) {
          closedMap.put(pl, new HashMap[CourseGroup, List[PlanCourse]]())
        }
        if (closedMap.get(pl).get(gr) == null) {
          closedMap.get(pl).put(gr, new ArrayList[PlanCourse]())
        }
        closedMap.get(pl).get(gr).add(planCourse)
        if (originalMap.get(pl) == null) {
          originalMap.put(pl, new HashMap[CourseGroup, List[PlanCourse]]())
        }
        if (originalMap.get(pl).get(gr) == null) {
          originalMap.get(pl).put(gr, new ArrayList[PlanCourse](PlanUtils.getPlanCourses(gr, term)))
        }
      }
    }
    for (plan <- closedMap.keySet) {
      val term = termCalc.getTerm(plan.getProgram.grade, true)
      for (group <- closedMap.get(plan).keySet) {
        val requestedList = closedMap.get(plan).get(group)
        requestedList.removeAll(requestOpenPlanCourses)
        val originalList = originalMap.get(plan).get(group)
        var requestedCreditsonThisTerm = 0f
        var totalCreditsOnThisTerm = 0f
        val requiredCreditsOnThisTerm = PlanUtils.getGroupCredits(group, term)
        for (i <- 0 until requestedList.size) {
          requestedCreditsonThisTerm += requestedList.get(i).getCourse.getCredits
        }
        for (i <- 0 until originalList.size) {
          totalCreditsOnThisTerm += originalList.get(i).getCourse.getCredits
        }
        if (totalCreditsOnThisTerm - requestedCreditsonThisTerm != 
          requiredCreditsOnThisTerm) {
          result.add(Array(plan, group, term, requiredCreditsOnThisTerm, totalCreditsOnThisTerm - requestedCreditsonThisTerm))
        }
      }
    }
    result
  }

  def getSemesterService(): SemesterService = semesterService

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }
}
