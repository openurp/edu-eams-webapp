package org.openurp.edu.eams.teach.program.majorapply.dao.hibernate

import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorCourseGroupModifyApplyDao
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorCourseGroupModifyDetailBeforeBean
//remove if not needed


class MajorCourseGroupModifyApplyDaoHibernate extends HibernateEntityDao with MajorCourseGroupModifyApplyDao {

  def saveModifyApply(apply: MajorCourseGroupModifyBean, before: MajorCourseGroupModifyDetailBeforeBean, after: MajorCourseGroupModifyDetailAfterBean) {
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
