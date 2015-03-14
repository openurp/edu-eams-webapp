package org.openurp.edu.eams.teach.program.major.dao.hibernate

import java.util.List
import org.beangle.commons.lang.Strings
import org.beangle.orm.hibernate.HibernateEntityDao
import org.hibernate.Query
import com.ekingstar.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.program.major.dao.MajorPlanStatDao
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanStatDaoHibernate extends HibernateEntityDao with MajorPlanStatDao {

  def statByDepart(realm: DataRealm, grade: String): List[_] = {
    val statHql = "select new com.ekingstar.eams.util.stat.CountItem(count(*), plan.department) " + 
      " from com.ekingstar.eams.teach.plan.model.TeachPlan as plan" + 
      " where plan.program.grade=  :grade " + 
      " and plan.department.id in (:departIds)" + 
      " and plan.program.stdType.id in (:stdTypeIds)" + 
      " group by  plan.department.id"
    val query = getSession.createQuery(statHql)
    query.setParameterList("departIds", Strings.splitToLong(realm.getDepartmentIdSeq))
    query.setParameterList("stdTypeIds", Strings.splitToLong(realm.getStudentTypeIdSeq))
    query.setParameter("grade", grade)
    query.list()
  }

  def statByStdType(realm: DataRealm, grade: String): List[_] = {
    val statHql = "select new com.ekingstar.eams.util.stat.CountItem(count(*), plan.program.stdType) " + 
      " from com.ekingstar.eams.teach.plan.model.TeachPlan as plan" + 
      " where plan.program.grade=  :grade " + 
      " and plan.department.id in (:departIds)" + 
      " and plan.program.stdType.id in (:stdTypeIds)" + 
      " group by  plan.program.stdType.id"
    val query = getSession.createQuery(statHql)
    query.setParameterList("departIds", Strings.splitToLong(realm.getDepartmentIdSeq))
    query.setParameterList("stdTypeIds", Strings.splitToLong(realm.getStudentTypeIdSeq))
    query.setParameter("grade", grade)
    query.list()
  }

  def getGrades(realm: DataRealm): List[_] = {
    val statHql = "select distinct plan.program.grade from com.ekingstar.eams.teach.plan.model.TeachPlan as plan " + 
      " where plan.department.id in (:departIds) " + 
      " and plan.program.stdType.id in(:stdTypeIds)" + 
      " order by  plan.program.grade desc"
    val query = getSession.createQuery(statHql)
    query.setParameterList("departIds", Strings.splitToLong(realm.getDepartmentIdSeq))
    query.setParameterList("stdTypeIds", Strings.splitToLong(realm.getStudentTypeIdSeq))
    query.list()
  }
}
