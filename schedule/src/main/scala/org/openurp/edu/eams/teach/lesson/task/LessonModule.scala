package org.openurp.edu.eams.teach.lesson.task

import org.beangle.commons.inject.bind.AbstractBindModule

import org.springframework.transaction.interceptor.TransactionProxyFactoryBean
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitAdminclassProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitDepartmentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitDirectionProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitEducationProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitGenderProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitGradeProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitMajorProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitNormalclassProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitProgramProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitStdLabelProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.CourseLimitStdTypeProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.DefaultCourseLimitItemContentProviderFactory
import org.openurp.edu.eams.teach.lesson.service.limit.impl.DefaultCourseLimitMetaEnumProvider
import org.openurp.edu.eams.teach.lesson.task.dao.hibernate.LessonStatDaoHibernate
import org.openurp.edu.eams.teach.lesson.task.service.genstrategy.impl.ClassicLessonGenStrategy
import org.openurp.edu.eams.teach.lesson.task.service.helper.LessonExamArrangeHelper
import org.openurp.edu.eams.teach.lesson.task.service.impl.LessonCollegeSwitchServiceImpl
import org.openurp.edu.eams.teach.lesson.task.service.impl.LessonGenServiceImpl
import org.openurp.edu.eams.teach.lesson.task.service.impl.LessonMergeSplitServiceImpl
import org.openurp.edu.eams.teach.lesson.task.service.impl.LessonPlanCheckServiceImpl
import org.openurp.edu.eams.teach.lesson.task.service.impl.LessonPlanRelationServiceImpl
import org.openurp.edu.eams.teach.lesson.task.service.impl.LessonStatServiceImpl
import org.openurp.edu.eams.teach.lesson.task.service.impl.TeachTaskGenServiceImpl
import org.openurp.edu.eams.teach.lesson.task.web.action.LessonAuditAction
import org.openurp.edu.eams.teach.lesson.task.web.action.LessonLogSearchAction
import org.openurp.edu.eams.teach.lesson.task.web.action.LessonMultiDimensionStatAction
import org.openurp.edu.eams.teach.lesson.task.web.action.LessonStatisticAction
import org.openurp.edu.eams.teach.lesson.task.web.action.LessonSubmitAction
import org.openurp.edu.eams.teach.lesson.task.web.action.TeachTaskAction
import org.openurp.edu.eams.teach.lesson.task.web.action.TeachTaskCollegeAction
import org.openurp.edu.eams.teach.lesson.task.web.action.TeachTaskGenAction
import org.openurp.edu.eams.teach.lesson.task.web.action.TeachTaskSearchAction
import org.openurp.edu.eams.teach.lesson.task.web.action.parent.LessonPlanCheckAction



class LessonModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[TeachTaskAction], classOf[TeachTaskCollegeAction], classOf[TeachTaskSearchAction])
    bind(classOf[LessonSubmitAction], classOf[LessonAuditAction], classOf[LessonLogSearchAction])
    bind(classOf[TeachTaskGenAction])
    bind(classOf[LessonStatisticAction], classOf[LessonMultiDimensionStatAction])
    bind(classOf[LessonPlanCheckAction])
    bind("courseLimitMetaEnumProvider", classOf[DefaultCourseLimitMetaEnumProvider])
    bind("courseLimitItemContentProviderFactory", classOf[DefaultCourseLimitItemContentProviderFactory])
      .property("providers", map(new Pair[Any, Any](CourseLimitMetaEnum.GRADE, classOf[CourseLimitGradeProvider]), 
      new Pair[Any, Any](CourseLimitMetaEnum.STDTYPE, classOf[CourseLimitStdTypeProvider]), new Pair[Any, Any](CourseLimitMetaEnum.GENDER, 
      classOf[CourseLimitGenderProvider]), new Pair[Any, Any](CourseLimitMetaEnum.DEPARTMENT, classOf[CourseLimitDepartmentProvider]), 
      new Pair[Any, Any](CourseLimitMetaEnum.MAJOR, classOf[CourseLimitMajorProvider]), new Pair[Any, Any](CourseLimitMetaEnum.DIRECTION, 
      classOf[CourseLimitDirectionProvider]), new Pair[Any, Any](CourseLimitMetaEnum.ADMINCLASS, classOf[CourseLimitAdminclassProvider]), 
      new Pair[Any, Any](CourseLimitMetaEnum.EDUCATION, classOf[CourseLimitEducationProvider]), new Pair[Any, Any](CourseLimitMetaEnum.PROGRAM, 
      classOf[CourseLimitProgramProvider]), new Pair[Any, Any](CourseLimitMetaEnum.NORMALCLASS, classOf[CourseLimitNormalclassProvider]), 
      new Pair[Any, Any](CourseLimitMetaEnum.STDLABEL, classOf[CourseLimitStdLabelProvider])))
    bind("lessonExamArrangeHelper", classOf[LessonExamArrangeHelper])
    bind("lessonStatDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[LessonStatDaoHibernate])
      .parent("baseTransactionProxy")
    bind("teachTaskGenService", classOf[TeachTaskGenServiceImpl])
    bind("lessonStatService", classOf[LessonStatServiceImpl])
    bind("lessonPlanCheckService", classOf[LessonPlanCheckServiceImpl])
    bind("lessonPlanRelationService", classOf[LessonPlanRelationServiceImpl])
    bind("lessonMergeSplitService", classOf[LessonMergeSplitServiceImpl])
    bind("lessonCollegeSwitchService", classOf[LessonCollegeSwitchServiceImpl])
    bind("lessonGenService", classOf[LessonGenServiceImpl])
      .property("strategies", list(ref("classicLessonGenStrategy")))
    bind("classicLessonGenStrategy", classOf[ClassicLessonGenStrategy])
  }
}
