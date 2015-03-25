package org.openurp.edu.eams.teach.lesson.task.web.action.parent





import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.model.LessonBean
import org.openurp.edu.eams.teach.lesson.model.LessonPlanRelationBean
import org.openurp.edu.eams.teach.lesson.task.biz.PlanPackage
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanCheckService
import org.openurp.edu.eams.teach.major.helper.MajorPlanSearchHelper
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class LessonPlanCheckAction extends SemesterSupportAction {

  var lessonPlanCheckService: LessonPlanCheckService = _

  var majorPlanSearchHelper: MajorPlanSearchHelper = _

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    put("stateList", CommonAuditState.values)
    forward()
  }

  def search(): String = {
    val semester = semesterService.getSemester(getInt("semester.id"))
    val project = entityDao.get(classOf[Project], getInt("project.id"))
    val query = majorPlanSearchHelper.buildPlanQuery()
    query.where("plan.program.major.project = :project", project)
      .where("plan.program.major.project in (:projects)", getProjects)
      .where("plan.program.department in (:departments)", getDeparts)
      .where("plan.program.stdType in (:stdTypes)", getStdTypes)
    if (CollectUtils.isNotEmpty(getEducations)) {
      query.where("plan.program.education in (:educations)", getEducations)
    }
    majorPlanSearchHelper.addSemesterActiveCondition(query, semester)
    val plans = entityDao.search(query)
    put("plans", plans)
    put("semester", semester)
    forward()
  }

  protected def buildRelationOql(): OqlBuilder = {
    val planIds = Strings.splitToLong(get("planIds"))
    val project = entityDao.get(classOf[Project], getInt("project.id"))
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val query = OqlBuilder.from(classOf[LessonPlanRelation], "relation")
    query.where("relation.plan.id in (:planIds)", planIds)
      .where("relation.lesson.semester = :semester", semester)
      .where("relation.lesson.project = :project", project)
      .where("relation.plan.program.major.project = :project", project)
      .where("relation.plan.program.major.project in (:projects)", getProjects)
      .where("relation.plan.program.department in (:departs)", getDeparts)
      .where("relation.plan.program.stdType in (:stdTypes)", getStdTypes)
    if (CollectUtils.isNotEmpty(getEducations)) {
      query.where("relation.plan.program.education in (:educations)", getEducations)
    }
    query
  }

  def viewLessonPackage(): String = {
    val relations = entityDao.search(buildRelationOql().orderBy("relation.plan.program.grade"))
    val lessonSubmitStatus = new HashMap[String, CommonAuditState]()
    for (relation <- relations) {
      val plan = relation.getPlan
      val lesson = relation.getLesson
      lessonSubmitStatus.put(plan.id + "," + lesson.id, lesson.getAuditStatus)
    }
    put("lessonSubmitStatus", lessonSubmitStatus)
    val planPackages = lessonPlanCheckService.makePackages(relations)
    val planIds = Strings.splitToLong(get("planIds"))
    val idsOfPlanWithoutLesson = new ArrayList[Long]()
    for (planId <- planIds) {
      var has = false
      for (planPackage <- planPackages if planId == planPackage.getPlan.id) {
        has = true
        //break
      }
      if (!has) {
        idsOfPlanWithoutLesson.add(planId)
      }
    }
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val dummyLesson = new LessonBean()
    dummyLesson.setSemester(semester)
    val plansWithoutLesson = entityDao.get(classOf[MajorPlan], idsOfPlanWithoutLesson)
    for (plan <- plansWithoutLesson) {
      val dummyRelation = new LessonPlanRelationBean()
      dummyRelation.setLesson(dummyLesson)
      dummyRelation.setPlan(plan)
      val dummyRelations = new ArrayList[LessonPlanRelation]()
      dummyRelations.add(dummyRelation)
      planPackages.addAll(lessonPlanCheckService.makePackages(dummyRelations))
    }
    put("planPackages", planPackages)
    forward()
  }
}
