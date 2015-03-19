package org.openurp.edu.eams.teach.schedule.dao.hibernate



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.util.EntityUtils
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.hibernate.Query
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.schedule.dao.CourseActivityDao



class CourseActivityDaoHibernate extends HibernateEntityDao with CourseActivityDao {

  def removeActivities(lessons: Iterable[Lesson]) {
    val getJSZYHQL = "select jszy.id from RoomOccupation jszy where jszy.id in (select kchd.roomOccupation.id from CourseActivity kchd where kchd.task in (:tasks))"
    var query = getSession.createQuery(getJSZYHQL)
    query.setParameterList("tasks", lessons)
    val res = query.list()
    val jszhIds = CollectUtils.newArrayList()
    for (i <- 0 until res.size) {
      jszhIds.add(res.get(i).asInstanceOf[java.lang.Long])
    }
    val deleteKCHDHQL = "delete from CourseActivity where  task in (:tasks) "
    query = getSession.createQuery(deleteKCHDHQL)
    query.setParameterList("tasks", lessons)
    query.executeUpdate()
    if (jszhIds.size > 0) {
      val deleteJSZYHQL = "delete from RoomOccupation jszy where jszy.id in (:jszyIds)"
      query = getSession.createQuery(deleteJSZYHQL)
      query.setParameterList("jszyIds", jszhIds)
      query.executeUpdate()
    }
    val taskIdList = EntityUtils.extractIds(lessons)
    val taskIds = Array.ofDim[Long](taskIdList.size)
    taskIdList.toArray(taskIds)
    for (task <- lessons) {
      getSession.refresh(task)
    }
  }
}
