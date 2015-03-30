package org.openurp.edu.eams.core.service.internal

import java.sql.Date
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Throwables
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.AdminclassService
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.collection.Collections
import org.beangle.commons.logging.Logging

class AdminclassServiceImpl extends BaseServiceImpl with AdminclassService with Logging {

  def getAdminclass(id: java.lang.Long): Adminclass = {
    entityDao.get(classOf[Adminclass], id).asInstanceOf[Adminclass]
  }

  def getAdminclass(code: String): Adminclass = {
    val query = OqlBuilder.from(classOf[Adminclass], "adminclass")
    query.where("adminclass.code=:code", code)
    val rs = entityDao.search(query)
    if (rs.isEmpty) null else rs(0).asInstanceOf[Adminclass]
  }

  def removeAdminclass(id: java.lang.Long) {
    if (null == id) return
    entityDao.remove(entityDao.get(classOf[Adminclass], id))
  }

  def saveOrUpdate(adminclass: Adminclass) {
    //    if (null == adminclass.createdAt) {
    //      adminclass.createdAt=new Date(System.currentTimeMillis())
    //    }
    //    if (!adminclass.isPersisted) {
    //      adminclass.createdAt=new Date(System.currentTimeMillis())
    //    }
    //    adminclass.updatedAt=new Date(System.currentTimeMillis())
    entityDao.saveOrUpdate(adminclass)
  }

  def updateActualStdCount(adminclassId: java.lang.Long): Int = {
    val stdCount = 0
    val updateHQL = "update Adminclass cls set cls.actualStdCount=(\n" +
      "select count(std.id) from Adminclass class1 join class1.students std where class1.id=cls.id and std.inSchool=1\n" +
      ") where cls.id=:id"
    val params = Collections.newMap[Any, Any]
    params.put("id", adminclassId)
    try {
      entityDao.executeUpdate(updateHQL, params)
    } catch {
      case e: RuntimeException => {
        info("execproduct is failed" + "in update_classactualstdcount" + Throwables.stackTrace(e))
        throw e
      }
    }
    stdCount
  }

  def updateStdCount(adminclassId: java.lang.Long): Int = {
    val stdCount = 0
    val updateHQL = "update Adminclass cls set cls.stdCount=(" +
      "select count(std.id) from Adminclass class1 join class1.students std where class1.id=cls.id and std.active=1\n" +
      ") where cls.id=:id"
    val params = Collections.newMap[Any, Any]
    params.put("id", adminclassId)
    try {
      entityDao.executeUpdate(updateHQL, params)
    } catch {
      case e: RuntimeException => {
        info("execproduct is failed" + "in update_classstdcount" + Throwables.stackTrace(e))
        throw e
      }
    }
    stdCount
  }

  def batchUpdateStdCountOfClass(adminclassIdSeq: String) {
    val adminclassIds = Strings.transformToInt(Strings.split(adminclassIdSeq))
    if (null != adminclassIds) {
      for (i <- 0 until adminclassIds.length) {
        updateActualStdCount(adminclassIds(i))
        updateStdCount(adminclassIds(i))
      }
    }
  }

  def batchUpdateStdCountOfClass(adminclassIds: Array[Integer]) {
    if (null != adminclassIds) {
      for (i <- 0 until adminclassIds.length) {
        updateActualStdCount(adminclassIds(i))
        updateStdCount(adminclassIds(i))
      }
    }
  }

  def batchAddStudentClass(students: List[_], adminclasses: List[_]) {
    var iterator = adminclasses.iterator
    while (iterator.hasNext) {
      val adminclass = iterator.next().asInstanceOf[Adminclass]
      val studentSet = adminclass.students
      var iter = students.iterator
      while (iter.hasNext) {
        val student = iter.next().asInstanceOf[Student]
        if (!studentSet.contains(student)) {
          studentSet += (student)
        }
      }
    }
    entityDao.saveOrUpdate(students)
    entityDao.saveOrUpdate(adminclasses)
  }

  def batchRemoveStudentClass(students: Seq[_], adminclasses: Seq[_]) {
    var iterator = adminclasses.iterator
    while (iterator.hasNext) {
      val adminclass = iterator.next().asInstanceOf[Adminclass]
      val studentSet = adminclass.students
      var iter = students.iterator
      while (iter.hasNext) {
        val student = iter.next().asInstanceOf[Student]
        if (studentSet.contains(student)) {
          studentSet -= (student)
        }
      }
    }
    entityDao.saveOrUpdate(students)
    entityDao.saveOrUpdate(adminclasses)
  }

  def updateStudentAdminclass(std: Student, adminclasses: Iterable[_], project: Project) {
    val orig = EntityUtils.extractIds(adminclasses)
    val dest = EntityUtils.extractIds(List(std.adminclass))
    val addClassList = Collections.subtract(orig, dest)
    val subClassList = Collections.subtract(dest, orig)
    batchRemoveStudentClass(List(std), entityDao.findBy(classOf[Adminclass], "id",  subClassList.toArray))
    batchAddStudentClass(List(std), entityDao.findBy(classOf[Adminclass], "id", addClassList.toArray))
  }
}
