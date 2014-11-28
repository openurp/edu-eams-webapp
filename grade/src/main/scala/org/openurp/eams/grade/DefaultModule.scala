package org.openurp.eams.grade

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.eams.grade.teacher.action.{EndGaAction, MakeupGaAction}
import org.openurp.eams.grade.teacher.action.IndexAction
import org.openurp.eams.grade.setting.action.RateAction
import org.openurp.eams.grade.teacher.action.ReportAction

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[EndGaAction], classOf[MakeupGaAction]) 
    bind(classOf[IndexAction])
    bind(classOf[RateAction])
    bind(classOf[ReportAction])
  }
}
