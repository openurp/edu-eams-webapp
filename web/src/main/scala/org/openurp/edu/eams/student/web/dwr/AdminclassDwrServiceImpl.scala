package org.openurp.edu.eams.student.web.dwr



import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass



class AdminclassDwrServiceImpl extends BaseServiceImpl {

  def getAdminclasses(grade: String, 
      stdTypeId: java.lang.Long, 
      departmentId: java.lang.Integer, 
      majorId: java.lang.Long, 
      directionId: java.lang.Long): List[_] = {
    val query = OqlBuilder.from(classOf[Adminclass], "adminClass")
    if (Strings.isEmpty(grade)) {
      return null
    }
    query.where("adminClass.grade = (:grade)", grade)
    if (null == stdTypeId) {
      return null
    }
    query.where("adminClass.stdType.id = (:stdTypeId)", stdTypeId)
    if (null == departmentId) {
      return null
    }
    query.where("adminClass.department.id = (:departmentId)", departmentId)
    if (null == majorId || majorId.intValue() == 0) {
      query.where("adminClass.major.id is null")
    } else {
      query.where("adminClass.major.id = (:majorId)", majorId)
    }
    if (null == directionId || directionId.intValue() == 0) {
      query.where("adminClass.direction.id is null")
    } else {
      query.where("adminClass.direction.id = (:directionId)", directionId)
    }
    entityDao.search(query).asInstanceOf[List[_]]
  }

  def getAdminclassNames(grade: String, 
      educationId: java.lang.Long, 
      departId: java.lang.Long, 
      majorId: java.lang.Long, 
      directionId: java.lang.Long): List[_] = {
    val params = new HashMap()
    var hql = "select d.id, d.name from Adminclass as d " + "where d.enabled=true"
    if (!Strings.isEmpty(grade)) {
      params.put("grade", grade)
      hql += " and d.grade=:grade"
    }
    if (null != educationId) {
      params.put("educationId", educationId)
      hql += " and d.education.id=:educationId"
    }
    if (null != departId) {
      params.put("departId", departId)
      hql += " and d.department.id=:departId"
    }
    if (null != majorId && majorId.intValue() != 0) {
      params.put("majorId", majorId)
      hql += " and d.major.id=:majorId"
    }
    if (null != directionId && directionId.intValue() != 0) {
      params.put("directionId", directionId)
      hql += " and d.direction.id=:directionId"
    }
    hql += " order by d.name"
    val query = OqlBuilder.from(hql)
    query.params(params)
    entityDao.search(query).asInstanceOf[List[_]]
  }
}
