package org.openurp.edu.eams.teach.program.personal.dao.hibernate



import org.apache.commons.lang3.Range
import org.beangle.commons.entity.util.ValidEntityPredicate
import org.beangle.commons.lang.Strings
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import com.ekingstar.eams.core.code.school.StdType
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.dao.hibernate.MajorPlanDaoHibernate
import org.openurp.edu.eams.teach.program.personal.dao.PersonalPlanDao
import com.ekingstar.eams.util.DataRealmLimit
//remove if not needed


class PersonalPlanDaoHibernate extends MajorPlanDaoHibernate with PersonalPlanDao {

  def getCreditByTerm(plan: MajorPlan, term: Int): java.lang.Float = {
    val termRange = Range.between(1, plan.getTermsCount.intValue())
    if (!termRange.contains(term)) {
      throw new RuntimeException("term out range")
    } else {
      null
    }
  }

  def addDataRealmLimt(criteria: Criteria, attr: Array[String], limit: DataRealmLimit) {
    if (null == limit || null == attr || null == limit.getDataRealm) {
      return
    }
    if (attr.length > 0) {
      if (Strings.isNotEmpty(limit.getDataRealm.getStudentTypeIdSeq) && 
        Strings.isNotEmpty(attr(0))) {
        criteria.add(Restrictions.in(attr(0), Strings.splitToLong(limit.getDataRealm.getStudentTypeIdSeq)))
      }
    }
    if (attr.length > 1) {
      if (Strings.isNotEmpty(limit.getDataRealm.getDepartmentIdSeq) && 
        Strings.isNotEmpty(attr(1))) {
        criteria.add(Restrictions.in(attr(1), Strings.splitToLong(limit.getDataRealm.getDepartmentIdSeq)))
      }
    }
  }

  protected def intersectStdTypeIdSeq(stdType: StdType, stdTypeIdSeq: String): String = {
    if (ValidEntityPredicate.Instance.apply(stdType)) {
      stdType = get(classOf[StdType], stdType.id)
      val stdTypes = new ArrayList()
      stdTypes.add(stdType)
      val sb = new StringBuffer()
      for (i <- 0 until stdTypes.size) {
        val one = stdTypes.get(i).asInstanceOf[StdType]
        sb.append(one.id.toString).append(", ")
      }
      stdTypeIdSeq = Strings.intersectSeq(stdTypeIdSeq, sb.toString)
    }
    stdTypeIdSeq
  }
}
