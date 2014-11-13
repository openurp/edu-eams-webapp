package org.openurp.eams.action

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.eams.BonusItem


class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[BonusItemAction])
  }
}