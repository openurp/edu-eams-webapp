package org.openurp.edu.eams.teach.service.internal

import java.sql.Date

import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.service.CourseService

class CourseServiceImpl extends BaseServiceImpl with CourseService {

  def getCourse(code: String): Course = {
    val query = OqlBuilder.from(classOf[Course], "course").cacheable()
      .where("course.code = :code", code)
    val courses = entityDao.search(query)
    if (Collections.isNotEmpty(courses)) {
      return courses(0)
    }
    null
  }
 
  def saveOrUpdate(course: Course) {
    saveSetting(course)
    entityDao.saveOrUpdate(course)
  }

  private def saveSetting(course: Course) {
//    if (!course.isPersisted) {
//      course.createdAt = new Date(System.currentTimeMillis())
//    }
//    course.updatedAt = new Date(System.currentTimeMillis())
//    if (null == course.createdAt) {
//      course.createdAt = new Date(System.currentTimeMillis())
//    }
  }

  def getCourseByCodes(courseCodes: String): Array[String] = {
    if (Strings.isNotEmpty(courseCodes)) {
      val query = OqlBuilder.from(classOf[Course]).where("code  in (:codes)", Strings.split(courseCodes.toUpperCase(),
        ','))
      val list = entityDao.search(query).asInstanceOf[List[Course]]
      val courseinfos = Array.ofDim[String](list.size)
      if (!list.isEmpty) {
        var i = 0
        var iter = list.iterator
        while (iter.hasNext) {
          val course = iter.next().asInstanceOf[Course]
          courseinfos(i) = course.name + "(" + course.credits + ")"
          i += 1
        }
      }
      return courseinfos
    }
    null
  }

  def getCourseByIdDwr(id: java.lang.Long): Course = {
    val list = entityDao.search(OqlBuilder.from(classOf[Course], "course").where("course.id=:id", id)
      .cacheable())
    if (Collections.isEmpty(list)) {
      return null
    }
    list(0)
  }

  def searchCoursesByCodeOrName(codeOrName: String): Seq[Course] = {
    val like = '%' + codeOrName + '%'
    if (Strings.isNotBlank(codeOrName) && codeOrName.contains("(")) {
      val str = codeOrName.split("\\(")
      if (str.length > 1) {
        var code = str(1).trim()
        if (code.endsWith(")")) {
          code = code.substring(0, code.length - 1)
        }
        return entityDao.search(OqlBuilder.from(classOf[Course], "course").where("(course.name like :name and course.code like :code) or course.code like :codeOrName or course.name like :codeOrName",
          "%" + str(0).trim() + "%", "%" + code + "%", like)
          .where("course.enabled is true")
          .orderBy("course.code")
          .orderBy("course.name")
          .limit(1, 10))
      }
    }
    entityDao.search(OqlBuilder.from(classOf[Course], "course").where("course.code like :code or course.name like :code",
      like)
      .where("course.enabled is true")
      .orderBy("course.code")
      .orderBy("course.name")
      .limit(1, 10))
  }

  def searchCourseByProjectAndCodeOrName(studentCode: String, codeOrName: String, projectId: String): Seq[Course] = {
    val like = '%' + codeOrName + '%'
    val proId = java.lang.Long.parseLong(projectId)
    entityDao.search(OqlBuilder.from(classOf[Course], "course").where("course.code like :code or course.name like :code",
      like)
      .where("course.project.id=:projectId", proId)
      .orderBy("course.code")
      .orderBy("course.name")
      .limit(1, 10))
  }
}
