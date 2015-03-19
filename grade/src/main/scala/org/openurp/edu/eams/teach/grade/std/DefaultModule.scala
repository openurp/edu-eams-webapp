package org.openurp.edu.eams.teach.grade.std

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.teach.grade.std.web.action.IndexAction
import org.openurp.edu.eams.teach.grade.std.web.action.TranscriptAction



class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[IndexAction], classOf[TranscriptAction])
  }
}
