package org.openurp.edu.eams.teach.lesson.service.internal

import java.io.Serializable

import java.util.Arrays


import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
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
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.service.CourseLimitUtils
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.service.TaskCopyParams
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil



class LogHelper {

  def info(s: String) {
  }
}

class LessonServiceImpl extends BaseServiceImpl with LessonService {

  private var lessonDao: LessonDao = _

  private var lessonLogHelper: LessonLogHelper = _

  def teachDepartsOfSemester(projects: List[Project], departments: List[Department], semester: Semester): List[Department] = {
    if (CollectUtils.isNotEmpty(projects) && CollectUtils.isNotEmpty(departments)) {
      val query = OqlBuilder.from(classOf[Lesson].getName + " lesson")
      query.select("distinct(lesson.teachDepart)")
      query.where(" lesson.semester=:semester and lesson.teachDepart in (:departments) and lesson.project in (:projects) ", 
        semester, departments, projects)
      entityDao.search(query)
    } else {
      CollectUtils.newArrayList(0)
    }
  }

  def courseTypesOfSemester(projects: List[Project], departments: List[Department], semester: Semester): List[CourseType] = {
    if (CollectUtils.isNotEmpty(projects) && CollectUtils.isNotEmpty(departments)) {
      val query = OqlBuilder.from(classOf[Lesson].getName + " lesson")
      query.select("distinct(lesson.courseType)")
      query.where(" lesson.semester=:semester and lesson.teachDepart in (:departments) and lesson.project in (:projects) ", 
        semester, departments, projects)
      entityDao.search(query)
    } else {
      CollectUtils.newArrayList(0)
    }
  }

  def attendDepartsOfSemester(projects: List[Project], semester: Semester): List[Department] = {
    if (CollectUtils.isNotEmpty(projects)) {
      val qq = OqlBuilder.from(classOf[Lesson].getName + " lesson")
      qq.join("lesson.teachClass.limitGroups", "lgroup").join("lgroup.items", "litem")
        .where("litem.meta.id = :metaid", CourseLimitMetaEnum.DEPARTMENT.getMetaId)
        .where("lesson.semester = :semester", semester)
        .where("lesson.project in (:projects)", projects)
      qq.select("litem").cacheable()
      val limitItems = entityDao.search(qq)
      val sb = new StringBuilder()
      for (item <- limitItems) {
        val ids = item.getContent.split(",")
        sb.append(Strings.join(ids, ",")).append(',')
      }
      val departmentIds = Strings.splitToInt(sb.toString)
      val distinctIds = new ArrayList[Integer]()
      Arrays.sort(departmentIds)
      var prev = -1
      for (i <- 0 until departmentIds.length) {
        if (prev != departmentIds(i)) {
          distinctIds.add(departmentIds(i))
        }
        prev = departmentIds(i)
      }
      if (CollectUtils.isEmpty(distinctIds)) {
        return new ArrayList[Department]()
      }
      entityDao.get(classOf[Department], distinctIds)
    } else {
      CollectUtils.newArrayList(0)
    }
  }

  def canAttendDepartsOfSemester(projects: List[Project], departments: List[Department], semester: Semester): List[Department] = {
    if (CollectUtils.isNotEmpty(projects) && CollectUtils.isNotEmpty(departments)) {
      val query = OqlBuilder.from(classOf[Department], "department")
      query.where("exists (from org.openurp.edu.teach.plan.MajorPlan plan" + 
        " where plan.program.department=department and plan.program.major.project in (:projects)" + 
        " and plan.program.department in (:departs)" + 
        " and current_date() >= plan.program.effectiveOn and (plan.program.invalidOn is null or current_date() <= plan.program.invalidOn))", 
        semester, departments, projects)
      entityDao.search(query)
    } else {
      CollectUtils.newArrayList(0)
    }
  }

  def getProjectsForTeacher(teacher: Teacher): List[Project] = {
    val query = OqlBuilder.from(classOf[Lesson], "lesson").select("lesson.project")
      .join("lesson.teachers", "teacher")
      .where("teacher = :teacher", teacher)
    entityDao.search(query).asInstanceOf[List[Project]]
  }

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semesters: Iterable[Semester]): List[Lesson] = {
    if (null == id || semesters.isEmpty) {
      CollectUtils.newArrayList(0)
    } else {
      val prefix = "select distinct lesson.id from org.openurp.edu.teach.lesson.Lesson as lesson "
      val postfix = " and lesson.semester in (:semesters) "
      val query = OqlBuilder.hql(prefix + strategy.getFilterString + postfix)
      query.param("id", id)
      query.param("semesters", semesters)
      val lessonIds = entityDao.search(query)
      var rs = CollectUtils.newArrayList()
      if (CollectUtils.isNotEmpty(lessonIds)) {
        rs = entityDao.get(classOf[Lesson], lessonIds.toArray(Array.ofDim[Long](0)))
      }
      rs
    }
  }

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semester: Semester): List[Lesson] = {
    if (null == id || null == semester.id) {
      CollectUtils.newArrayList(0)
    } else {
      getLessonByCategory(id, strategy, Collections.singletonList(semester))
    }
  }

  def copy(lessons: List[Lesson], params: TaskCopyParams): List[Lesson] = {
    val copiedTasks = new ArrayList[Lesson]()
    for (lesson <- lessons) {
      val copy = lesson.clone()
      if (!params.isCopyCourseTakes) {
        copy.getTeachClass.getCourseTakes.clear()
        copy.getTeachClass.setStdCount(0)
      } else {
        for (take <- copy.getTeachClass.getCourseTakes) {
          take.setElectionMode(Model.newInstance(classOf[ElectionMode], ElectionMode.ASSIGEND))
        }
      }
      copy.setAuditStatus(CommonAuditState.UNSUBMITTED)
      copy.setSemester(params.getSemester)
      LessonElectionUtil.normalizeTeachClass(copy)
      copiedTasks.add(copy)
    }
    for (copy <- copiedTasks) {
      lessonDao.saveOrUpdate(copy)
      lessonLogHelper.log(LessonLogBuilder.create(copy, "复制任务,生成任务"))
    }
    copiedTasks
  }

  def getLessons[T <: Entity[_]](semester: Semester, entity: T): List[Lesson] = {
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.semester =:semester", semester)
    val con = CourseLimitUtils.build(entity, "lgi")
    val params = con.getParams
    builder.where("exists(from lesson.teachClass.limitGroups lg join lg.items as lgi where" + 
      con.getContent + 
      ")", params.get(0), params.get(1), params.get(2))
    entityDao.search(builder)
  }

  def fillTeachers(teacherIds: Array[Long], lesson: Lesson) {
    lesson.getTeachers.clear()
    if (teacherIds != null && teacherIds.length > 0) {
      lesson.getTeachers.addAll(new HashSet[Teacher](entityDao.get(classOf[Teacher], teacherIds)))
    }
  }

  def setLessonDao(lessonDao: LessonDao) {
    this.lessonDao = lessonDao
  }

  def setLessonLogHelper(lessonLogHelper: LessonLogHelper) {
    this.lessonLogHelper = lessonLogHelper
  }
}
