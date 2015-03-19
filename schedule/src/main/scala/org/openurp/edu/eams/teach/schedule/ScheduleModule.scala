package org.openurp.edu.eams.teach.schedule

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.ems.io.ClasspathDocLoader
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean
import org.openurp.edu.eams.teach.schedule.dao.hibernate.CourseActivityDaoHibernate
import org.openurp.edu.eams.teach.schedule.service.ScheduleLogHelper
import org.openurp.edu.eams.teach.schedule.service.impl.BruteForceArrangeServiceImpl
import org.openurp.edu.eams.teach.schedule.service.impl.CourseActivityServiceImpl
import org.openurp.edu.eams.teach.schedule.service.impl.CourseTableCheckServiceImpl
import org.openurp.edu.eams.teach.schedule.service.impl.CourseTableMailServiceImpl
import org.openurp.edu.eams.teach.schedule.service.impl.DefaultStdCourseTablePermissionChecker
import org.openurp.edu.eams.teach.schedule.service.impl.ScheduleRoomServiceImpl
import org.openurp.edu.eams.teach.schedule.service.impl.StdStatServiceImpl
import org.openurp.edu.eams.teach.schedule.web.action.AllocateLessonForArrangeAction
import org.openurp.edu.eams.teach.schedule.web.action.ArrangeSuggestAction
import org.openurp.edu.eams.teach.schedule.web.action.ArrangeSuggestToScheduleAction
import org.openurp.edu.eams.teach.schedule.web.action.CalendarDownloadAction
import org.openurp.edu.eams.teach.schedule.web.action.CourseArrangeAlterationAction
import org.openurp.edu.eams.teach.schedule.web.action.CourseArrangeSettingAction
import org.openurp.edu.eams.teach.schedule.web.action.CourseArrangeSwitchAction
import org.openurp.edu.eams.teach.schedule.web.action.CourseTableAction
import org.openurp.edu.eams.teach.schedule.web.action.CourseTableCheckAction
import org.openurp.edu.eams.teach.schedule.web.action.CourseTableForStdAction
import org.openurp.edu.eams.teach.schedule.web.action.CourseTableForTeacherAction
import org.openurp.edu.eams.teach.schedule.web.action.CurriculumChangeApplyAction
import org.openurp.edu.eams.teach.schedule.web.action.CurriculumChangeManageAction
import org.openurp.edu.eams.teach.schedule.web.action.GroupArrangeAction
import org.openurp.edu.eams.teach.schedule.web.action.GroupArrangeDepartmentAction
import org.openurp.edu.eams.teach.schedule.web.action.LessonGroupAction
import org.openurp.edu.eams.teach.schedule.web.action.ManualArrangeAction
import org.openurp.edu.eams.teach.schedule.web.action.ManualArrangeForDepartAction
import org.openurp.edu.eams.teach.schedule.web.action.MultiManualArrangeAction
import org.openurp.edu.eams.teach.schedule.web.action.MultiManualArrangeForDepartAction
import org.openurp.edu.eams.teach.schedule.web.action.ReplaceRoomAction
import org.openurp.edu.eams.teach.schedule.web.action.ScheduleLogSearchAction
import org.openurp.edu.eams.teach.schedule.web.action.ScheduleSearchAction
import org.openurp.edu.eams.web.util.DownloadHelper



class ScheduleModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[GroupArrangeDepartmentAction], classOf[GroupArrangeAction], classOf[ScheduleSearchAction], 
      classOf[ScheduleLogSearchAction], classOf[AllocateLessonForArrangeAction], classOf[ManualArrangeForDepartAction], 
      classOf[MultiManualArrangeForDepartAction], classOf[CourseTableForStdAction], classOf[CourseTableForTeacherAction], 
      classOf[CourseTableAction], classOf[CourseArrangeSwitchAction], classOf[CourseTableCheckAction], 
      classOf[CourseArrangeAlterationAction], classOf[CurriculumChangeApplyAction], classOf[CourseArrangeSettingAction], 
      classOf[CurriculumChangeManageAction], classOf[MultiManualArrangeAction], classOf[CalendarDownloadAction], 
      classOf[ReplaceRoomAction], classOf[LessonGroupAction], classOf[ArrangeSuggestAction], classOf[ArrangeSuggestToScheduleAction])
    bind("stdCourseTablePermissionChecker", classOf[DefaultStdCourseTablePermissionChecker])
    bind("manualArrange", classOf[ManualArrangeAction])
    bind("courseActivityService", classOf[CourseActivityServiceImpl])
    bind("courseTableCheckService", classOf[CourseTableCheckServiceImpl])
    bind("stdStatService", classOf[StdStatServiceImpl])
    bind("downloadHelper", classOf[DownloadHelper])
    bind("staticFileLoader", classOf[ClasspathDocLoader])
    bind("courseTableMailService", classOf[CourseTableMailServiceImpl])
    bind("scheduleLogHelper", classOf[ScheduleLogHelper])
    bind("scheduleRoomService", classOf[ScheduleRoomServiceImpl])
    bind("bruteForceArrangeService", classOf[BruteForceArrangeServiceImpl])
    bind("courseActivityDao", classOf[TransactionProxyFactoryBean])
      .proxy("target", classOf[CourseActivityDaoHibernate])
      .parent("baseTransactionProxy")
  }
}
