package org.openurp.edu.eams.core.service.internal

import java.sql.Date
import java.util.Collection
import java.util.Collections
import java.util.Iterator
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.TeacherService
import org.openurp.edu.eams.core.service.event.TeacherCreationEvent

import scala.collection.JavaConversions._

class TeacherServiceImpl extends BaseServiceImpl with TeacherService {

  def getTeacher(code: String): Teacher = {
    if (Strings.isBlank(code)) {
      return null
    }
    val it = entityDao.get(classOf[Teacher], "code", code).iterator()
    if (it.hasNext) it.next() else null
  }

  def getTeacherNamesByDepart(departmentId: java.lang.Integer): List[Array[Any]] = {
    if (null == departmentId) return CollectUtils.newArrayList(0)
    val builder = OqlBuilder.from(classOf[Teacher].getName + " teacher")
    builder.where("teacher.department.id=:departmentId", departmentId)
    builder.select("teacher.id,teacher.name")
    entityDao.search(builder)
  }

  def getTeacherById(id: java.lang.Long): Teacher = entityDao.get(classOf[Teacher], id)

  def getTeacherByNO(teacherNO: String): Teacher = {
    if (Strings.isEmpty(teacherNO)) null else {
      val teachers = entityDao.get(classOf[Teacher], "code", teacherNO)
      if (teachers.isEmpty) null else teachers.get(0)
    }
  }

  def getTeachersByDepartment(departIds: String): List[Teacher] = {
    if (Strings.isEmpty(departIds)) Collections.emptyList() else getTeachersByDepartment(Strings.transformToLong(Strings.split(departIds)))
  }

  def getTeachersByDepartment(departIds: Array[Long]): List[Teacher] = {
    if (null == departIds || departIds.length < 1) Collections.emptyList() else entityDao.get(classOf[Teacher], 
      "department.id", departIds)
  }

  def getTeachersById(teacherIds: Array[Long]): List[Teacher] = {
    if (null == teacherIds || teacherIds.length < 1) Collections.emptyList() else entityDao.get(classOf[Teacher], 
      "id", teacherIds)
  }

  def getTeachersById(teacherIds: Collection[_]): List[Teacher] = {
    if (!teacherIds.isEmpty) entityDao.get(classOf[Teacher], "id", teacherIds) else Collections.emptyList()
  }

  def getTeachersByNO(teacherNOs: Array[String]): List[Teacher] = {
    if (null == teacherNOs || teacherNOs.length < 1) Collections.emptyList() else {
      entityDao.get(classOf[Teacher], "code", teacherNOs)
    }
  }

  def removeTeacher(id: java.lang.Long) {
    if (null == id) return
    entityDao.remove(entityDao.get(classOf[Teacher], java.lang.Long.valueOf(id)))
  }

  def saveOrUpdate(teacher: Teacher) {
    val newTeacher = !teacher.isPersisted
    if (!teacher.isPersisted) teacher.setCreatedAt(new Date(System.currentTimeMillis()))
    teacher.setUpdatedAt(new Date(System.currentTimeMillis()))
    entityDao.saveOrUpdate(teacher)
    if (newTeacher) publish(new TeacherCreationEvent(teacher))
  }
}
