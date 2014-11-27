package org.openurp.eams.grade

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.eams.grade.service.impl.CourseGradeSettingsImpl

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[CourseGradeSettingsImpl]) 
  }
}
