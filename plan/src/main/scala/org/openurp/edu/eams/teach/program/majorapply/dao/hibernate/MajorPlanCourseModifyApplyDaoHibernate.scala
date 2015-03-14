package org.openurp.edu.eams.teach.program.majorapply.dao.hibernate

import org.beangle.orm.hibernate.HibernateEntityDao
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseModifyApplyDao
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBeforeBean
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseModifyApplyDaoHibernate extends HibernateEntityDao with MajorPlanCourseModifyApplyDao {

  def saveModifyApply(apply: MajorPlanCourseModifyBean, before: MajorPlanCourseModifyDetailBeforeBean, after: MajorPlanCourseModifyDetailAfterBean) {
    saveOrUpdate(apply)
    if (before != null) {
      apply.setOldPlanCourse(before)
      before.setApply(apply)
      saveOrUpdate(before)
    }
    if (after != null) {
      apply.setNewPlanCourse(after)
      after.setApply(apply)
      saveOrUpdate(after)
    }
  }
}
