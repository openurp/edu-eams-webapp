package org.openurp.eams.grade.teacher

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.eams.grade.teacher.action.{EndGaAction, MakeupGaAction}

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[EndGaAction], classOf[MakeupGaAction])
  }
}
