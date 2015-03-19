package org.openurp.edu.eams.teach.grade.search

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.teach.grade.search.web.action.GpaAction
import org.openurp.edu.eams.teach.grade.search.web.action.GpaStatAction
import org.openurp.edu.eams.teach.grade.search.web.action.MultiStdAction



class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[GpaAction], classOf[GpaStatAction], classOf[MultiStdAction])
  }
}
