package org.openurp.edu.eams.teach.schedule.dao.hibernate



import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.hibernate.Query
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.schedule.dao.CourseActivityDao
import org.hibernate.SessionFactory
import collection.JavaConversions._

class CourseActivityDaoHibernate(sf:SessionFactory) extends HibernateEntityDao(sf) with CourseActivityDao {

  def removeActivities(lessons: Iterable[Lesson]) {
    val getJSZYHQL = "select jszy.id from RoomOccupation jszy where jszy.id in (select kchd.roomOccupation.id from CourseActivity kchd where kchd.task in (:tasks))"
    var query = currentSession.createQuery(getJSZYHQL)
    query.setParameterList("tasks", lessons)
    val res = query.list()
    val jszhIds = Collections.newBuffer[Any]
    for (i <- 0 until res.size) {
      jszhIds += res.get(i).asInstanceOf[java.lang.Long]
    }
    val deleteKCHDHQL = "delete from CourseActivity where  task in (:tasks) "
    query = currentSession.createQuery(deleteKCHDHQL)
    query.setParameterList("tasks", lessons)
    query.executeUpdate()
    if (jszhIds.size > 0) {
      val deleteJSZYHQL = "delete from RoomOccupation jszy where jszy.id in (:jszyIds)"
      query = currentSession.createQuery(deleteJSZYHQL)
      query.setParameterList("jszyIds", jszhIds)
      query.executeUpdate()
    }
    val taskIdList = EntityUtils.extractIds(lessons)
    val taskIds = Array.ofDim[Long](taskIdList.size)
    taskIdList.toArray(taskIds)
    for (task <- lessons) {
      currentSession.refresh(task)
    }
  }
}
