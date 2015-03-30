package org.openurp.edu.eams.teach.lesson.service.internal

import java.io.Serializable
import java.util.Arrays
import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.CourseSchedule
import org.openurp.edu.teach.lesson.model.LessonBean
import org.openurp.edu.teach.lesson.model.CourseScheduleBean
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.service.LessonLimitUtils
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.service.TaskCopyParams
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.base.User
import org.openurp.edu.base.States

class LogHelper {

  def info(s: String) {
  }
}

class LessonServiceImpl extends BaseServiceImpl with LessonService {

  private var lessonDao: LessonDao = _

  private var lessonLogHelper: LessonLogHelper = _

  def teachDepartsOfSemester(projects: List[Project], departments: List[Department], semester: Semester): Seq[Department] = {
    if (Collections.isNotEmpty(projects) && Collections.isNotEmpty(departments)) {
      val query = OqlBuilder.from(classOf[Lesson].getName + " lesson")
      query.select("distinct(lesson.teachDepart)")
      query.where(" lesson.semester=:semester and lesson.teachDepart in (:departments) and lesson.project in (:projects) ",
        semester, departments, projects)
      entityDao.search(query)
    } else {
      Collections.newBuffer[Department]
    }
  }

  def courseTypesOfSemester(projects: List[Project], departments: List[Department], semester: Semester): Seq[CourseType] = {
    if (Collections.isNotEmpty(projects) && Collections.isNotEmpty(departments)) {
      val query = OqlBuilder.from(classOf[Lesson].getName + " lesson")
      query.select("distinct(lesson.courseType)")
      query.where(" lesson.semester=:semester and lesson.teachDepart in (:departments) and lesson.project in (:projects) ",
        semester, departments, projects)
      entityDao.search(query)
    } else {
      Collections.newBuffer[CourseType]
    }
  }

  def attendDepartsOfSemester(projects: List[Project], semester: Semester): Seq[Department] = {
    if (Collections.isNotEmpty(projects)) {
      val qq = OqlBuilder.from[LessonLimitItem](classOf[Lesson].getName + " lesson")
      qq.join("lesson.teachClass.limitGroups", "lgroup").join("lgroup.items", "litem")
        .where("litem.meta.id = :metaid", LessonLimitMeta.Department.id)
        .where("lesson.semester = :semester", semester)
        .where("lesson.project in (:projects)", projects)
      qq.select("litem").cacheable()
      val limitItems = entityDao.search(qq)
      val sb = new StringBuilder()
      for (item <- limitItems) {
        val ids = Strings.split(item.content)
        sb.append(Strings.join(ids, ",")).append(',')
      }
      val departmentIds = Strings.splitToInt(sb.toString)
      val distinctIds = new collection.mutable.ListBuffer[Integer]
      Arrays.sort(departmentIds)
      var prev = -1
      for (i <- 0 until departmentIds.length) {
        if (prev != departmentIds(i)) {
          distinctIds += departmentIds(i)
        }
        prev = departmentIds(i)
      }
      if (Collections.isEmpty(distinctIds)) {
        return List.empty
      }
      entityDao.findBy(classOf[Department], "id", distinctIds)
    } else {
      Collections.newBuffer[Department]
    }
  }

  def canAttendDepartsOfSemester(projects: List[Project], departments: List[Department], semester: Semester): Seq[Department] = {
    if (Collections.isNotEmpty(projects) && Collections.isNotEmpty(departments)) {
      val query = OqlBuilder.from(classOf[Department], "department")
      query.where("exists (from org.openurp.edu.teach.plan.MajorPlan plan" +
        " where plan.program.department=department and plan.program.major.project in (:projects)" +
        " and plan.program.department in (:departs)" +
        " and current_date() >= plan.program.effectiveOn and (plan.program.invalidOn is null or current_date() <= plan.program.invalidOn))",
        semester, departments, projects)
      entityDao.search(query)
    } else {
      Collections.newBuffer[Department]
    }
  }

  def getProjectsForTeacher(teacher: Teacher): Seq[Project] = {
    val query = OqlBuilder.from(classOf[Lesson], "lesson").select("lesson.project")
      .join("lesson.teachers", "teacher")
      .where("teacher = :teacher", teacher)
    entityDao.search(query).asInstanceOf[Seq[Project]]
  }

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semesters: Iterable[Semester]): Seq[Lesson] = {
    if (null == id || semesters.isEmpty) {
      Collections.newBuffer[Lesson]
    } else {
      val prefix = "select distinct lesson.id from org.openurp.edu.teach.lesson.Lesson as lesson "
      val postfix = " and lesson.semester in (:semesters) "
      val query = OqlBuilder.oql[java.lang.Long](prefix + strategy.filterString + postfix)
      query.param("id", id)
      query.param("semesters", semesters)
      val lessonIds = entityDao.search(query)
      if (Collections.isNotEmpty(lessonIds)) {
        entityDao.findBy(classOf[Lesson], "id", lessonIds)
      } else {
        Collections.newBuffer[Lesson]
      }
    }
  }

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semester: Semester): Seq[Lesson] = {
    if (null == id || null == semester.id) {
      Collections.newBuffer[Lesson]
    } else {
      getLessonByCategory(id, strategy, List(semester))
    }
  }

  def clone(lesson: Lesson): Lesson = {
    val newlesson = new LessonBean
    newlesson.semester = lesson.semester
    newlesson.course = lesson.course
    newlesson.campus = lesson.campus
    newlesson.courseType = lesson.courseType

    newlesson.langType = lesson.langType
    newlesson.remark = lesson.remark
    newlesson.state = States.Draft
    newlesson.teachDepart = lesson.teachDepart
    newlesson.teachers ++= lesson.teachers

    val newSchedule = new CourseScheduleBean
    newlesson.schedule = newSchedule
    newSchedule.lesson = newlesson
    newSchedule.startWeek = lesson.schedule.startWeek
    newSchedule.endWeek = lesson.schedule.endWeek
    newSchedule.period = lesson.schedule.period
    newSchedule.roomType = lesson.schedule.roomType

    //FIMXE 
    //    newlesson.exam=lesson.exam
    newlesson
  }

  def copy(lessons: List[Lesson], params: TaskCopyParams): Seq[Lesson] = {
    val copiedTasks = Collections.newBuffer[Lesson]
    for (lesson <- lessons) {
      val copy = clone(lesson)
      if (!params.isCopyCourseTakes) {
        copy.teachClass.courseTakes.clear()
        copy.teachClass.stdCount = 0
      } else {
        for (take <- copy.teachClass.courseTakes) {
          take.electionMode = Model.newInstance(classOf[ElectionMode], ElectionMode.ASSIGEND)
        }
      }
      copy.state = States.Draft
      copy.semester = params.semester
      LessonElectionUtil.normalizeTeachClass(copy)
      copiedTasks += copy
    }
    for (copy <- copiedTasks) {
      lessonDao.saveOrUpdate(copy)
      lessonLogHelper.log(LessonLogBuilder.create(copy, "复制任务,生成任务"))
    }
    copiedTasks
  }

  def getLessons[T <: Entity[_]](semester: Semester, entity: T): Seq[Lesson] = {
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.semester =:semester", semester)
    val con = LessonLimitUtils.build(entity, "lgi")
    val params = con.params
    builder.where("exists(from lesson.teachClass.limitGroups lg join lg.items as lgi where" +
      con.content +
      ")", params(0), params(1), params(2))
    entityDao.search(builder)
  }

  def fillTeachers(teacherIds: Array[java.lang.Long], lesson: Lesson) {
    lesson.teachers.clear()
    if (teacherIds != null && teacherIds.length > 0) {
      lesson.teachers ++= (entityDao.find(classOf[User], teacherIds))
    }
  }
}
