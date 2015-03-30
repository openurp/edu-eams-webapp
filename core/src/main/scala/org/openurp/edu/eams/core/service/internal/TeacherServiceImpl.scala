package org.openurp.edu.eams.core.service.internal

import java.sql.Date

import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.TeacherService
import org.openurp.edu.eams.core.service.event.TeacherCreationEvent

class TeacherServiceImpl extends BaseServiceImpl with TeacherService {

  def getTeacher(code: String): Teacher = {
    if (Strings.isBlank(code)) {
      return null
    }
    val it = entityDao.findBy(classOf[Teacher], "code", code).iterator
    if (it.hasNext) it.next() else null
  }

  def getTeacherNamesByDepart(departmentId: java.lang.Integer): Seq[Array[Any]] = {
    if (null == departmentId) return List.empty
    val builder = OqlBuilder.from(classOf[Teacher].getName + " teacher")
    builder.where("teacher.department.id=:departmentId", departmentId)
    builder.select("teacher.id,teacher.name")
    entityDao.search(builder)
  }

  def getTeacherById(id: java.lang.Long): Teacher = entityDao.get(classOf[Teacher], id)

  def getTeacherByNO(teacherNO: String): Teacher = {
    if (Strings.isEmpty(teacherNO)) null else {
      val teachers = entityDao.findBy(classOf[Teacher], "code", teacherNO)
      if (teachers.isEmpty) null else teachers(0)
    }
  }

  def getTeachersByDepartment(departIds: String): Seq[Teacher] = {
    if (Strings.isEmpty(departIds))
      List.empty
    else
      getTeachersByDepartment(Strings.transformToLong(Strings.split(departIds)))
  }

  def getTeachersByDepartment(departIds: Array[java.lang.Long]): Seq[Teacher] = {
    if (null == departIds || departIds.length < 1) List.empty else entityDao.findBy(classOf[Teacher],
      "department.id", departIds)
  }

  def getTeachersById(teacherIds: Array[java.lang.Long]): Seq[Teacher] = {
    if (null == teacherIds || teacherIds.length < 1) List.empty else entityDao.findBy(classOf[Teacher],
      "id", teacherIds)
  }

  def getTeachersById(teacherIds: Iterable[_]): Seq[Teacher] = {
    if (!teacherIds.isEmpty) entityDao.findBy(classOf[Teacher], "id", teacherIds) else List.empty
  }

  override def getTeachersByNO(teacherNOs: Array[String]): Seq[Teacher] = {
    if (null == teacherNOs || teacherNOs.length < 1) List.empty
    else entityDao.findBy(classOf[Teacher], "code", teacherNOs)
  }

  def removeTeacher(id: java.lang.Long) {
    if (null == id) return
    entityDao.remove(entityDao.get(classOf[Teacher], java.lang.Long.valueOf(id)))
  }

  def saveOrUpdate(teacher: Teacher) {
    val newTeacher = !teacher.persisted
    //    teacher.updatedAt = new Date(System.currentTimeMillis())
    entityDao.saveOrUpdate(teacher)
    if (newTeacher) publish(new TeacherCreationEvent(teacher))
  }
}
