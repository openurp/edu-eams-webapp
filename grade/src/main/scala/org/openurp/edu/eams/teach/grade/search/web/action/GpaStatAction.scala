package org.openurp.edu.eams.teach.grade.search.web.action

import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Major
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class GpaStatAction extends SemesterSupportAction {

  def adminclassIndex(): String = forward()

  def adminclassSearch(): String = {
    val query = OqlBuilder.from(classOf[Adminclass], "adminclass")
    populateConditions(query)
    query.where("adminclass.project =:project", getProject)
    query.where("exists (from " + classOf[StdGpa].getName + 
      " stdGpa where stdGpa.std.adminclass = adminclass and stdGpa.gpa is not null )")
    query.orderBy(Order.parse(get("orderBy"))).limit(getPageLimit)
    put("adminclasses", entityDao.search(query))
    "adminclassList"
  }

  def adminclassRanking(): String = {
    val adminclass = entityDao.get(classOf[Adminclass], getInt("adminclass.id"))
    val builder = OqlBuilder.from(classOf[StdGpa], "stdGpa")
    val isMinor = adminclass.major.getProject.isMinor
    if (isMinor) {
      builder.where("exists(from " + classOf[Student].getName + " std where std.adminclass=:adminclass)", 
        adminclass)
    } else {
      builder.where("stdGpa.std.adminclass = :adminclass", adminclass)
    }
    builder.where("stdGpa.gpa is not null")
    builder.where("stdGpa.minor=:minor", isMinor)
    builder.orderBy("stdGpa.gpa desc")
    put("rankings", entityDao.search(builder))
    forward()
  }

  def majorIndex(): String = adminclassIndex()

  def majorSearch(): String = {
    val query = OqlBuilder.from(classOf[Major], "major")
    populateConditions(query)
    query.where("major.project =:project", getProject)
    query.where("exists (from " + classOf[StdGpa].getName + 
      " stdGpa where stdGpa.std.major = major and stdGpa.gpa is not null )")
    query.orderBy(Order.parse(get("orderBy"))).limit(getPageLimit)
    put("majors", entityDao.search(query))
    "majorList"
  }

  def majorRanking(): String = {
    val query = OqlBuilder.from(classOf[StdGpa], "stdGpa")
    query.where("stdGpa.std.major.id = :majorId", getLong("major.id"))
    query.where("stdGpa.gpa is not null")
    query.orderBy("stdGpa.gpa desc").limit(getPageLimit)
    put("rankings", entityDao.search(query))
    forward()
  }
}
