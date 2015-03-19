package org.openurp.edu.eams.teach

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.inject.bind.BeanConfig.ReferenceValue
import org.beangle.commons.lang.tuple.Pair
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean
import org.openurp.edu.eams.teach.grade.service.impl.BestGradeFilter
import org.openurp.edu.eams.teach.grade.service.impl.BestOriginGradeFilter
import org.openurp.edu.eams.teach.grade.service.impl.DefaultCourseGradeCalculator
import org.openurp.edu.eams.teach.grade.service.impl.DefaultGpaPolicy
import org.openurp.edu.eams.teach.grade.service.impl.DefaultGpaService
import org.openurp.edu.eams.teach.grade.service.impl.DefaultGpaStatService
import org.openurp.edu.eams.teach.grade.service.impl.PassGradeFilter
import org.openurp.edu.eams.teach.grade.service.impl.ScriptGradeFilter
import org.openurp.edu.eams.teach.grade.service.impl.SpringGradeFilterRegistry
import org.openurp.edu.eams.teach.grade.service.internal.BestGradeCourseGradeProviderImpl
import org.openurp.edu.eams.teach.grade.service.internal.CourseGradeProviderImpl
import org.openurp.edu.eams.teach.grade.service.internal.CourseGradeServiceImpl
import org.openurp.edu.eams.teach.grade.service.internal.GradeCourseTypeProviderImpl
import org.openurp.edu.eams.teach.grade.service.internal.GradeRateServiceImpl
import org.openurp.edu.eams.teach.lesson.dao.hibernate.internal.CoursePrefixSeqNoGeneratorImpl
import org.openurp.edu.eams.teach.lesson.dao.hibernate.internal.LessonDaoHibernate
import org.openurp.edu.eams.teach.lesson.dao.hibernate.internal.LessonPlanRelationHibernateDao
import org.openurp.edu.eams.teach.lesson.dao.hibernate.internal.LessonSeqNoGeneratorImpl
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.internal.CourseLimitExtractorServiceImpl
import org.openurp.edu.eams.teach.lesson.service.internal.CourseLimitServiceImpl
import org.openurp.edu.eams.teach.lesson.service.internal.DefaultTeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.LessonServiceImpl
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.DefaultLessonFilterStrategyFactory
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByAdminclassStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByCourseTypeStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByDirectionStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByMajorStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByStdStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByStdTypeStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByTeachCLassDepartStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByTeachDepartStrategy
import org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy.LessonFilterByTeacherStrategy
import org.openurp.edu.eams.teach.planaudit.service.internal.PlanAuditServiceImpl
import org.openurp.edu.eams.teach.planaudit.service.listeners.PlanAuditCommonElectiveListener
import org.openurp.edu.eams.teach.planaudit.service.listeners.PlanAuditCourseSubstitutionListener
import org.openurp.edu.eams.teach.planaudit.service.listeners.PlanAuditCourseTakeListener
import org.openurp.edu.eams.teach.planaudit.service.listeners.PlanAuditCourseTypeMatchListener
import org.openurp.edu.eams.teach.planaudit.service.listeners.PlanAuditSkipListener
import org.openurp.edu.eams.teach.planaudit.service.observers.PlanAuditPersistObserver
import org.openurp.edu.eams.teach.program.service.internal.CoursePlanProviderImpl
import org.openurp.edu.eams.teach.program.service.internal.CourseSubstitutionServiceImpl
import org.openurp.edu.eams.teach.service.internal.CourseServiceImpl
import org.openurp.edu.eams.teach.service.internal.TeachResourceServiceImpl
import org.openurp.edu.eams.teach.textbook.service.internal.DefaultTextbookOrderLineCodeGenerator
import org.openurp.edu.eams.teach.textbook.service.internal.TextbookOrderLineServiceImpl



class TeachServiceModule extends AbstractBindModule {

  protected override def doBinding() {
    bind("courseService", classOf[CourseServiceImpl])
    bind("lessonService", classOf[LessonServiceImpl])
    bind("courseSubstitutionService", classOf[CourseSubstitutionServiceImpl])
    bind("gradeRateService", classOf[GradeRateServiceImpl])
    bind("bestGradeFilter", classOf[BestGradeFilter])
    bind("gpaPolicy", classOf[DefaultGpaPolicy])
    bind("bestOriginGradeFilter", classOf[BestOriginGradeFilter])
    bind("passGradeFilter", classOf[PassGradeFilter])
    bind("gradeFilterRegistry", classOf[SpringGradeFilterRegistry])
    bind("courseGradeService", classOf[CourseGradeServiceImpl])
    bind("scriptGradeFilter", classOf[ScriptGradeFilter])
    bind("courseGradeProvider", classOf[CourseGradeProviderImpl])
    bind("courseGradeCalculator", classOf[DefaultCourseGradeCalculator])
    bind("gpaService", classOf[DefaultGpaService])
    bind("gpaStatService", classOf[DefaultGpaStatService])
    bind("gradeCourseTypeProvider", classOf[GradeCourseTypeProviderImpl])
    bind("teachResourceService", classOf[TeachResourceServiceImpl])
    bind("textbookOrderLineService", classOf[TextbookOrderLineServiceImpl])
    bind("textbookOrderLineCodeGenerator", classOf[DefaultTextbookOrderLineCodeGenerator])
    bind("coursePlanProvider", classOf[CoursePlanProviderImpl])
    bind("bestGradeCourseGradeProvider", classOf[BestGradeCourseGradeProviderImpl])
    bind("courseLimitService", classOf[CourseLimitServiceImpl])
    bind("courseLimitExtractorService", classOf[CourseLimitExtractorServiceImpl])
    bind("teachClassNameStrategy", classOf[DefaultTeachClassNameStrategy])
    bind("lessonLogHelper", classOf[LessonLogHelper])
    bind("planAuditSkipListener", classOf[PlanAuditSkipListener])
    bind("planAuditCourseSubstitutionListener", classOf[PlanAuditCourseSubstitutionListener])
    bind("planAuditCourseTakeListener", classOf[PlanAuditCourseTakeListener])
    bind("planAuditCourseTypeMatchListener", classOf[PlanAuditCourseTypeMatchListener])
    bind("planAuditCommonElectiveListener", classOf[PlanAuditCommonElectiveListener])
    bind("planAuditPersistObserver", classOf[PlanAuditPersistObserver])
    bind("lessonDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[LessonDaoHibernate]))
      .parent("baseTransactionProxy")
      .property("transactionAttributes", props("*=PROPAGATION_REQUIRED"))
    bind("lessonPlanRelationDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", bean(classOf[LessonPlanRelationHibernateDao]))
      .parent("baseTransactionProxy")
      .property("transactionAttributes", props("related*=PROPAGATION_REQUIRED,readOnly", "possible*=PROPAGATION_REQUIRED,readOnly", 
      "relations=PROPAGATION_REQUIRED,readOnly", "planAuditStatuses=PROPAGATION_REQUIRED,readOnly", "operateViolationCheck=PROPAGATION_REQUIRED,readOnly", 
      "auditStatus=PROPAGATION_REQUIRED,readOnly", "save*=PROPAGATION_REQUIRED", "remove*=PROPAGATION_REQUIRED", 
      "update*=PROPAGATION_REQUIRED"))
    bind("lessonSeqNoGeneratorImpl", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[LessonSeqNoGeneratorImpl])
      .parent("baseTransactionProxy")
    bind("coursePrefixSeqNoGeneratorImpl", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[CoursePrefixSeqNoGeneratorImpl])
      .parent("baseTransactionProxy")
    bind("lessonFilterByAdminclassStrategy", classOf[LessonFilterByAdminclassStrategy])
    bind("lessonFilterByCourseTypeStrategy", classOf[LessonFilterByCourseTypeStrategy])
    bind("lessonFilterByDirectionStrategy", classOf[LessonFilterByDirectionStrategy])
    bind("lessonFilterByMajorStrategy", classOf[LessonFilterByMajorStrategy])
    bind("lessonFilterByStdStrategy", classOf[LessonFilterByStdStrategy])
    bind("lessonFilterByStdTypeStrategy", classOf[LessonFilterByStdTypeStrategy])
    bind("lessonFilterByTeachDepartStrategy", classOf[LessonFilterByTeachDepartStrategy])
    bind("lessonFilterByTeachCLassDepartStrategy", classOf[LessonFilterByTeachCLassDepartStrategy])
    bind("lessonFilterByTeacherStrategy", classOf[LessonFilterByTeacherStrategy])
    bind("lessonFilterStrategyFactory", classOf[DefaultLessonFilterStrategyFactory])
      .property("lessonFilterStrategies", map(new Pair[String, ReferenceValue](LessonFilterStrategy.ADMINCLASS, 
      ref("lessonFilterByAdminclassStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.COURSE_TYPE, 
      ref("lessonFilterByCourseTypeStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.DIRECTION, 
      ref("lessonFilterByDirectionStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.MAJOR, 
      ref("lessonFilterByMajorStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.STD, 
      ref("lessonFilterByStdStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.STD_TYPE, 
      ref("lessonFilterByStdTypeStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.TEACH_DEPART, 
      ref("lessonFilterByTeachDepartStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.TEACHCLASS_DEPART, 
      ref("lessonFilterByTeachCLassDepartStrategy")), new Pair[String, ReferenceValue](LessonFilterStrategy.TEACHER, 
      ref("lessonFilterByTeacherStrategy"))))
    bind("planAuditService", classOf[PlanAuditServiceImpl])
  }
}
