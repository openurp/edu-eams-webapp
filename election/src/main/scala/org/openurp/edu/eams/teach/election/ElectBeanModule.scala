package org.openurp.edu.eams.teach.election

import org.beangle.commons.inject.Scope
import org.beangle.commons.inject.bind.AbstractBindModule
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean
import org.openurp.edu.eams.teach.election.dao.impl.ElectionDaoHibernate
import org.openurp.edu.eams.teach.election.filter.DefaultCourseTakeFilterStrategy
import org.openurp.edu.eams.teach.election.service.RetakeFeeCalculator
import org.openurp.edu.eams.teach.election.service.cache.ProfileLessonCountProvider
import org.openurp.edu.eams.teach.election.service.cache.ProfileLessonDataProvider
import org.openurp.edu.eams.teach.election.service.checker.RetakePaymentChecker
import org.openurp.edu.eams.teach.election.service.event.ElectionProfileChangeEventListener
import org.openurp.edu.eams.teach.election.service.impl.CourseTakeServiceImpl
import org.openurp.edu.eams.teach.election.service.impl.CreditConstraintServiceImpl
import org.openurp.edu.eams.teach.election.service.impl.ElectLoggerServiceImpl
import org.openurp.edu.eams.teach.election.service.impl.ElectionProfileServiceImpl
import org.openurp.edu.eams.teach.election.service.impl.FilterMessageServiceImpl
import org.openurp.edu.eams.teach.election.service.impl.RetakeFeeConfigServiceImpl
import org.openurp.edu.eams.teach.election.service.impl.StdElectionServiceImpl
import org.openurp.edu.eams.teach.election.service.rule.RetakeServiceImpl
import org.openurp.edu.eams.teach.election.service.rule.election.CourseCountConstraintChecker
import org.openurp.edu.eams.teach.election.service.rule.election.CourseGradePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.CoursePlanPrepare
import org.openurp.edu.eams.teach.election.service.rule.election.CreditConstraintChecker
import org.openurp.edu.eams.teach.election.service.rule.election.CurrentTermPrepare
import org.openurp.edu.eams.teach.election.service.rule.election.LimitCountChecker
import org.openurp.edu.eams.teach.election.service.rule.election.PlanCourseGroupCreditsChecker
import org.openurp.edu.eams.teach.election.service.rule.election.PlanCreditLimitPrepare
import org.openurp.edu.eams.teach.election.service.rule.election.TimeConflictChecker
import org.openurp.edu.eams.teach.election.service.rule.election.TotalCreditConstraintChecker
import org.openurp.edu.eams.teach.election.service.rule.election.filter.CampusFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonByCourseAbilityRateFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonByGenderFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonByPlanFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonByTeachClassFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonByXMajorFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonNoRetakeFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonOnlyRetakeFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectionTeachClassFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.UniquePassRetakeFilter
import org.openurp.edu.eams.teach.election.service.rule.election.retake.RetakeCheatedChecker
import org.openurp.edu.eams.teach.election.service.rule.election.retake.RetakeCheckByCoursePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.retake.RetakeCourseBuildInPrepare
import org.openurp.edu.eams.teach.election.service.rule.general.EvaluationChecker
import org.openurp.edu.eams.teach.election.service.rule.withdraw.AssignedWithdrawPrepare
import org.openurp.edu.eams.teach.election.service.rule.withdraw.WithdrawTimePrepare
import org.openurp.edu.eams.teach.election.web.action.ElectScopeAction
import org.openurp.edu.eams.teach.election.web.action.ElectionProfileAction
import org.openurp.edu.eams.teach.election.web.action.StdElectCourseAction
import org.openurp.edu.eams.teach.election.web.action.StdElectLogAction
import org.openurp.edu.eams.teach.election.web.action.constraint.ConstraintLoggerAction
import org.openurp.edu.eams.teach.election.web.action.constraint.CourseTypeCreditConstraintAction
import org.openurp.edu.eams.teach.election.web.action.constraint.StdCourseCountConstraintAction
import org.openurp.edu.eams.teach.election.web.action.constraint.StdCreditConstraintAction
import org.openurp.edu.eams.teach.election.web.action.constraint.StdTotalCreditConstraintAction
import org.openurp.edu.eams.teach.election.web.action.courseTake.CourseTakeAction
import org.openurp.edu.eams.teach.election.web.action.courseTake.CourseTakeForTaskAction
import org.openurp.edu.eams.teach.election.web.action.courseTake.CourseTakeForTeacherAction
import org.openurp.edu.eams.teach.election.web.action.courseTake.CourseTakeRestoreAction
import org.openurp.edu.eams.teach.election.web.action.courseTake.CourseTakeSearchAction
import org.openurp.edu.eams.teach.election.web.action.retakePay.RetakeFeeConfigAction
import org.openurp.edu.eams.teach.election.web.action.retakePay.RetakePayAction
import org.openurp.edu.eams.teach.election.web.action.retakePay.RetakePayForStdAction
import org.openurp.edu.eams.teach.election.web.action.rule.ElectPlanAction
import org.openurp.edu.eams.teach.election.web.action.rule.ElectRuleAction
import org.openurp.edu.eams.teach.election.web.action.rule.ElectRuleConfigAction
import org.openurp.edu.eams.teach.election.web.action.rule.ElectRuleParameterAction

import scala.collection.JavaConversions._

class ElectBeanModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[ElectScopeAction])
    bind(classOf[ElectPlanAction])
    bind("courseTakeFilterStrategy", classOf[DefaultCourseTakeFilterStrategy])
    bind(classOf[ElectionProfileAction])
    bind(classOf[ElectRuleConfigAction])
    bind(classOf[ElectRuleAction], classOf[ElectRuleParameterAction])
    bind(classOf[StdCreditConstraintAction])
    bind(classOf[StdTotalCreditConstraintAction])
    bind(classOf[StdCourseCountConstraintAction])
    bind(classOf[CourseTypeCreditConstraintAction])
    bind(classOf[CourseTakeAction], classOf[CourseTakeSearchAction])
    bind(classOf[CourseTakeForTeacherAction])
    bind(classOf[CourseTakeForTaskAction])
    bind(classOf[StdElectCourseAction])
    bind(classOf[CourseTakeRestoreAction])
    bind(classOf[ConstraintLoggerAction])
    bind(classOf[StdElectLogAction])
    bind("electionProfileService", classOf[ElectionProfileServiceImpl])
      .in(Scope.SINGLETON)
      .in(Scope.SINGLETON)
      .lazy(false)
    bind("electionDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[ElectionDaoHibernate])
      .parent("baseTransactionProxy")
    bind("courseTakeService", classOf[CourseTakeServiceImpl])
    bind("creditConstraintService", classOf[CreditConstraintServiceImpl])
    bind("electLoggerService", classOf[ElectLoggerServiceImpl])
    bind("filterMessageService", classOf[FilterMessageServiceImpl])
    bind("evaluationChecker", classOf[EvaluationChecker])
    bind("campusFilter", classOf[CampusFilter])
    bind("limitCountChecker", classOf[LimitCountChecker])
    bind("timeConflictChecker", classOf[TimeConflictChecker])
    bind("planCourseGroupCreditsChecker", classOf[PlanCourseGroupCreditsChecker])
    bind("courseCountConstraintChecker", classOf[CourseCountConstraintChecker])
    bind("creditConstraintChecker", classOf[CreditConstraintChecker])
    bind("totalCreditConstraintChecker", classOf[TotalCreditConstraintChecker])
    bind("retakeCheckByCourse", classOf[RetakeCheckByCoursePrepare])
    bind("retakeCheatedChecker", classOf[RetakeCheatedChecker])
    bind("uniquePassRetakeFilter", classOf[UniquePassRetakeFilter])
    bind("assignedWithdrawSwitch", classOf[AssignedWithdrawPrepare])
    bind("withdrawTimeChecker", classOf[WithdrawTimePrepare])
    bind("electableLessonNoRetakeFilter", classOf[ElectableLessonNoRetakeFilter])
    bind("electableLessonOnlyRetakeFilter", classOf[ElectableLessonOnlyRetakeFilter])
    bind("electableLessonByCourseAbilityRateFilter", classOf[ElectableLessonByCourseAbilityRateFilter])
    bind("electableLessonByTeachClassFilter", classOf[ElectableLessonByTeachClassFilter])
    bind("electableLessonByXMajorFilter", classOf[ElectableLessonByXMajorFilter])
    bind("electableLessonByPlanFilter", classOf[ElectableLessonByPlanFilter])
    bind("electableLessonByGenderFilter", classOf[ElectableLessonByGenderFilter])
    bind("electionTeachClassFilter", classOf[ElectionTeachClassFilter])
      .in(Scope.PROTOTYPE)
    bind(classOf[CourseGradePrepare], classOf[CoursePlanPrepare], classOf[PlanCreditLimitPrepare], classOf[CurrentTermPrepare])
      .shortName()
    bind(classOf[StdElectionServiceImpl])
    bind("retakeService", classOf[RetakeServiceImpl])
    bind("retakeCourseBuildInPrepare", classOf[RetakeCourseBuildInPrepare])
    bind(classOf[RetakeFeeConfigAction])
    bind(classOf[RetakePayForStdAction])
    bind(classOf[RetakePayAction])
    bind("retakeFeeCalculator", classOf[RetakeFeeCalculator])
    bind("retakePaymentChecker", classOf[RetakePaymentChecker])
    bind("retakeFeeConfigService", classOf[RetakeFeeConfigServiceImpl])
    bind("planElectPreBuildinPrepare", classOf[CoursePlanPrepare])
    bind("profileLessonCountProvider", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[ProfileLessonCountProvider])
      .parent("baseTransactionProxy")
      .in(Scope.SINGLETON)
      .lazy(false)
    bind("profileLessonDataProvider", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[ProfileLessonDataProvider])
      .parent("baseTransactionProxy")
      .in(Scope.SINGLETON)
      .lazy(false)
    bind("stdElectionService", classOf[StdElectionServiceImpl])
      .property("buildInPrepares", list(ref("retakeCourseBuildInPrepare")))
    bind(classOf[ElectionProfileChangeEventListener])
  }
}
