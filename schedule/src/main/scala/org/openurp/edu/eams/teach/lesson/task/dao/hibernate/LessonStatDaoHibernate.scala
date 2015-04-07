package org.openurp.edu.eams.teach.lesson.task.dao.hibernate


import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.hibernate.SQLQuery
import org.hibernate.`type`.StandardBasicTypes
import org.openurp.edu.eams.teach.lesson.task.dao.LessonStatDao



class LessonStatDaoHibernate extends HibernateEntityDao with LessonStatDao {

  def statTeacherTitle(semesters: List[_]): List[_] = {
    val queryString = "select XNXQID,jszcid,count(*) as num from (" + 
      "				select distinct teachtask0_.XNXQID as XNXQID, teacher2_.id as jzgid,teacher2_.JSZCID as JSZCID" + 
      "				from JXRW_T teachtask0_ inner join JXRW_LS_T teachers1_ on teachtask0_.id=teachers1_.JXRWID" + 
      "				inner join JCXX_JZG_T teacher2_ on teachers1_.LSID=teacher2_.id where" + 
      "				(teachtask0_.XNXQID in (:semesterIds))" + 
      "				)group by XNXQID,jszcid"
    val query = currentSession.createSQLQuery(queryString)
    query.setParameterList("semesterIds", EntityUtils.extractIds(semesters))
    query.addScalar("XNXQID", StandardBasicTypes.LONG)
    query.addScalar("jszcid", StandardBasicTypes.LONG)
    query.addScalar("num", StandardBasicTypes.INTEGER)
    query.list()
  }
}
