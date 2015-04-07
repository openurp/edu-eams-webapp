package org.openurp.edu.eams.teach.lesson.task

import org.beangle.commons.inject.bind.AbstractBindModule
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitAdminclassProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitDepartmentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitDirectionProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitEducationProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitGenderProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitGradeProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitMajorProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitNormalclassProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitProgramProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitStdLabelProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.LessonLimitStdTypeProvider
import org.openurp.edu.eams.teach.lesson.service.limit.impl.DefaultLessonLimitItemContentProviderFactory
import org.openurp.edu.eams.teach.lesson.service.limit.impl.DefaultLessonLimitMetaEnumProvider
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
import org.openurp.edu.teach.lesson.LessonLimitMeta



class LessonModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[TeachTaskAction], classOf[TeachTaskCollegeAction], classOf[TeachTaskSearchAction])
    bind(classOf[LessonSubmitAction], classOf[LessonAuditAction], classOf[LessonLogSearchAction])
    bind(classOf[TeachTaskGenAction])
    bind(classOf[LessonStatisticAction], classOf[LessonMultiDimensionStatAction])
    bind(classOf[LessonPlanCheckAction])
    bind("lessonLimitItemContentProviderFactory", classOf[DefaultLessonLimitItemContentProviderFactory])
      .property("providers", map(new Pair[Any, Any](LessonLimitMeta.Grade, classOf[LessonLimitGradeProvider]), 
      new Pair[Any, Any](LessonLimitMeta.StdType, classOf[LessonLimitStdTypeProvider]), new Pair[Any, Any](LessonLimitMeta.Gender, 
      classOf[LessonLimitGenderProvider]), new Pair[Any, Any](LessonLimitMeta.Department, classOf[LessonLimitDepartmentProvider]), 
      new Pair[Any, Any](LessonLimitMeta.Major, classOf[LessonLimitMajorProvider]), new Pair[Any, Any](LessonLimitMeta.Direction, 
      classOf[LessonLimitDirectionProvider]), new Pair[Any, Any](LessonLimitMeta.Adminclass, classOf[LessonLimitAdminclassProvider]), 
      new Pair[Any, Any](LessonLimitMeta.Education, classOf[LessonLimitEducationProvider]), new Pair[Any, Any](LessonLimitMeta.Program, 
      classOf[LessonLimitProgramProvider]), 
      new Pair[Any, Any](LessonLimitMeta.StdLabel, classOf[LessonLimitStdLabelProvider])))
    bind("lessonExamArrangeHelper", classOf[LessonExamArrangeHelper])
    bind("lessonStatDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[LessonStatDaoHibernate])
      .parent("baseTransactionProxy")
    bind("teachTaskGenService", classOf[TeachTaskGenServiceImpl])
    bind("lessonStatService", classOf[LessonStatServiceImpl])
    bind("lessonPlanCheckService", classOf[LessonPlanCheckServiceImpl])
    bind("lessonMergeSplitService", classOf[LessonMergeSplitServiceImpl])
    bind("lessonCollegeSwitchService", classOf[LessonCollegeSwitchServiceImpl])
    bind("lessonGenService", classOf[LessonGenServiceImpl])
      .property("strategies", list(ref("classicLessonGenStrategy")))
    bind("classicLessonGenStrategy", classOf[ClassicLessonGenStrategy])
  }
}
