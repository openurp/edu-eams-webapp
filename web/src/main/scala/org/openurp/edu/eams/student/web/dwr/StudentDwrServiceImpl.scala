package org.openurp.edu.eams.student.web.dwr

import java.util.Date
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Department
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Student
import org.openurp.edu.base.StudentJournal

import scala.collection.JavaConversions._

class StudentDwrServiceImpl extends BaseServiceImpl {

  def getStudent(code: String): Student = {
    val query = OqlBuilder.from(classOf[Student], "student")
    query.where("student.code = :code", code)
    val list = entityDao.search(query)
    if (list.isEmpty) {
      null
    } else {
      val newOne = Model.newInstance(classOf[Student]).asInstanceOf[Student]
      val std = list.get(0).asInstanceOf[Student]
      entityDao.initialize(std.major.getName)
      entityDao.initialize(std.getAdminclass)
      entityDao.initialize(std.department.getName)
      val depart = Model.newInstance(classOf[Department]).asInstanceOf[Department]
      val major = Model.newInstance(classOf[Major]).asInstanceOf[Major]
      val direction = Model.newInstance(classOf[Direction]).asInstanceOf[Direction]
      try {
        depart.setName(std.department.getName)
        newOne.setDepartment(depart)
        major.setName(std.major.getName)
        newOne.setMajor(std.major)
        direction.setName(std.direction.getName)
        newOne.setDirection(std.direction)
      } catch {
        case e: Exception => 
      }
      newOne.setId(std.getId)
      newOne.setCode(std.getCode)
      newOne.setName(std.getName)
      newOne.setGrade(std.grade)
      newOne.setEffectiveAt(std.getEffectiveAt)
      newOne.setInvalidAt(std.getInvalidAt)
      newOne
    }
  }

  def getStudentMap(code: String): Map[_,_] = {
    val query = OqlBuilder.from(classOf[Student], "student")
    query.where("student.code = :code", code)
    val list = entityDao.search(query)
    val stdMap = new HashMap()
    if (list.isEmpty) {
      return null
    } else {
      val std = list.get(0).asInstanceOf[Student]
      val journals = std.getJournals
      val date = new Date()
      var isInSchool = false
      var iterator = journals.iterator()
      while (iterator.hasNext) {
        val journal = iterator.next()
        if (date.before(journal.getEndOn) && date.after(journal.getBeginOn)) {
          isInSchool = journal.isInschool
        }
      }
      stdMap.put("id", std.getId)
      stdMap.put("stdId", std.getId)
      stdMap.put("name", std.getName)
      stdMap.put("adminclass", if ((null != std.getAdminclass)) std.getAdminclass.getName else "")
      stdMap.put("major", if ((null != std.major)) std.major.getName else "")
      stdMap.put("grade", std.grade)
      stdMap.put("inSchool", new java.lang.Boolean(isInSchool))
      stdMap.put("department", if ((null != std.department)) std.department.getName else "")
    }
    stdMap
  }

  def getStudentObject(code: String): Student = getStudent(code)

  def getStudentDWR(code: String): Student = getStudent(code)
}
