package org.openurp.eams.grade.std.action

import org.beangle.commons.inject.bind.AbstractBindModule

class DefaultModule extends AbstractBindModule {
  protected override def binding() {
    bind(classOf[IndexAction])
  }
}