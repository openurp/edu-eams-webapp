package org.openurp.edu.eams.teach.grade

import org.beangle.commons.inject.bind.AbstractBindModule
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean
import org.openurp.edu.eams.teach.grade.course.dao.hibernate.CourseGradeDaoHibernate
import org.openurp.edu.eams.teach.grade.course.service.MarkStyleHelper
import org.openurp.edu.eams.teach.grade.course.service.impl.DefaultGradeTypePolicy
import org.openurp.edu.eams.teach.grade.course.service.impl.DefaultMarkStyleStrategy
import org.openurp.edu.eams.teach.grade.course.service.impl.ExamTakeGeneratePublishListener
import org.openurp.edu.eams.teach.grade.course.service.impl.GradeInputSwithServiceImpl
import org.openurp.edu.eams.teach.grade.course.service.impl.GradeModifyApplyServiceImpl
import org.openurp.edu.eams.teach.grade.course.service.impl.MakeupByExamStrategy
import org.openurp.edu.eams.teach.grade.course.service.impl.MakeupGradeFilter
import org.openurp.edu.eams.teach.grade.course.service.impl.More01ReserveMethod
import org.openurp.edu.eams.teach.grade.course.service.impl.RecalcGpPublishListener
import org.openurp.edu.eams.teach.grade.course.service.impl.StdGradeServiceImpl
import org.openurp.edu.eams.teach.grade.course.web.action.AdminAction
import org.openurp.edu.eams.teach.grade.course.web.action.AdminGaAction
import org.openurp.edu.eams.teach.grade.course.web.action.AdminMakeupAction
import org.openurp.edu.eams.teach.grade.course.web.action.GradeFailCreditStatsAction
import org.openurp.edu.eams.teach.grade.course.web.action.GradeStateStatAction
import org.openurp.edu.eams.teach.grade.course.web.action.GradeViewScopeAction
import org.openurp.edu.eams.teach.grade.course.web.action.MultiStdReportAction
import org.openurp.edu.eams.teach.grade.course.web.action.PersonAction
import org.openurp.edu.eams.teach.grade.course.web.action.StatAction
import org.openurp.edu.eams.teach.grade.course.web.action.StateAction
import org.openurp.edu.eams.teach.grade.course.web.action.StdGradeAction
import org.openurp.edu.eams.teach.grade.course.web.action.StdGradeSearchAction
import org.openurp.edu.eams.teach.grade.course.web.action.TeacherAction
import org.openurp.edu.eams.teach.grade.course.web.action.TeacherGaAction
import org.openurp.edu.eams.teach.grade.course.web.action.TeacherMakeupAction
import org.openurp.edu.eams.teach.grade.course.web.action.TermReportAction
import org.openurp.edu.eams.teach.grade.course.web.dwr.GradeCalcualtorDwr
import org.openurp.edu.eams.teach.grade.course.web.helper.CourseGradeHelper
import org.openurp.edu.eams.teach.grade.course.web.helper.StringBuilderHelper
import org.openurp.edu.eams.teach.grade.course.web.helper.TeachClassGradeHelper
import org.openurp.edu.eams.teach.grade.lesson.web.action.ManageAction
import org.openurp.edu.eams.teach.grade.service.CourseGradePublishStack
import org.openurp.edu.eams.teach.grade.service.impl.MoreHalfReserveMethod
import org.openurp.edu.eams.teach.grade.transfer.web.action.AuditAction

import scala.collection.JavaConversions._

class GradeBeanModule extends AbstractBindModule {

  protected override def doBinding() {
    bind("makeupStdStrategy", classOf[MakeupByExamStrategy])
    bind("markStyleHelper", classOf[MarkStyleHelper])
    bind("markStyleStrategy", classOf[DefaultMarkStyleStrategy])
    bind("teachClassGradeHelper", classOf[TeachClassGradeHelper])
    bind("courseGradeDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[CourseGradeDaoHibernate])
      .parent("baseTransactionProxy")
    bind("courseGradeHelper", classOf[CourseGradeHelper])
    bind("stdGradeService", classOf[StdGradeServiceImpl])
    bind("stringBuilderHelper", classOf[StringBuilderHelper])
    bind("makeupGradeFilter", classOf[MakeupGradeFilter])
    bind("courseGradePublishStack", classOf[CourseGradePublishStack])
    bind("recalcGpPublishListener", classOf[RecalcGpPublishListener])
    bind("examTakeGeneratePublishListener", classOf[ExamTakeGeneratePublishListener])
    bind(classOf[GradeInputSwithServiceImpl])
    bind(classOf[DefaultGradeTypePolicy])
    bind(classOf[MoreHalfReserveMethod], classOf[More01ReserveMethod])
      .shortName()
    bind(classOf[GradeStateStatAction], classOf[TeacherAction], classOf[TeacherMakeupAction], classOf[TeacherGaAction], 
      classOf[AdminAction], classOf[AdminMakeupAction], classOf[AdminGaAction], classOf[StatAction], 
      classOf[GradeFailCreditStatsAction], classOf[StateAction], classOf[AuditAction], classOf[ManageAction], 
      classOf[PersonAction], classOf[GradeViewScopeAction], classOf[StdGradeAction], classOf[StdGradeSearchAction], 
      classOf[MultiStdReportAction], classOf[TermReportAction])
    bind(classOf[GradeCalcualtorDwr]).shortName()
    bind("gradeModifyApplyService", classOf[GradeModifyApplyServiceImpl])
  }
}
