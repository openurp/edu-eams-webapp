package org.openurp.edu.eams.teach.program

import org.beangle.commons.inject.bind.AbstractBindModule
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean
import com.ekingstar.eams.teach.major.helper.MajorPlanSearchHelper
import org.openurp.edu.eams.teach.program.bind.service.impl.StudentProgramBindServiceImpl
import org.openurp.edu.eams.teach.program.bind.web.action.ProgramBindManageAction
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.MajorCourseGroupCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.OriginalMajorCourseGroupCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.PersonalPlanCourseGroupCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.plan.MajorPlanCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.plan.OriginalMajorPlanCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.plan.PersonalPlanCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.plancourse.MajorPlanCourseCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.plancourse.OriginalMajorPlanCourseCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.copydao.plancourse.PersonalPlanCourseCopyDaoHibernate
import org.openurp.edu.eams.teach.program.common.dao.impl.PlanCommonDaoHibernate
import org.openurp.edu.eams.teach.program.common.dao.impl.PlanCourseCommonDaoHibernate
import org.openurp.edu.eams.teach.program.common.dao.impl.PlanCourseGroupCommonDaoHibernate
import org.openurp.edu.eams.teach.program.common.service.impl.PlanCompareServiceImpl
import org.openurp.edu.eams.teach.program.major.dao.hibernate.MajorPlanAuditDaoHibernate
import org.openurp.edu.eams.teach.program.major.dao.hibernate.MajorPlanCourseDaoHibernate
import org.openurp.edu.eams.teach.program.major.dao.hibernate.MajorCourseGroupDaoHibernate
import org.openurp.edu.eams.teach.program.major.dao.hibernate.MajorPlanDaoHibernate
import org.openurp.edu.eams.teach.program.major.flexible.impl.DefaultMajorProgramTextTitleProvider
import org.openurp.edu.eams.teach.program.major.guard.impl.MajorProgramBasicGuard
import org.openurp.edu.eams.teach.program.major.guard.impl.MajorProgramCUDGuard
import org.openurp.edu.eams.teach.program.major.service.impl.MajorPlanAuditServiceImpl
import org.openurp.edu.eams.teach.program.major.service.impl.MajorCourseGroupServiceImpl
import org.openurp.edu.eams.teach.program.major.service.impl.MajorPlanCourseServiceImpl
import org.openurp.edu.eams.teach.program.major.service.impl.MajorPlanServiceImpl
import org.openurp.edu.eams.teach.program.major.web.action.CollegeCourseAction
import org.openurp.edu.eams.teach.program.major.web.action.MajorPlanAction
import org.openurp.edu.eams.teach.program.major.web.action.MajorPlanAuditAction
import org.openurp.edu.eams.teach.program.major.web.action.MajorPlanCourseAction
import org.openurp.edu.eams.teach.program.major.web.action.MajorCourseGroupAction
import org.openurp.edu.eams.teach.program.major.web.action.MajorPlanSearchAction
import org.openurp.edu.eams.teach.program.major.web.action.ProgramDocAction
import org.openurp.edu.eams.teach.program.majorapply.dao.hibernate.MajorCourseGroupModifyApplyDaoHibernate
import org.openurp.edu.eams.teach.program.majorapply.dao.hibernate.MajorCourseGroupModifyAuditDaoHibernate
import org.openurp.edu.eams.teach.program.majorapply.dao.hibernate.MajorPlanCourseModifyApplyDaoHibernate
import org.openurp.edu.eams.teach.program.majorapply.dao.hibernate.MajorPlanCourseModifyAuditDaoHibernate
import org.openurp.edu.eams.teach.program.majorapply.service.impl.MajorCourseGroupModifyApplyServiceImpl
import org.openurp.edu.eams.teach.program.majorapply.service.impl.MajorCourseGroupModifyAuditServiceImpl
import org.openurp.edu.eams.teach.program.majorapply.service.impl.MajorPlanCourseModifyApplyServiceImpl
import org.openurp.edu.eams.teach.program.majorapply.service.impl.MajorPlanCourseModifyAuditServiceImpl
import org.openurp.edu.eams.teach.program.majorapply.web.action.MajorCourseGroupModifyApplyAction
import org.openurp.edu.eams.teach.program.majorapply.web.action.MajorCourseGroupModifyAuditAction
import org.openurp.edu.eams.teach.program.majorapply.web.action.MajorPlanCourseModifyApplyAction
import org.openurp.edu.eams.teach.program.majorapply.web.action.MajorPlanCourseModifyAuditAction
import org.openurp.edu.eams.teach.program.majorapply.web.action.MajorPlanModifyApplyAction
import org.openurp.edu.eams.teach.program.majorapply.web.action.MajorPlanModifyAuditAction
import org.openurp.edu.eams.teach.program.original.web.action.OriginalMajorPlanSearchAction
import org.openurp.edu.eams.teach.program.personal.service.impl.PersonalPlanCompareServiceImpl
import org.openurp.edu.eams.teach.program.personal.service.impl.PersonalPlanCourseServiceImpl
import org.openurp.edu.eams.teach.program.personal.service.impl.PersonalPlanServiceImpl
import org.openurp.edu.eams.teach.program.personal.web.action.PersonalPlanAction
import org.openurp.edu.eams.teach.program.personal.web.action.PersonalPlanCourseAction
import org.openurp.edu.eams.teach.program.personal.web.action.PersonalPlanCourseGroupAction
import org.openurp.edu.eams.teach.program.personal.web.action.PersonalPlanSearchAction
import org.openurp.edu.eams.teach.program.share.web.action.SharePlanAction
import org.openurp.edu.eams.teach.program.share.web.action.SharePlanCourseAction
import org.openurp.edu.eams.teach.program.share.web.action.SharePlanCourseGroupAction
import org.openurp.edu.eams.teach.program.student.web.action.MyPlanAction
import org.openurp.edu.eams.teach.program.subst.web.action.MajorCourseSubstitutionAction
import org.openurp.edu.eams.teach.program.subst.web.action.StdCourseSubstitutionAction
import org.openurp.edu.eams.teach.program.template.web.action.DocTemplateAction
//remove if not needed


class PlanWebActionModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[MyPlanAction], classOf[DocTemplateAction], classOf[ProgramDocAction], classOf[CollegeCourseAction])
    bind(classOf[ProgramBindManageAction], classOf[StdCourseSubstitutionAction], classOf[MajorCourseSubstitutionAction])
    bind(classOf[MajorPlanSearchAction], classOf[MajorCourseGroupAction], classOf[MajorPlanCourseAction], 
      classOf[PersonalPlanSearchAction], classOf[PersonalPlanAction], classOf[PersonalPlanCourseAction], 
      classOf[PersonalPlanCourseGroupAction], classOf[MajorPlanAuditAction], classOf[OriginalMajorPlanSearchAction], 
      classOf[MajorPlanModifyApplyAction], classOf[MajorPlanModifyAuditAction], classOf[MajorPlanCourseModifyApplyAction], 
      classOf[MajorPlanCourseModifyAuditAction], classOf[MajorCourseGroupModifyApplyAction], classOf[MajorCourseGroupModifyAuditAction])
    bind(classOf[SharePlanAction], classOf[SharePlanCourseAction], classOf[SharePlanCourseGroupAction])
    bind(classOf[MajorPlanAction]).property("guards", list(ref("majorProgramBasicGuard"), ref("majorProgramCUDGuard")))
    bind("textTitleProvider", classOf[DefaultMajorProgramTextTitleProvider])
    bind("majorProgramBasicGuard", classOf[MajorProgramBasicGuard])
    bind("majorProgramCUDGuard", classOf[MajorProgramCUDGuard])
    bind("studentProgramBindService", classOf[StudentProgramBindServiceImpl])
    bind("baseTransactionProxyExt", classOf[TransactionProxyFactoryBean])
      .parent("baseTransactionProxy")
      .setAbstract()
      .property("transactionAttributes", props("save*=PROPAGATION_REQUIRED", "update*=PROPAGATION_REQUIRED", 
      "remove*=PROPAGATION_REQUIRED", "delete*=PROPAGATION_REQUIRED", "create*=PROPAGATION_REQUIRED", 
      "gen*=PROPAGATION_REQUIRED", "copy**=PROPAGATION_REQUIRED", "init*=PROPAGATION_REQUIRED", "add*=PROPAGATION_REQUIRED", 
      "approved*=PROPAGATION_REQUIRED", "rejected*=PROPAGATION_REQUIRED", "*=PROPAGATION_REQUIRED,readOnly"))
    bind("majorPlanDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorPlanDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("MajorCourseGroupDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorCourseGroupDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("majorPlanCourseDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorPlanCourseDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("majorPlanCourseModifyApplyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorPlanCourseModifyApplyDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("majorPlanCourseModifyAuditDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorPlanCourseModifyAuditDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("MajorCourseGroupModifyApplyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorCourseGroupModifyApplyDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("MajorCourseGroupModifyAuditDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorCourseGroupModifyAuditDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("majorPlanService", classOf[MajorPlanServiceImpl])
    bind("MajorCourseGroupService", classOf[MajorCourseGroupServiceImpl])
    bind("majorPlanCourseService", classOf[MajorPlanCourseServiceImpl])
    bind("planCompareService", classOf[PlanCompareServiceImpl])
    bind("personalPlanCompareService", classOf[PersonalPlanCompareServiceImpl])
    bind("personalPlanService", classOf[PersonalPlanServiceImpl])
    bind("personalPlanCourseService", classOf[PersonalPlanCourseServiceImpl])
    bind("majorPlanAuditService", classOf[MajorPlanAuditServiceImpl])
    bind("majorPlanAuditDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorPlanAuditDaoHibernate])
      .parent("baseTransactionProxy")
    bind("majorPlanCourseModifyApplyService", classOf[MajorPlanCourseModifyApplyServiceImpl])
    bind("majorPlanCourseModifyAuditService", classOf[MajorPlanCourseModifyAuditServiceImpl])
    bind("MajorCourseGroupModifyApplyService", classOf[MajorCourseGroupModifyApplyServiceImpl])
    bind("MajorCourseGroupModifyAuditService", classOf[MajorCourseGroupModifyAuditServiceImpl])
    bind("majorPlanSearchHelper", classOf[MajorPlanSearchHelper])
    bind("planCommonDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[PlanCommonDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("planCourseCommonDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[PlanCourseCommonDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("planCourseGroupCommonDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[PlanCourseGroupCommonDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("planCompareService", classOf[PlanCompareServiceImpl])
    bind("majorPlanCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[MajorPlanCopyDaoHibernate]).property("courseGroupCopyDao", ref("MajorCourseGroupCopyDao")))
      .parent("baseTransactionProxyExt")
    bind("MajorCourseGroupCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[MajorCourseGroupCopyDaoHibernate])
      .property("planCourseCopyDao", ref("majorPlanCourseCopyDao")))
      .parent("baseTransactionProxyExt")
    bind("majorPlanCourseCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[MajorPlanCourseCopyDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("personalPlanCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[PersonalPlanCopyDaoHibernate]).property("courseGroupCopyDao", ref("personalPlanCourseGroupCopyDao")))
      .parent("baseTransactionProxyExt")
    bind("personalPlanCourseGroupCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[PersonalPlanCourseGroupCopyDaoHibernate])
      .property("planCourseCopyDao", ref("personalPlanCourseCopyDao")))
      .parent("baseTransactionProxyExt")
    bind("personalPlanCourseCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[PersonalPlanCourseCopyDaoHibernate])
      .parent("baseTransactionProxyExt")
    bind("originalMajorPlanCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[OriginalMajorPlanCopyDaoHibernate]).property("courseGroupCopyDao", 
      ref("originalMajorCourseGroupCopyDao")))
      .parent("baseTransactionProxyExt")
    bind("originalMajorCourseGroupCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[OriginalMajorCourseGroupCopyDaoHibernate])
      .property("planCourseCopyDao", ref("originalMajorPlanCourseCopyDao")))
      .parent("baseTransactionProxyExt")
    bind("originalMajorPlanCourseCopyDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[OriginalMajorPlanCourseCopyDaoHibernate])
      .parent("baseTransactionProxyExt")
  }
}
