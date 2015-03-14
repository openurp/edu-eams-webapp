package org.openurp.edu.eams.teach.grade.course.model

import org.openurp.base.Department
import org.openurp.edu.eams.teach.code.industry.GradeType
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class GradeStateStat {

  @BeanProperty
  var department: Department = _

  @BeanProperty
  var unpublished: java.lang.Integer = _

  @BeanProperty
  var submited: java.lang.Integer = _

  @BeanProperty
  var published: java.lang.Integer = _

  @BeanProperty
  var gradeType: GradeType = _
}
