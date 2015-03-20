package org.openurp.edu.eams.teach.lesson.dao.hibernate.internal

import java.io.Serializable





import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.page.Page
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.query.builder.Conditions
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.hibernate.Cache
import org.hibernate.FlushMode
import org.hibernate.Query
import org.openurp.base.Semester
import org.openurp.edu.eams.classroom.Occupancy
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.lesson.ArrangeSuggest
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.dao.LessonPlanRelationDao
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.teach.plan.MajorPlan


class LessonDaoHibernate extends HibernateEntityDao with LessonDao {

  private var lessonSeqNoGenerator: LessonSeqNoGenerator = _

  private var lessonPlanRelationDao: LessonPlanRelationDao = _

  private def evictLessonRegion() {
    val cache = sessionFactory.cache
    if (null != cache) {
      cache.evictEntityRegion(classOf[Lesson])
    }
  }

  def getLessonsByCategory(id: Serializable, 
      strategy: LessonFilterStrategy, 
      semester: Semester, 
      pageNo: Int, 
      pageSize: Int): Page[Lesson] = {
    val params = new HashMap[String, Any](3)
    if (strategy.name == "teacher") {
      id = "%" + id + "%"
    }
    params.put("id", id)
    params.put("semesterId", semester.id)
    val queryStr = strategy.queryString(null, " and task.semester.id= :semesterId ")
    val lessons = search(queryStr, params, new PageLimit(pageNo, pageSize), false)
    lessons.asInstanceOf[Page[Lesson]]
  }

  def getLessonsByCategory(id: Serializable, strategy: LessonFilterStrategy, semesters: Iterable[Semester]): List[Lesson] = {
    val taskQuery = strategy.createQuery(getSession, "select distinct task.id from Lesson as task ", 
      " and task.semester in (:semesters) ")
    taskQuery.parameter="id", id
    taskQuery.parameterList="semesters", semesters
    get(classOf[Lesson], taskQuery.list())
  }

  def getLessonsOfStd(stdId: Serializable, semesters: List[Semester]): List[Lesson] = {
    val queryBuilder = OqlBuilder.from(classOf[Lesson], "lesson")
    queryBuilder.join("lesson.teachClass.courseTakes", "courseTake")
    queryBuilder.where("courseTake.std.id =:stdId", stdId)
    queryBuilder.where("lesson.semester in (:semesters)", semesters)
    search(queryBuilder)
  }

  def updateLessonByCategory(attr: String, 
      value: AnyRef, 
      id: java.lang.Long, 
      strategy: LessonFilterStrategy, 
      semester: Semester): Int = {
    evictLessonRegion()
    val queryStr = strategy.queryString("update TeachTask set " + attr + " = :value ", " and semester.id = :semesterId")
    executeUpdate(queryStr, Array(value, semester.id))
  }

  private def getUpdateQueryString(attr: String, 
      value: AnyRef, 
      task: Lesson, 
      stdTypeIds: Array[Integer], 
      departIds: Array[Long], 
      newParamsMap: Map[String, Any]): String = {
    val entityQuery = OqlBuilder.from(classOf[Lesson], "task")
    entityQuery.where(Conditions.extractConditions("task", task))
    if (null != stdTypeIds && 0 != stdTypeIds.length) {
      entityQuery.where("task.teachClass.stdType.id in (:stdTypeIds) ", stdTypeIds)
    }
    if (null != departIds && 0 != departIds.length) {
      entityQuery.where("task.teachDepart.id in (:departIds) ", departIds)
    }
    val updateSql = new StringBuffer("update TeachTask set " + attr + "=(:" + attr + ") where id in (")
    updateSql.append(entityQuery.build().statement).append(")")
    newParamsMap.put(attr, value)
    newParamsMap.putAll(entityQuery.params)
    updateSql.toString
  }

  def updateLessonByCriteria(attr: String, 
      value: AnyRef, 
      task: Lesson, 
      stdTypeIds: Array[Integer], 
      departIds: Array[Long]): Int = {
    evictLessonRegion()
    val newParamsMap = CollectUtils.newHashMap()
    val updateSql = getUpdateQueryString(attr, value, task, stdTypeIds, departIds, newParamsMap)
    val query = getSession.createQuery(updateSql)
    QuerySupport.parameter=query, newParamsMap
    query.executeUpdate()
  }

  def countLesson(id: Serializable, strategy: LessonFilterStrategy, semester: Semester): Int = {
    val countQuery = strategy.createQuery(getSession, "select count(task.id) from TeachTask as task ", 
      " and task.semester.id  = :semesterId")
    if (strategy.name == "teacher") id = "%" + id + "%"
    countQuery.parameter="id", id
    countQuery.parameter="semesterId", semester.id
    val rsList = countQuery.list()
    rsList.get(0).asInstanceOf[Number].intValue()
  }

  def saveMergeResult(lessons: Array[Lesson], index: Int) {
    saveOrUpdate(lessons(index))
    for (i <- 0 until lessons.length) {
      if (i == index) {
        //continue
      }
      remove(lessons(i))
    }
  }

  def remove(lesson: Lesson) {
    val removeEntities = CollectUtils.newArrayList()
    val occupancies = getOccupancies(lesson)
    removeEntities.addAll(occupancies)
    val relations = lessonPlanRelationDao.relations(lesson)
    removeEntities.addAll(relations)
    val lessonMaterials = get(classOf[LessonMaterial], "lesson", lesson)
    removeEntities.addAll(lessonMaterials)
    val suggests = get(classOf[ArrangeSuggest], "lesson", lesson)
    removeEntities.addAll(suggests)
    removeEntities.add(lesson)
    super.remove(removeEntities)
  }

  def getOccupancies(lesson: Lesson): List[Occupancy] = {
    val builder = OqlBuilder.from(classOf[Occupancy], "occupancy").where("occupancy.userid in( :lessonIds)", 
      RoomUseridGenerator.gen(lesson, Usage.COURSE, Usage.EXAM))
    search(builder)
  }

  def saveGenResult(plan: MajorPlan, 
      semester: Semester, 
      lessons: List[Lesson], 
      removeExists: Boolean) {
    if (removeExists) {
      val existsLessons = lessonPlanRelationDao.relatedLessons(plan, semester)
      for (lesson <- existsLessons) {
        remove(lesson)
      }
    }
    getSession.flushMode=FlushMode.COMMIT
    lessonSeqNoGenerator.genLessonSeqNos(lessons)
    for (lesson <- lessons) {
      lesson.auditStatus=CommonAuditState.UNSUBMITTED
      super.saveOrUpdate(lesson)
      lessonPlanRelationDao.saveRelation(plan, lesson)
    }
    getSession.flush()
  }

  def saveOrUpdate(lesson: Lesson) {
    val iter = lesson.teachClass.limitGroups.iterator()
    while (iter.hasNext) {
      if (CollectUtils.isEmpty(iter.next().items)) {
        iter.remove()
      }
    }
    lessonSeqNoGenerator.genLessonSeqNo(lesson)
    super.saveOrUpdate(lesson)
  }

  def setLessonSeqNoGenerator(lessonSeqNoGenerator: LessonSeqNoGenerator) {
    this.lessonSeqNoGenerator = lessonSeqNoGenerator
  }

  def setLessonPlanRelationDao(lessonPlanRelationDao: LessonPlanRelationDao) {
    this.lessonPlanRelationDao = lessonPlanRelationDao
  }
}
