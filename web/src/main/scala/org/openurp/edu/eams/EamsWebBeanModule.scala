package org.openurp.edu.eams

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.security.blueprint.service.impl.IdentifierDataResolver
import org.openurp.edu.eams.bootstrap.web.action.BootstrapAction
import org.openurp.edu.eams.core.web.action.AdminclassAction
import org.openurp.edu.eams.core.web.action.AdminclassSearchAction
import org.openurp.edu.eams.core.web.action.AdminclassStudentAction
import org.openurp.edu.eams.core.web.action.DirectionAction
import org.openurp.edu.eams.core.web.action.DirectionSearchAction
import org.openurp.edu.eams.core.web.action.MajorAction
import org.openurp.edu.eams.core.web.action.MajorSearchAction
import org.openurp.edu.eams.core.web.action.ProjectAction
import org.openurp.edu.eams.core.web.action.ProjectCodeAction
import org.openurp.edu.eams.dataQuery.web.action.DataQueryAction
import org.openurp.edu.eams.prompt.web.action.OopsAction
import org.openurp.edu.eams.system.firstlogin.impl.DefaultFirstLoginCheckService
import org.openurp.edu.eams.system.firstlogin.impl.DefaultPasswordValidator
import org.openurp.edu.eams.system.firstlogin.web.action.AccountInitCheckAction
import org.openurp.edu.eams.system.firstlogin.web.action.AccountMailVerifyAction
import org.openurp.edu.eams.system.message.web.action.SystemMessageAction
import org.openurp.edu.eams.system.message.web.action.SystemMessageConfigAction
import org.openurp.edu.eams.system.message.web.action.SystemMessageForStdAction
import org.openurp.edu.eams.system.message.web.action.SystemMessageForTeacherAction
import org.openurp.edu.eams.system.report.web.action.ReportTemplateAction
import org.openurp.edu.eams.system.security.web.action.MyAction
import org.openurp.edu.eams.system.security.web.action.ProfileAction
import org.openurp.edu.eams.system.security.web.action.RoleAction
import org.openurp.edu.eams.system.validate.service.impl.NumberChallengeGenerator
import org.openurp.edu.eams.system.validate.web.action.MailValidateAction
import org.openurp.edu.eams.system.web.action.DataTemplateAction
import org.openurp.edu.eams.system.web.action.DocumentAction
import org.openurp.edu.eams.system.web.action.DocumentDownloadAction
import org.openurp.edu.eams.system.web.action.HelpAction
import org.openurp.edu.eams.system.web.action.HomeAction
import org.openurp.edu.eams.system.web.action.NoticeAction
import org.openurp.edu.eams.system.web.action.NoticeSearchAction
import org.openurp.edu.eams.system.web.action.PreferenceAction
import org.openurp.edu.eams.web.helper.LogHelper
import org.openurp.edu.eams.web.helper.RestrictionHelperImpl
import org.openurp.edu.eams.web.helper.SemesterHelper
import org.openurp.edu.eams.web.helper.StdSearchHelper
import org.openurp.edu.eams.web.view.component.semester.ui.MenuSemesterCalendar
import org.openurp.edu.eams.web.view.component.semester.ui.SemesterUIFactory



class EamsWebBeanModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[DataQueryAction])
    bind(classOf[MajorSearchAction], classOf[DirectionSearchAction])
    bind(classOf[ProjectAction], classOf[ProjectCodeAction], classOf[MajorAction], classOf[DirectionAction])
    bind(classOf[SemesterHelper]).shortName()
    bind("restrictionHelper", classOf[RestrictionHelperImpl])
    bind(classOf[DocumentAction], classOf[DataTemplateAction], classOf[PreferenceAction], classOf[DocumentDownloadAction], 
      classOf[HelpAction])
    bind(classOf[NoticeAction], classOf[NoticeSearchAction], classOf[ReportTemplateAction])
    bind(classOf[HomeAction])
    bind(classOf[LogHelper])
    bind(classOf[SystemMessageConfigAction], classOf[SystemMessageAction], classOf[SystemMessageForStdAction], 
      classOf[SystemMessageForTeacherAction])
    bind("idDataResolver", classOf[IdentifierDataResolver])
    bind(classOf[org.openurp.edu.eams.web.action.api.TeacherAction])
    bind(classOf[org.openurp.edu.eams.web.action.api.MajorAction])
    bind(classOf[org.openurp.edu.eams.web.action.api.DirectionAction])
    bind("stdSearchHelper", classOf[StdSearchHelper])
    bind(classOf[AdminclassAction], classOf[AdminclassSearchAction], classOf[AdminclassStudentAction])
    SemesterUIFactory.register("MENU", new MenuSemesterCalendar())
    bind(classOf[BootstrapAction])
    bind("challengeGenerator", classOf[NumberChallengeGenerator])
    bind(classOf[MailValidateAction])
    bind(classOf[RoleAction], classOf[ProfileAction], classOf[MyAction])
    bind(classOf[OopsAction])
    bind("passwordValidator", classOf[DefaultPasswordValidator])
    bind(classOf[DefaultFirstLoginCheckService], classOf[AccountMailVerifyAction])
    bind("/accountInitCheck", classOf[AccountInitCheckAction])
  }
}
