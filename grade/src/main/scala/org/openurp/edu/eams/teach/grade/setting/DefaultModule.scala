package org.openurp.edu.eams.teach.grade.setting

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.teach.grade.setting.service.impl.CourseGradeSettingsImpl
import org.openurp.edu.eams.teach.grade.setting.web.action.ConfigAction
import org.openurp.edu.eams.teach.grade.setting.web.action.RateAction



class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[ConfigAction], classOf[RateAction])
    bind(classOf[CourseGradeSettingsImpl])
  }
}
