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
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
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
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.code.LessonTag
import org.openurp.code.job.ProfessionalTitle

class LessonStatServiceImpl extends BaseServiceImpl with LessonStatService {

  var lessonStatDao: LessonStatDao = _

  var lessonLimitService: LessonLimitService = _

  def countByAdminclass(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_] = {
    var query = OqlBuilder.from(classOf[Lesson], "lesson").select("distinct lesson")
      .join("lesson.teachClass.limitGroups", "lgroup")
      .join("lgroup.items", "litem")
      .where("litem.meta.id = :metaId", LessonLimitMeta.Adminclass.id)
      .where("litem.content like '_%'")
      .where("not exists (select tag.id from lesson.tags tag where tag.id=:guaPai)", LessonTag.PredefinedTags.GUAPAI.id)
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .where("lesson.teachDepart.id in (:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
    var commonLessons = entityDao.search(query)
    var tmpMap = Collections.newMap[Integer, StatItem]
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
      .where("litem.meta.id = :metaId", LessonLimitMeta.Adminclass.id)
      .where("litem.content like '_%'")
      .where("exists (select tag.id from lesson.tags tag where tag.id=:guaPai)", LessonTag.PredefinedTags.GUAPAI.id)
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
      .where("lesson.teachDepart.id in (:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
    commonLessons = entityDao.search(query)
    tmpMap = Collections.newMap[Integer, StatItem]
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
    val statMap = Collections.newMap[Any, Any]
    var iter = commonTasks.iterator
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatItem]
      statMap.put(element.what, element)
    }
    iter = gpTasks.iterator
    while (iter.hasNext) {
      val pgStat = iter.next().asInstanceOf[StatItem]
      val stat = statMap.get(pgStat.what).asInstanceOf[StatItem]
      if (null == stat) {
        statMap.put(pgStat.what, new StatItem(pgStat.what, java.lang.Integer.valueOf(1), pgStat.countors(1),
          pgStat.countors(2), pgStat.countors(3)))
      } else {
        stat.countors(0) = java.lang.Integer.valueOf(stat.countors(0).asInstanceOf[Number].intValue() +
          1)
        stat.countors(1) = new java.lang.Float(pgStat.countors(1).asInstanceOf[Number].floatValue() +
          stat.countors(1).asInstanceOf[Number].floatValue())
        stat.countors(2) = java.lang.Integer.valueOf(pgStat.countors(2).asInstanceOf[Number].intValue() +
          stat.countors(2).asInstanceOf[Number].intValue())
        stat.countors(3) = new java.lang.Float(pgStat.countors(3).asInstanceOf[Number].floatValue() +
          stat.countors(3).asInstanceOf[Number].floatValue())
      }
    }
    val adminClasses = entityDao.findBy(classOf[Adminclass], "id", statMap.keySet)
    for (adminClass <- adminClasses) {
      val stat = statMap.get(adminClass.id).asInstanceOf[StatItem]
      stat.what = adminClass
    }
    Collections.newBuffer(statMap.values)
  }

  def countByTeacher(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_] = {
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

  def countByCourseType(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_] = {
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

  def countByStdType(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_] = {
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

  def countByTeachDepart(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_] = {
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

  def statCourseTypeConfirm(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_] = {
    statTaskConfirm(project, semester, dataRealm, "lesson.courseType.id", classOf[CourseType])
  }

  def statTeachDepartConfirm(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_] = {
    statTaskConfirm(project, semester, dataRealm, "lesson.teachDepart.id", classOf[Department])
  }

  private def statQuery(project: Project,
    semester: Semester,
    dataRealm: DataRealm,
    item: String,
    index: Int): OqlBuilder[StatItem] = {
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
    entityQuery.asInstanceOf[OqlBuilder[StatItem]]
  }

  private def statTaskConfirm(project: Project,
    semester: Semester,
    dataRealm: DataRealm,
    item: String,
    entityClass: Class[_]): Seq[_] = {
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
      val existItem = statMap.get(submitted.what).asInstanceOf[StatItem]
      if (existItem == null) {
        statMap.put(submitted.what, submitted)
      } else {
        existItem.countors(1) = submitted.countors(1)
      }
    }
    for (accepted <- acceptedStats) {
      val existItem = statMap.get(accepted.what).asInstanceOf[StatItem]
      if (existItem == null) {
        statMap.put(accepted.what, accepted)
      } else {
        existItem.countors(2) = accepted.countors(2)
      }
    }
    for (rejected <- rejectedStats) {
      val existItem = statMap.get(rejected.what).asInstanceOf[StatItem]
      if (existItem == null) {
        statMap.put(rejected.what, rejected)
      } else {
        existItem.countors(3) = rejected.countors(3)
      }
    }
    setStatEntities(statMap, entityClass)
  }

  private def buildStatMap(stats: Iterable[_]): collection.mutable.Map[Any, Any] = {
    val statMap = Collections.newMap[Any, Any]
    var iter = stats.iterator
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatItem]
      statMap.put(element.what, element)
    }
    statMap
  }

  private def setStatEntities(statMap: collection.mutable.Map[Any, Any], entityClass: Class[_]): Seq[_] = {

    val ec = entityClass.asInstanceOf[Class[Entity[_]]]
    val entities = entityDao.findBy(ec, "id", statMap.keySet)
    var iter = entities.iterator
    while (iter.hasNext) {
      val entity = iter.next().asInstanceOf[Entity[_]]
      val stat = statMap.get(entity.id).asInstanceOf[StatItem]
      stat.what = entity
    }
    Collections.newBuffer(statMap.values)
  }

  private def setStatEntities(stats: Iterable[_], entityClass: Class[_]): Seq[_] = {
    val statMap = buildStatMap(stats)
    setStatEntities(statMap, entityClass)
  }

  private def addAdminclassDataRealm(query: OqlBuilder[_], dataRealm: DataRealm) {
    if (null != dataRealm) {
      if (Strings.isNotBlank(dataRealm.studentTypeIdSeq)) {
        query.where(MessageFormat.format("adminClass.stdType.id (:stdTypeIds{0})", new java.lang.Long(System.currentTimeMillis())),
          Strings.splitToInt(dataRealm.studentTypeIdSeq))
      }
      if (Strings.isNotBlank(dataRealm.departmentIdSeq)) {
        query.where(MessageFormat.format("adminClass.department.id in(:departIds)", new java.lang.Long(System.currentTimeMillis())),
          Strings.splitToInt(dataRealm.departmentIdSeq))
      }
    }
  }

  private def addLessonDataRealm(query: OqlBuilder[_], dataRealm: DataRealm) {
    if (null != dataRealm) {
      if (Strings.isNotBlank(dataRealm.departmentIdSeq)) {
        query.where("lesson.teachDepart.id in(:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
      }
    }
  }

  def statTeacherTitle(project: Project, semesters: List[_]): List[_] = {
    val stats = lessonStatDao.statTeacherTitle(semesters)
    new StatHelper(entityDao).replaceIdWith(stats, Array(classOf[Semester], classOf[ProfessionalTitle]))
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
    courseTypes: Iterable[CourseType]): Seq[TaskOfCourseType] = {
    val courseTypeSet = Collections.newSet[CourseType]
    courseTypeSet ++= (courseTypes)
    var plans: Seq[MajorPlan] = null
    if (!Strings.isEmpty(dataRealm.studentTypeIdSeq) && !Strings.isEmpty(dataRealm.departmentIdSeq)) {
      val now = new java.util.Date()
      val planQuery = OqlBuilder.from(classOf[MajorPlan], "plan")
      planQuery.where("plan.program.stdType.id in (:stdTypeIds)", Strings.splitToInt(dataRealm.studentTypeIdSeq))
        .where("plan.program.department.id in (:departIds)", Strings.splitToInt(dataRealm.departmentIdSeq))
        .where("plan.program.major.project = :project", project)
        .where(":now < plan.program.invalidOn or plan.program.invalidOn is null", now)
      plans = entityDao.search(planQuery)
    }
    val termCalc = new TermCalculator(semesterService, semester)
    val taskOfCourseTypes = Collections.newBuffer[TaskOfCourseType]
    for (plan <- plans) {
      val adminClasses = entityDao.search(AdminclassQueryBuilder.build(plan))
      val term = termCalc.getTerm(plan.program.beginOn, true)
      if (term < 0 || term > plan.terms.intValue) {
        //continue
      }
      var iterator = plan.groups.iterator
      while (iterator.hasNext) {
        val group = iterator.next()
        val credits = PlanUtils.getGroupCredits(group, term)
        val work = courseTypeSet.contains(group.courseType) || !Collections.isNotEmpty(group.planCourses) || credits != 0f
        if (work) {
          for (adminClass <- adminClasses) {
            taskOfCourseTypes += new TaskOfCourseType(group.courseType, adminClass, credits)
          }
        }
      }
    }
    taskOfCourseTypes
  }
}
