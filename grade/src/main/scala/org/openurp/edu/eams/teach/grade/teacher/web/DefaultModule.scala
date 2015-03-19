package org.openurp.edu.eams.teach.grade.teacher.web

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.teach.grade.teacher.web.action.GradeForTeacherAction
import org.openurp.edu.eams.teach.grade.teacher.web.action.GradeModifyAdminAuditAction
import org.openurp.edu.eams.teach.grade.teacher.web.action.GradeModifyApplyAction
import org.openurp.edu.eams.teach.grade.teacher.web.action.GradeModifyAuditSearchAction
import org.openurp.edu.eams.teach.grade.teacher.web.action.GradeModifyDepartAuditAction
import org.openurp.edu.eams.teach.grade.teacher.web.action.GradeModifyFinalAuditAction



class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[GradeModifyAuditSearchAction], classOf[GradeModifyDepartAuditAction], classOf[GradeModifyAdminAuditAction], 
      classOf[GradeModifyFinalAuditAction], classOf[GradeModifyApplyAction], classOf[GradeForTeacherAction])
  }
}
