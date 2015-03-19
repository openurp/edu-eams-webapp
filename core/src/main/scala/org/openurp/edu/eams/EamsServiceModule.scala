package org.openurp.edu.eams

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.notification.mail.JavaMailSender
import org.openurp.edu.eams.bootstrap.checker.CalendarChecker
import org.openurp.edu.eams.bootstrap.checker.CampusChecker
import org.openurp.edu.eams.bootstrap.checker.DefaultRoleChecker
import org.openurp.edu.eams.bootstrap.checker.ProjectChecker
import org.openurp.edu.eams.bootstrap.checker.SchoolChecker
import org.openurp.edu.eams.bootstrap.service.internal.BootstrapServiceImpl
import org.openurp.edu.eams.classroom.service.internal.RoomResourceServiceImpl
import org.openurp.edu.eams.core.service.internal.AdminclassServiceImpl
import org.openurp.edu.eams.core.service.internal.BaseInfoServiceImpl
import org.openurp.edu.eams.core.service.internal.RoomServiceImpl
import org.openurp.edu.eams.core.service.internal.DepartmentServiceImpl
import org.openurp.edu.eams.core.service.internal.SemesterServiceImpl
import org.openurp.edu.eams.core.service.internal.StudentServiceImpl
import org.openurp.edu.eams.core.service.internal.TeacherServiceImpl
import org.openurp.edu.eams.core.service.internal.TimeSettingServiceImpl
import org.openurp.edu.eams.fee.service.internal.TuitionServiceImpl
import org.openurp.edu.eams.system.mail.service.internal.MailServiceImpl
import org.openurp.edu.eams.system.msg.service.internal.SystemMessageConfigServiceImpl
import org.openurp.edu.eams.system.msg.service.internal.SystemMessageServiceImpl
import org.openurp.edu.eams.system.report.service.internal.ReportTemplateServiceImpl
import org.openurp.edu.eams.system.security.service.inernal.EamsUserServiceImpl



class EamsServiceModule extends AbstractBindModule {

  protected override def doBinding() {
    bind("semesterService", classOf[SemesterServiceImpl])
    bind("teacherService", classOf[TeacherServiceImpl])
    bind("baseInfoService", classOf[BaseInfoServiceImpl])
    bind("studentService", classOf[StudentServiceImpl])
    bind("departmentService", classOf[DepartmentServiceImpl])
    bind("systemMessageConfigService", classOf[SystemMessageConfigServiceImpl])
    bind("systemMessageService", classOf[SystemMessageServiceImpl])
    bind("classroomService", classOf[RoomServiceImpl])
    bind("adminclassService", classOf[AdminclassServiceImpl])
    bind("classroomResourceService", classOf[RoomResourceServiceImpl])
    bind(classOf[ReportTemplateServiceImpl])
    bind(classOf[TuitionServiceImpl])
    bind("mailService", classOf[MailServiceImpl])
    bind("mailSender", classOf[JavaMailSender])
    bind("eamsUserService", classOf[EamsUserServiceImpl])
    bind("timeSettingService", classOf[TimeSettingServiceImpl])
    bind("bootstrapService", classOf[BootstrapServiceImpl])
      .property("bootstrapCheckers", list(classOf[DefaultRoleChecker], classOf[SchoolChecker], classOf[CalendarChecker], 
      classOf[CampusChecker], classOf[ProjectChecker]))
  }
}
