package org.openurp.eams.grade

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.eams.grade.setting.action.RateAction
import org.openurp.eams.grade.teacher.action.{EndGaAction, IndexAction, MakeupGaAction, ReportAction}
import org.openurp.eams.grade.service.SimpleGradeCourseTypeProviderImpl
class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[EndGaAction], classOf[MakeupGaAction]) 
    bind(classOf[IndexAction])
    bind(classOf[RateAction])
    bind(classOf[ReportAction])
        //FIXME move to teach-core
    bind(classOf[SimpleGradeCourseTypeProviderImpl])
  }
}
