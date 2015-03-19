package org.openurp.edu.eams.baseinfo.web.dwr


import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Department
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education



class DepartmentDwrServiceImpl extends BaseServiceImpl {

  def getMajor(departmentId: java.lang.Integer, educationId: java.lang.Integer): List[_] = {
    val depart = entityDao.get(classOf[Department], departmentId).asInstanceOf[Department]
    var rs = Collections.EMPTY_LIST
    if (null != depart) {
      val departs = new ArrayList()
      departs.add(depart)
      departs.addAll(depart.getChildren)
      val query = OqlBuilder.from(classOf[Major], "major")
      query.where("major.enabled=true")
      query.where("exists(from major.journals md where md.depart in (:departs))", departs)
      if (null != educationId) {
        query.join("major.educations", "education")
        query.where("education=:education", new Education(educationId))
      }
      rs = entityDao.search(query).asInstanceOf[List[_]]
    }
    rs
  }
}
