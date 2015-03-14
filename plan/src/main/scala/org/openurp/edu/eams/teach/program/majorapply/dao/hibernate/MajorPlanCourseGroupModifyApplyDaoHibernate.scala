package org.openurp.edu.eams.teach.program.majorapply.dao.hibernate

import org.beangle.orm.hibernate.HibernateEntityDao
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseGroupModifyApplyDao
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyDetailBeforeBean
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupModifyApplyDaoHibernate extends HibernateEntityDao with MajorPlanCourseGroupModifyApplyDao {

  def saveModifyApply(apply: MajorPlanCourseGroupModifyBean, before: MajorPlanCourseGroupModifyDetailBeforeBean, after: MajorPlanCourseGroupModifyDetailAfterBean) {
    saveOrUpdate(apply)
    if (before != null) {
      apply.setOldPlanCourseGroup(before)
      before.setApply(apply)
      saveOrUpdate(before)
    }
    if (after != null) {
      apply.setNewPlanCourseGroup(after)
      after.setApply(apply)
      saveOrUpdate(after)
    }
  }
}
