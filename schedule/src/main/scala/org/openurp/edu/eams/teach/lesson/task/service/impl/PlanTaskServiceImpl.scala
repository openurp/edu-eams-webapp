package org.openurp.edu.eams.teach.lesson.task.service.impl







import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Semester
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.lesson.task.model.PlanTask
import org.openurp.edu.eams.teach.lesson.task.service.PlanTaskService
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator



class PlanTaskServiceImpl extends BaseServiceImpl with PlanTaskService {

  var semesterService: SemesterService = _

  def checkIsAppropriateClose(planCourseList: Seq[PlanCourse], planTaskList: Seq[PlanTask], semester: Semester): Seq[Array[Any]] = {
    val res = findAllRelatedRequestClosePlanCourse(planCourseList, planTaskList, semester)
    res ++= planCourseList
    checkNotAppropriateClosedNastyWork(res, semester)
  }

  def checkIsAppropriateOpen(planCourseList: Seq[PlanCourse], planTaskList: Seq[PlanTask], semester: Semester): Seq[Array[Any]] = {
    val requestedOpenPlanCourses = Collections.newBuffer[PlanCourse]
    requestedOpenPlanCourses ++= planCourseList
    val termCalc = new TermCalculator(semesterService, semester)
    for (planTask <- planTaskList) {
      var term = 0
      term = termCalc.getTerm(planTask.teachPlan .program.beginOn, true)
      val planCourseQuery = OqlBuilder.from[PlanCourse](classOf[MajorPlan].getName, "plan")
      planCourseQuery.select("select planCourse")
      planCourseQuery.join("plan.courseGroups", "courseGroup")
      planCourseQuery.join("courseGroup.planCourses", "planCourse")
      planCourseQuery.where("plan=:plan", planTask.teachPlan )
      planCourseQuery.where("planCourse.course=:course", planTask.course)
      val termCondition = "(instr(planCourse.terms, '" + term + "') <> 0 and length(planCourse.terms)=length('" + 
        term + 
        "'))" + 
        "or (instr(planCourse.terms, '," + 
        term + 
        ",') <> 0 and length(planCourse.terms)<>length('" + 
        term + 
        "'))"
      planCourseQuery.where(termCondition)
      requestedOpenPlanCourses ++= entityDao.search(planCourseQuery)
    }
    checkNotAppropriateOpenNastyWork(findAllRelatedRequestClosePlanCourse(planCourseList, planTaskList, 
      semester), requestedOpenPlanCourses, semester)
  }

  def extractInappropriateTeachPlan(majorPlans: Iterable[MajorPlan], semester: Semester): collection.mutable.Map[MajorPlan, collection.mutable.Map[CourseGroup, Array[Double]]] = {
    val result = Collections.newMap[MajorPlan, collection.mutable.Map[CourseGroup, Array[Double]]]
    val termCalc = new TermCalculator(semesterService, semester)
    for (plan <- majorPlans) {
      val term = termCalc.getTerm(plan.program.beginOn, true)
      val singletonTermList = Collections.singletonList(term)
      for (group <- plan.groups) {
        val requiredCreditsOnTerm = PlanUtils.getGroupCredits(group, term).toDouble
        val planCourses = PlanUtils.getPlanCourses(group, term)
        if (planCourses.isEmpty) {
          //continue
        }
        val courses = Collections.collect(planCourses, new PropertyTransformer("course"))
        val closedCreditsQry = OqlBuilder.from(classOf[PlanTask], "sq")
        closedCreditsQry.select("select nvl(sum(sq.course.credits),0)")
        closedCreditsQry.where("sq.teachPlan=:plan", plan)
        closedCreditsQry.where("sq.semester=:semester", semester)
        closedCreditsQry.where("sq.flag=:flag", PlanTask.REQ_CLOSE)
        closedCreditsQry.where("sq.course in (:courses)", courses)
        val closedCreditsOnTerm = entityDao.search(closedCreditsQry)(0).asInstanceOf[java.lang.Double]
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
        val totalCreditsOnTerm = entityDao.search(totalCreditsQry)(0).asInstanceOf[java.lang.Double]
        if (totalCreditsOnTerm - closedCreditsOnTerm != requiredCreditsOnTerm) {
          if (result.get(plan) == null) {
            result.put(plan, collection.mutable.Map[CourseGroup, Array[Double]]())
          }
          result.get(plan).asInstanceOf[collection.mutable.Map[CourseGroup, Array[Double]]].put(group, Array(requiredCreditsOnTerm, totalCreditsOnTerm - closedCreditsOnTerm))
        }
      }
    }
    result
  }

  private def findAllRelatedRequestClosePlanCourse(planCourseList: Seq[PlanCourse], planTaskList: Seq[PlanTask], semester: Semester): collection.mutable.Buffer[PlanCourse] = {
    val requestedPlanCourses = Collections.newBuffer[PlanCourse]
    val termCalc = new TermCalculator(semesterService, semester)
    val allPlanTask = Collections.newSet[PlanTask]
    allPlanTask ++= planTaskList
    val usedTeachPlanId = Collections.newSet[Integer]
    for (i <- 0 until planTaskList.size) {
      if (usedTeachPlanId.contains(planTaskList(i).teachPlan.id)) {
        //continue
      }
      usedTeachPlanId.add(planTaskList(i).teachPlan.id)
      val planTaskQuery = OqlBuilder.from(classOf[PlanTask], "planTask")
      planTaskQuery.where("planTask.teachPlan=:plan", planTaskList(i).teachPlan)
      planTaskQuery.where("planTask.flag=:flag", PlanTask.REQ_CLOSE)
      allPlanTask ++= entityDao.search(planTaskQuery)
    }
    for (i <- 0 until planCourseList.size) {
      val planCourse = planCourseList(i)
      val teachPlanQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      teachPlanQuery.join("plan.courseGroups", "courseGroup")
      teachPlanQuery.join("courseGroup.planCourses", "planCourse")
      teachPlanQuery.where("planCourse=:planCourse", planCourse)
      val planTaskQuery = OqlBuilder.from(classOf[PlanTask], "planTask")
      planTaskQuery.where("planTask.teachPlan in (:plans)", entityDao.search(teachPlanQuery))
      planTaskQuery.where("planTask.flag=:flag", PlanTask.REQ_CLOSE)
      allPlanTask ++= entityDao.search(planTaskQuery)
    }
    for (planTask <- allPlanTask) {
      var term = 0
      term = termCalc.getTerm(planTask.teachPlan.program.beginOn, true)
      val planCourseQuery = OqlBuilder.from[PlanCourse](classOf[MajorPlan].getName, "plan")
      planCourseQuery.select("select planCourse")
      planCourseQuery.join("plan.courseGroups", "courseGroup")
      planCourseQuery.join("courseGroup.planCourses", "planCourse")
      planCourseQuery.where("plan=:plan", planTask.teachPlan)
      planCourseQuery.where("planCourse.course=:course", planTask.course)
      val termCondition = "(instr(planCourse.terms, '" + term + "') <> 0 and length(planCourse.terms)=length('" + 
        term + 
        "'))" + 
        "or (instr(planCourse.terms, '," + 
        term + 
        ",') <> 0 and length(planCourse.terms)<>length('" + 
        term + 
        "'))"
      planCourseQuery.where(termCondition)
      requestedPlanCourses ++= entityDao.search(planCourseQuery)
    }
    requestedPlanCourses
  }

  private def checkNotAppropriateClosedNastyWork(planCourses: Seq[PlanCourse], semester: Semester): Seq[Array[Any]] = {
    val result = Collections.newBuffer[Array[Any]]
    val termCalc = new TermCalculator(semesterService, semester)
    val requestedMap = Collections.newMap[MajorPlan, collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]]
    val originalMap = Collections.newMap[MajorPlan, collection.mutable.Map[CourseGroup, Seq[PlanCourse]]]
    for (i <- 0 until planCourses.size) {
      val planCourse = planCourses(i)
      val requestedMapQuery = OqlBuilder.from[Seq[Seq[Any]]](classOf[MajorPlan].getName, "plan")
      requestedMapQuery.select("plan, courseGroup")
      requestedMapQuery.join("plan.courseGroups", "courseGroup")
      requestedMapQuery.join("courseGroup.planCourses", "planCourse")
      requestedMapQuery.where("planCourse=:planCourse", planCourse)
      val res = entityDao.search(requestedMapQuery)
      for (j <- 0 until res.size) {
        val pl = res(j)(0).asInstanceOf[MajorPlan]
        val gr = res(j)(1).asInstanceOf[CourseGroup]
        val term = termCalc.getTerm(pl.program.beginOn, true)
        if (requestedMap.get(pl) == null) {
          requestedMap.put(pl, collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]())
        }
        if (requestedMap.get(pl).get(gr) == null) {
          requestedMap.get(pl).asInstanceOf[collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]].put(gr, Collections.newBuffer[PlanCourse]())
        }
        requestedMap.get(pl).get(gr) += planCourse
        if (originalMap.get(pl) == null) {
          originalMap.put(pl, collection.mutable.Map[CourseGroup, Seq[PlanCourse]]())
        }
        if (originalMap.get(pl).get(gr) == null) {
          originalMap.get(pl).asInstanceOf[collection.mutable.Map[CourseGroup, Seq[PlanCourse]]].put(gr, PlanUtils.getPlanCourses(gr, term))
        }
      }
    }
    for (plan <- requestedMap.keySet) {
      val term = termCalc.getTerm(plan.program.grade, true)
      for (group <- requestedMap.get(plan).asInstanceOf[collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]].keySet) {
        val requestedList = requestedMap.get(plan).get(group)
        val originalList = originalMap.get(plan).get(group)
        var requestedCreditsonThisTerm = 0f
        var totalCreditsOnThisTerm = 0f
        val requiredCreditsOnThisTerm = PlanUtils.getGroupCredits(group, term)
        for (i <- 0 until requestedList.size) {
          requestedCreditsonThisTerm += requestedList(i).course.credits
        }
        for (i <- 0 until originalList.size) {
          totalCreditsOnThisTerm += originalList(i).course.credits
        }
        if (totalCreditsOnThisTerm - requestedCreditsonThisTerm != 
          requiredCreditsOnThisTerm) {
          result += Array(plan, group, term, requiredCreditsOnThisTerm, totalCreditsOnThisTerm - requestedCreditsonThisTerm)
        }
      }
    }
    result
  }

  private def checkNotAppropriateOpenNastyWork(closedPlanCourses: Seq[PlanCourse], requestOpenPlanCourses: Seq[PlanCourse], semester: Semester): Seq[Array[Any]] = {
    val result = Collections.newBuffer[Array[Any]]
    val termCalc = new TermCalculator(semesterService, semester)
    val closedMap = Collections.newMap[MajorPlan, collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]]
    val originalMap = Collections.newMap[MajorPlan, collection.mutable.Map[CourseGroup, Seq[PlanCourse]]]
    for (i <- 0 until requestOpenPlanCourses.size) {
      val planCourse = requestOpenPlanCourses(i)
      val requestedMapQuery = OqlBuilder.from[Seq[Seq]](classOf[MajorPlan].getName, "plan")
      requestedMapQuery.select("plan, courseGroup")
      requestedMapQuery.join("plan.courseGroups", "courseGroup")
      requestedMapQuery.join("courseGroup.planCourses", "planCourse")
      requestedMapQuery.where("planCourse=:planCourse", planCourse)
      val res = entityDao.search(requestedMapQuery)
      for (j <- 0 until res.size) {
        val pl = res(j)(0).asInstanceOf[MajorPlan]
        val gr = res(j)(1).asInstanceOf[CourseGroup]
        val term = termCalc.getTerm(pl.program.beginOn, true)
        if (closedMap.get(pl) == null) {
          closedMap.put(pl, collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]())
        }
        if (closedMap.get(pl).get(gr) == null) {
          closedMap.get(pl).asInstanceOf[collection.mutable.Map[CourseGroup, Seq[PlanCourse]]].put(gr, Collections.newBuffer[PlanCourse])
        }
        if (originalMap.get(pl) == null) {
          originalMap.put(pl, collection.mutable.Map[CourseGroup, Seq[PlanCourse]]())
        }
        if (originalMap.get(pl).get(gr) == null) {
          originalMap.get(pl).asInstanceOf[collection.mutable.Map[CourseGroup, Seq[PlanCourse]]].put(gr, (PlanUtils.getPlanCourses(gr, term)))
        }
      }
    }
    for (i <- 0 until closedPlanCourses.size) {
      val planCourse = closedPlanCourses(i)
      val requestedMapQuery = OqlBuilder.from[Seq[Seq]](classOf[MajorPlan].getName, "plan")
      requestedMapQuery.select("plan, courseGroup")
      requestedMapQuery.join("plan.courseGroups", "courseGroup")
      requestedMapQuery.join("courseGroup.planCourses", "planCourse")
      requestedMapQuery.where("planCourse=:planCourse", planCourse)
      val res = entityDao.search(requestedMapQuery)
      for (j <- 0 until res.size) {
        val pl = res(j)(0).asInstanceOf[MajorPlan]
        val gr = res(j)(1).asInstanceOf[CourseGroup]
        val term = termCalc.getTerm(pl.program.grade, true)
        if (closedMap.get(pl) == null) {
          closedMap.put(pl, collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]())
        }
        if (closedMap.get(pl).get(gr) == null) {
          closedMap.get(pl).asInstanceOf[collection.mutable.Map[CourseGroup, Seq[PlanCourse]]].put(gr, Collections.newBuffer[PlanCourse]())
        }
        closedMap.get(pl).get(gr) += planCourse
        if (originalMap.get(pl) == null) {
          originalMap.put(pl, collection.mutable.Map[CourseGroup, Seq[PlanCourse]]())
        }
        if (originalMap.get(pl).get(gr) == null) {
          originalMap.get(pl).asInstanceOf[collection.mutable.Map[CourseGroup, Seq[PlanCourse]]].put(gr, (PlanUtils.getPlanCourses(gr, term)))
        }
      }
    }
    for (plan <- closedMap.keySet) {
      val term = termCalc.getTerm(plan.program.grade, true)
      for (group <- closedMap.get(plan).asInstanceOf[collection.mutable.Map[CourseGroup, collection.mutable.Buffer[PlanCourse]]].keySet) {
        val requestedList = closedMap.get(plan).get(group)
        requestedList --= (requestOpenPlanCourses)
        val originalList = originalMap.get(plan).get(group)
        var requestedCreditsonThisTerm = 0f
        var totalCreditsOnThisTerm = 0f
        val requiredCreditsOnThisTerm = PlanUtils.getGroupCredits(group, term)
        for (i <- 0 until requestedList.size) {
          requestedCreditsonThisTerm += requestedList(i).course.credits
        }
        for (i <- 0 until originalList.size) {
          totalCreditsOnThisTerm += originalList(i).course.credits
        }
        if (totalCreditsOnThisTerm - requestedCreditsonThisTerm != 
          requiredCreditsOnThisTerm) {
          result += Array(plan, group, term, requiredCreditsOnThisTerm, totalCreditsOnThisTerm - requestedCreditsonThisTerm)
        }
      }
    }
    result
  }
}
