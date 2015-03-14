package org.openurp.edu.eams.core.web.action

import java.util.ArrayList
import java.util.List
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.AdminclassService
import org.openurp.edu.eams.core.service.StudentService
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction

import scala.collection.JavaConversions._

class AdminclassStudentAction extends RestrictionSupportAction {

  protected var adminclassService: AdminclassService = _

  protected var studentService: StudentService = _

  def setAdminclassService(adminclassService: AdminclassService) {
    this.adminclassService = adminclassService
  }

  def setStudentService(studentService: StudentService) {
    this.studentService = studentService
  }

  def search(): String = {
    if (Strings.isEmpty(getDepartmentIdSeq) || Strings.isEmpty(getStdTypeIdSeq)) {
      return forwardError("对不起，您没有权限！")
    }
    val builder = OqlBuilder.from(classOf[Student], "student")
    populateConditions(builder)
    builder.join("left outer", "student.adminclass", "adminclass")
    builder.orderBy(get(Order.ORDER_STR)).limit(getPageLimit)
    put("students", entityDao.search(builder))
    forward("list")
  }

  def arrangeAdminclass(): String = {
    val stdIds = Strings.splitToLong(get("stdIds"))
    val query = OqlBuilder.from(classOf[Adminclass], "clazz")
    query.select("distinct clazz").newFrom("org.openurp.edu.base.Adminclass clazz, org.openurp.edu.base.Student std")
      .where("clazz.major = std.major")
      .where("(clazz.direction = std.direction or (clazz.direction is null and std.direction is null))")
      .where("std.id in (:stdIds)", stdIds)

    put("adminclasses", entityDao.search(query))
    put("students", entityDao.get(classOf[Student], stdIds))
    forward()
  }

  def detachAdminclass(): String = {
    val studentIds = Strings.splitToLong(get("stdIds"))
    val students = entityDao.get(classOf[Student], studentIds)
    for (student <- students) {
      val adminclass = student.getAdminclass
      if (adminclass == null) {
        //continue
      }
      student.setAdminclass(null)
      entityDao.saveOrUpdate(student)
      adminclass.setStdCount(adminclass.getStudents.size)
      entityDao.saveOrUpdate(adminclass)
    }
    redirect("search", "info.action.success", get("params"))
  }

  def attachAdminclass(): String = {
    val adminclass = entityDao.get(classOf[Adminclass], getInt("adminclassId"))
    val students = entityDao.get(classOf[Student], Strings.splitToLong(get("stdIds")))
    val notAddStudents = new ArrayList[Student]()
    val successAddStudents = new ArrayList[Student]()
    for (student <- students) {
      if (student.major != adminclass.major || student.direction != adminclass.direction) {
        notAddStudents.add(student)
      } else {
        successAddStudents.add(student)
        student.setAdminclass(adminclass)
        entityDao.saveOrUpdate(student)
      }
    }
    adminclass.setStdCount(adminclass.getStudents.size)
    entityDao.saveOrUpdate(adminclass)
    put("adminclass", adminclass)
    if (notAddStudents.size > 0) {
      put("noAddStudents", notAddStudents)
    }
    if (successAddStudents.size > 0) {
      put("successAddStudents", successAddStudents)
    }
    forward("attachAdminclassResult")
  }
}
