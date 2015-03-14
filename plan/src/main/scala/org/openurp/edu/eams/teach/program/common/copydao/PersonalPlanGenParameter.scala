package org.openurp.edu.eams.teach.program.common.copydao

import java.lang.reflect.InvocationTargetException
import org.apache.commons.beanutils.PropertyUtils
import com.ekingstar.eams.core.Student
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenException
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

class PersonalPlanGenParameter extends MajorPlanGenParameter {

  @BeanProperty
  var std: Student = _

  def this(plan: MajorPlan) {
    this()
    PropertyUtils.copyProperties(this, plan)
  }
}
