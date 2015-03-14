package org.openurp.edu.eams.teach.grade.adminclass

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.teach.grade.adminclass.web.action.AlertAction
import org.openurp.edu.eams.teach.grade.adminclass.web.action.AllAction
import org.openurp.edu.eams.teach.grade.adminclass.web.action.IndexAction
import org.openurp.edu.eams.teach.grade.adminclass.web.action.TermAction

import scala.collection.JavaConversions._

class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[IndexAction], classOf[AlertAction], classOf[TermAction], classOf[AllAction])
  }
}
