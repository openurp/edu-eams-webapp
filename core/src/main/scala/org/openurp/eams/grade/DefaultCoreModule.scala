package org.openurp.eams.grade

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.eams.grade.service.impl.CourseGradeSettingsImpl
import org.openurp.eams.grade.service.internal.GradeRateServiceImpl
import org.openurp.eams.grade.service.internal.CourseGradeServiceImpl
import org.openurp.eams.grade.domain.impl.DefaultGradeTypePolicy
import org.openurp.teach.grade.domain.impl.GradeCourseTypeProviderImpl
import org.openurp.eams.grade.domain.impl.DefaultCourseGradeCalculator

class DefaultCoreModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[CourseGradeServiceImpl], classOf[GradeRateServiceImpl], classOf[CourseGradeSettingsImpl], classOf[DefaultGradeTypePolicy])
    bind(classOf[DefaultCourseGradeCalculator])
  }
}
