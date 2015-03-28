package org.openurp.edu.eams.teach.lesson.task.service.impl

import java.text.MessageFormat





import java.util.LinkedList



import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.code.nation.TeacherTitle
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.task.dao.LessonStatDao
import org.openurp.edu.eams.teach.lesson.task.service.LessonStatService
import org.openurp.edu.eams.teach.lesson.task.util.TaskOfCourseType
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.teach.util.AdminclassQueryBuilder
import org.openurp.edu.eams.util.stat.StatGroup
import org.openurp.edu.eams.util.stat.StatHelper
import org.openurp.edu.eams.util.stat.StatItem



class LessonStatServiceImpl extends BaseServiceImpl with LessonStatService {

  var lessonStatDao: LessonStatDao = _

  var lessonLimitService: LessonLimitService = _

  def countByAdminclass(project: Project, semester: Semester, dataRealm: DataRealm): List[_] = {
    var query = OqlBuilder.from(classOf[Lesson], "lesson").select("distinct lesson")
      .join("lesson.teachClass.limitGroups", "lgroup")
      .join("lgroup.items", "litem")
      .where("litem.meta.id = :metaId", LessonLimitMeta.Adminclass.getMetaId)
      .where("litem.content like '_%'")
      .where("not exists (select tag.id from lesson.tags tag where tag.id=:guaPai)", LessonTag.PredefinedTags.GUAPAI.id)
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .where("lesson.teachDepart.id in (:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
    var commonLessons = entityDao.search(query)
    var tmpMap = new HashMap[Integer, StatItem]()
    for (lesson <- commonLessons) {
      val adminclasses = lessonLimitService.extractAdminclasses(lesson.getTeachClass)
      for (adminclass <- adminclasses) {
        var item = tmpMap.get(adminclass.id)
        if (item == null) {
          item = new StatItem(adminclass.id, 0, 0f, 0f, 0f)
          tmpMap.put(adminclass.id, item)
        }
        val countors = item.getCountors
        countors(0) = countors(0).asInstanceOf[java.lang.Integer] + 1
        countors(1) = countors(1).asInstanceOf[java.lang.Float] + lesson.getCourse.getWeekHour
        countors(2) = countors(2).asInstanceOf[java.lang.Float] + lesson.getCourse.getPeriod
        countors(3) = countors(3).asInstanceOf[java.lang.Float] + lesson.getCourse.getCredits
      }
    }
    val commonTasks = tmpMap.values
    query = OqlBuilder.from(classOf[Lesson], "lesson").select("distinct lesson")
      .join("lesson.teachClass.limitGroups", "lgroup")
      .join("lgroup.items", "litem")
      .where("litem.meta.id = :metaId", LessonLimitMeta.Adminclass.getMetaId)
      .where("litem.content like '_%'")
      .where("exists (select tag.id from lesson.tags tag where tag.id=:guaPai)", LessonTag.PredefinedTags.GUAPAI.id)
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .where("lesson.teachDepart.id in (:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
    commonLessons = entityDao.search(query)
    tmpMap = new HashMap[Integer, StatItem]()
    for (lesson <- commonLessons) {
      val adminclasses = lessonLimitService.extractAdminclasses(lesson.getTeachClass)
      for (adminclass <- adminclasses) {
        var item = tmpMap.get(adminclass.id)
        if (item == null) {
          item = new StatItem(adminclass.id, 0, 0f, 0f, 0f)
          tmpMap.put(adminclass.id, item)
        }
        val countors = item.getCountors
        countors(0) = countors(0).asInstanceOf[java.lang.Integer] + 1
        countors(1) = countors(1).asInstanceOf[java.lang.Float] + lesson.getCourse.getWeekHour
        countors(2) = countors(2).asInstanceOf[java.lang.Float] + 
          lesson.getCourse.getWeekHour * lesson.getCourseSchedule.getWeeks
        countors(3) = countors(3).asInstanceOf[java.lang.Float] + lesson.getCourse.getCredits
      }
    }
    val gpTasks = tmpMap.values
    val statMap = new HashMap()
    var iter = commonTasks.iterator()
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatItem]
      statMap.put(element.getWhat, element)
    }
    var iter = gpTasks.iterator()
    while (iter.hasNext) {
      val pgStat = iter.next().asInstanceOf[StatItem]
      val stat = statMap.get(pgStat.getWhat).asInstanceOf[StatItem]
      if (null == stat) {
        statMap.put(pgStat.getWhat, new StatItem(pgStat.getWhat, java.lang.Integer.valueOf(1), pgStat.getCountors()(1), 
          pgStat.getCountors()(2), pgStat.getCountors()(3)))
      } else {
        stat.getCountors()(0) = java.lang.Integer.valueOf(stat.getCountors()(0).asInstanceOf[Number].intValue() + 
          1)
        stat.getCountors()(1) = new java.lang.Float(pgStat.getCountors()(1).asInstanceOf[Number].floatValue() + 
          stat.getCountors()(1).asInstanceOf[Number].floatValue())
        stat.getCountors()(2) = java.lang.Integer.valueOf(pgStat.getCountors()(2).asInstanceOf[Number].intValue() + 
          stat.getCountors()(2).asInstanceOf[Number].intValue())
        stat.getCountors()(3) = new java.lang.Float(pgStat.getCountors()(3).asInstanceOf[Number].floatValue() + 
          stat.getCountors()(3).asInstanceOf[Number].floatValue())
      }
    }
    val adminClasses = entityDao.get(classOf[Adminclass], "id", statMap.keySet)
    for (adminClass <- adminClasses) {
      val stat = statMap.get(adminClass.id).asInstanceOf[StatItem]
      stat.setWhat(adminClass)
    }
    new ArrayList(statMap.values)
  }

  def countByTeacher(project: Project, semester: Semester, dataRealm: DataRealm): List[_] = {
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.select("select new org.openurp.edu.eams.util.stat.StatItem(" + 
      "teacher.id," + 
      "count(*)," + 
      "sum(lesson.course.weekHour)," + 
      "sum(lesson.course.weekHour * (lesson.schedule.endWeek - lesson.schedule.startWeek + 1))," + 
      "sum(lesson.course.credits)" + 
      ")")
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .join("lesson.teachers", "teacher")
      .groupBy("teacher.id")
    addLessonDataRealm(query, dataRealm)
    val stats = entityDao.search(query)
    setStatEntities(stats, classOf[Teacher])
  }

  def countByCourseType(project: Project, semester: Semester, dataRealm: DataRealm): List[_] = {
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.select("select new org.openurp.edu.eams.util.stat.StatItem(" + 
      "lesson.courseType.id," + 
      "count(*)," + 
      "sum(lesson.course.weekHour)," + 
      "sum(lesson.course.weekHour * (lesson.schedule.endWeek - lesson.schedule.startWeek + 1))," + 
      "sum(lesson.course.credits)" + 
      ")")
      .where("lesson.semester=:semester", semester)
      .where("lesson.project = :project", project)
      .groupBy("lesson.courseType.id")
    addLessonDataRealm(query, dataRealm)
    val stats = entityDao.search(query)
    setStatEntities(stats, classOf[CourseType])
  }

  def countByStdType(project: Project, semester: Semester, dataRealm: DataRealm): List[_] = {
    val entityQuery = OqlBuilder.from(classOf[Lesson], "lesson")
    entityQuery.select("select new  org.openurp.edu.eams.util.stat.StatItem(" + 
      "lesson.teachClass.stdType.id," + 
      "count(*)," + 
      "sum(lesson.course.weekHour)," + 
      "sum(lesson.course.weekHour * (lesson.schedule.endWeek - lesson.schedule.startWeek + 1))," + 
      "sum(lesson.course.credits)" + 
      ")")
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .groupBy("lesson.teachClass.stdType.id")
    val stats = entityDao.search(entityQuery)
    setStatEntities(stats, classOf[StdType])
  }

  def countByTeachDepart(project: Project, semester: Semester, dataRealm: DataRealm): List[_] = {
    val entityQuery = OqlBuilder.from(classOf[Lesson], "lesson")
    entityQuery.select("select new  org.openurp.edu.eams.util.stat.StatItem(" + 
      "lesson.teachDepart.id," + 
      "count(*)," + 
      "sum(lesson.course.weekHour)," + 
      "sum(lesson.course.weekHour * (lesson.schedule.endWeek - lesson.schedule.startWeek + 1))," + 
      "sum(lesson.course.credits)" + 
      ")")
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .groupBy("lesson.teachDepart.id")
    addLessonDataRealm(entityQuery, dataRealm)
    val stats = entityDao.search(entityQuery)
    setStatEntities(stats, classOf[Department])
  }

  def statCourseTypeConfirm(project: Project, semester: Semester, dataRealm: DataRealm): List[_] = {
    statTaskConfirm(project, semester, dataRealm, "lesson.courseType.id", classOf[CourseType])
  }

  def statTeachDepartConfirm(project: Project, semester: Semester, dataRealm: DataRealm): List[_] = {
    statTaskConfirm(project, semester, dataRealm, "lesson.teachDepart.id", classOf[Department])
  }

  private def statQuery(project: Project, 
      semester: Semester, 
      dataRealm: DataRealm, 
      item: String, 
      index: Int): OqlBuilder = {
    val entityQuery = OqlBuilder.from(classOf[Lesson], "lesson")
    val arr = Array("0", "0", "0", "0")
    arr(index) = "count(*)"
    entityQuery.select("select new  org.openurp.edu.eams.util.stat.StatItem(" + 
      item + 
      "," + 
      Strings.join(arr, ",") + 
      ")")
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .groupBy(item)
    addLessonDataRealm(entityQuery, dataRealm)
    entityQuery
  }

  private def statTaskConfirm(project: Project, 
      semester: Semester, 
      dataRealm: DataRealm, 
      item: String, 
      entityClass: Class[_]): List[_] = {
    var entityQuery = statQuery(project, semester, dataRealm, item, 0)
    entityQuery.where("lesson.auditStatus = :status", CommonAuditState.UNSUBMITTED)
    val unsubmittedStats = entityDao.search(entityQuery)
    val statMap = buildStatMap(unsubmittedStats)
    entityQuery = statQuery(project, semester, dataRealm, item, 1)
    entityQuery.where("lesson.auditStatus = :status", CommonAuditState.SUBMITTED)
    val submittedStats = entityDao.search(entityQuery)
    entityQuery = statQuery(project, semester, dataRealm, item, 2)
    entityQuery.where("lesson.auditStatus = :status", CommonAuditState.ACCEPTED)
    val acceptedStats = entityDao.search(entityQuery)
    entityQuery = statQuery(project, semester, dataRealm, item, 3)
    entityQuery.where("lesson.auditStatus = :status", CommonAuditState.REJECTED)
    val rejectedStats = entityDao.search(entityQuery)
    for (submitted <- submittedStats) {
      val existItem = statMap.get(submitted.getWhat).asInstanceOf[StatItem]
      if (existItem == null) {
        statMap.put(submitted.getWhat, submitted)
      } else {
        existItem.getCountors()(1) = submitted.getCountors()(1)
      }
    }
    for (accepted <- acceptedStats) {
      val existItem = statMap.get(accepted.getWhat).asInstanceOf[StatItem]
      if (existItem == null) {
        statMap.put(accepted.getWhat, accepted)
      } else {
        existItem.getCountors()(2) = accepted.getCountors()(2)
      }
    }
    for (rejected <- rejectedStats) {
      val existItem = statMap.get(rejected.getWhat).asInstanceOf[StatItem]
      if (existItem == null) {
        statMap.put(rejected.getWhat, rejected)
      } else {
        existItem.getCountors()(3) = rejected.getCountors()(3)
      }
    }
    setStatEntities(statMap, entityClass)
  }

  private def buildStatMap(stats: Iterable[_]): Map[_,_] = {
    val statMap = new HashMap()
    var iter = stats.iterator()
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatItem]
      statMap.put(element.getWhat, element)
    }
    statMap
  }

  private def setStatEntities(statMap: Map[_,_], entityClass: Class[_]): List[_] = {
    val entities = entityDao.get(entityClass, "id", statMap.keySet)
    var iter = entities.iterator()
    while (iter.hasNext) {
      val entity = iter.next().asInstanceOf[Entity[_]]
      val stat = statMap.get(entity.id).asInstanceOf[StatItem]
      stat.setWhat(entity)
    }
    new ArrayList(statMap.values)
  }

  private def setStatEntities(stats: Iterable[_], entityClass: Class[_]): List[_] = {
    val statMap = buildStatMap(stats)
    setStatEntities(statMap, entityClass)
  }

  private def addAdminclassDataRealm(query: OqlBuilder, dataRealm: DataRealm) {
    if (null != dataRealm) {
      if (Strings.isNotBlank(dataRealm.getStudentTypeIdSeq)) {
        query.where(MessageFormat.format("adminClass.stdType.id (:stdTypeIds{0})", System.currentTimeMillis()), 
          Strings.splitToInt(dataRealm.getStudentTypeIdSeq))
      }
      if (Strings.isNotBlank(dataRealm.departmentIdSeq)) {
        query.where(MessageFormat.format("adminClass.department.id in(:departIds)", System.currentTimeMillis()), 
          Strings.splitToInt(dataRealm.departmentIdSeq))
      }
    }
  }

  private def addLessonDataRealm(query: OqlBuilder, dataRealm: DataRealm) {
    if (null != dataRealm) {
      if (Strings.isNotBlank(dataRealm.departmentIdSeq)) {
        query.where("lesson.teachDepart.id in(:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
      }
    }
  }

  def statTeacherTitle(project: Project, semesters: List[_]): List[_] = {
    val stats = lessonStatDao.statTeacherTitle(semesters)
    new StatHelper(entityDao).replaceIdWith(stats, Array(classOf[Semester], classOf[TeacherTitle]))
    stats
  }

  def setLessonStatDao(lessonStatDao: LessonStatDao) {
    this.lessonStatDao = lessonStatDao
  }

  private var majorPlanService: MajorPlanService = _

  private var semesterService: SemesterService = _

  def getTaskOfCourseTypes(project: Project, 
      semester: Semester, 
      dataRealm: DataRealm, 
      courseTypes: Iterable[_]): List[TaskOfCourseType] = {
    val courseTypeSet = new HashSet(courseTypes)
    var plans = Collections.newBuffer[Any]
    if (!Strings.isEmpty(dataRealm.getStudentTypeIdSeq) && !Strings.isEmpty(dataRealm.departmentIdSeq)) {
      val now = new java.util.Date()
      val planQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      planQuery.where("plan.program.stdType.id in (:stdTypeIds)", Strings.splitToInt(dataRealm.getStudentTypeIdSeq))
        .where("plan.program.department.id in (:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
        .where("plan.program.major.project = :project", project)
        .where(":now < plan.program.invalidOn or plan.program.invalidOn is null", now)
      plans = entityDao.search(planQuery)
    }
    val termCalc = new TermCalculator(semesterService, semester)
    val taskOfCourseTypes = new LinkedList[TaskOfCourseType]()
    for (plan <- plans) {
      val adminClasses = entityDao.search(AdminclassQueryBuilder.build(plan))
      val term = termCalc.getTerm(plan.getProgram.getEffectiveOn, true)
      if (term < 0 || term > plan.getTermsCount.intValue()) {
        //continue
      }
      var iterator = plan.getGroups.iterator()
      while (iterator.hasNext) {
        val group = iterator.next()
        if (!courseTypeSet.contains(group.getCourseType)) {
          //continue
        }
        if (Collections.isNotEmpty(group.getPlanCourses)) {
          //continue
        }
        val credits = PlanUtils.getGroupCredits(group, term)
        if (credits == 0f) {
          //continue
        }
        for (adminClass <- adminClasses) {
          taskOfCourseTypes.add(new TaskOfCourseType(group.getCourseType, adminClass, credits))
        }
      }
    }
    taskOfCourseTypes
  }

  def setMajorPlanService(majorPlanService: MajorPlanService) {
    this.majorPlanService = majorPlanService
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setLessonLimitService(lessonLimitService: LessonLimitService) {
    this.lessonLimitService = lessonLimitService
  }
}
