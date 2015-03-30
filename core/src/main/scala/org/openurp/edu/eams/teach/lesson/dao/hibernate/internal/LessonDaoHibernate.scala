package org.openurp.edu.eams.teach.lesson.dao.hibernate.internal

import java.io.Serializable

import scala.collection.JavaConversions._

import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.page.Page
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.lang.annotation.description
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.beangle.data.jpa.hibernate.QuerySupport
import org.beangle.data.model.dao.Conditions
import org.hibernate.FlushMode
import org.hibernate.SessionFactory
import org.openurp.base.Semester
import org.openurp.edu.base.States
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator
import org.openurp.edu.eams.classroom.util.RoomUseridGenerator.Usage
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.lg.room.Occupancy

class LessonDaoHibernate(sf: SessionFactory) extends HibernateEntityDao(sf) with LessonDao {

  var lessonSeqNoGenerator: LessonSeqNoGenerator = _

  private def evictLessonRegion() {
    val cache = sessionFactory.getCache
    if (null != cache) {
      cache.evictEntityRegion(classOf[Lesson])
    }
  }

  def getLessonsByCategory(sid: Serializable,
    strategy: LessonFilterStrategy,
    semester: Semester,
    pageNo: Int,
    pageSize: Int): Page[Lesson] = {
    val params = Collections.newMap[String, Any]
    val id = if (strategy.name == "teacher") "%" + sid + "%" else sid

    params.put("id", id)
    params.put("semesterId", semester.id)
    val queryStr = strategy.queryString(null, " and task.semester.id= :semesterId ")
    val lessons = search(queryStr, params, new PageLimit(pageNo, pageSize), false)
    lessons.asInstanceOf[Page[Lesson]]
  }

  def getLessonsByCategory(id: Serializable, strategy: LessonFilterStrategy, semesters: Iterable[Semester]): Seq[Lesson] = {
    val taskQuery = strategy.createQuery(currentSession, "select distinct task.id from Lesson as task ",
      " and task.semester in (:semesters) ")
    taskQuery.setParameter("id", id)
    taskQuery.setParameterList("semesters", semesters)
    find(classOf[Lesson], taskQuery.list().toArray().asInstanceOf[Array[java.lang.Long]])
  }

  def getLessonsOfStd(stdId: Serializable, semesters: List[Semester]): Seq[Lesson] = {
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
    newParamsMap: collection.Map[String, Any]): String = {
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
    val newParamsMap = Collections.newMap[String, Any]
    val updateSql = getUpdateQueryString(attr, value, task, stdTypeIds, departIds, newParamsMap)
    val query = currentSession.createQuery(updateSql)
    QuerySupport.setParameters(query, newParamsMap)
    query.executeUpdate()
  }

  def countLesson(sid: Serializable, strategy: LessonFilterStrategy, semester: Semester): Int = {
    val countQuery = strategy.createQuery(currentSession, "select count(task.id) from TeachTask as task ",
      " and task.semester.id  = :semesterId")
    val id = if (strategy.name == "teacher") "%" + sid + "%" else sid
    countQuery.setParameter("id", id)
    countQuery.setParameter("semesterId", semester.id)
    val rsList = countQuery.list()
    rsList.get(0).asInstanceOf[Number].intValue()
  }

  def saveMergeResult(lessons: Array[Lesson], index: Int) {
    saveOrUpdate(lessons(index))
    for (i <- 0 until lessons.length) {
      if (i != index) {
        remove(lessons(i))
      }
    }
  }

  def remove(lesson: Lesson) {
    val removeEntities = Collections.newBuffer[Any]
    val occupancies = getOccupancies(lesson)
    removeEntities.addAll(occupancies)
    removeEntities.add(lesson)
    super.remove(removeEntities)
  }

  def getOccupancies(lesson: Lesson): Seq[Occupancy] = {
    val builder = OqlBuilder.from(classOf[Occupancy], "occupancy").where("occupancy.userid in( :lessonIds)",
      RoomUseridGenerator.gen(lesson, Usage.COURSE, Usage.EXAM))
    search(builder)
  }

  def saveGenResult(plan: MajorPlan,
    semester: Semester,
    lessons: List[Lesson],
    removeExists: Boolean) {
    currentSession.setFlushMode(FlushMode.COMMIT)
    lessonSeqNoGenerator.genLessonSeqNos(lessons)
    for (lesson <- lessons) {
      lesson.state = States.Draft
      super.saveOrUpdate(lesson)
    }
    currentSession.flush()
  }

  override def saveOrUpdate[E](entities: Iterable[E]): Unit = {
    for (entity <- entities) {
      val lesson = entity.asInstanceOf[Lesson]
      val iter = lesson.teachClass.limitGroups.iterator
      while (iter.hasNext) {
        if (Collections.isEmpty(iter.next().items)) {
          iter.remove()
        }
      }
      lessonSeqNoGenerator.genLessonSeqNo(lesson)
      super.saveOrUpdate(lesson)
    }
  }
}
