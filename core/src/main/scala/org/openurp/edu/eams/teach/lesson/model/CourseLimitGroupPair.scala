package org.openurp.edu.eams.teach.lesson.model

import java.util.List
import org.beangle.commons.lang.tuple.Pair
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CourseLimitGroupPair {

  @BeanProperty
  var courseLimitGroup: CourseLimitGroup = _

  @BeanProperty
  var gradeLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var genderLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var educationLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var majorLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var departmentLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var adminclassLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var directionLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var normalClassLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var programLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var stdTypeLimit: Pair[Operator, List[_]] = _

  @BeanProperty
  var stdLabelLimit: Pair[Operator, List[_]] = _

  def this(courseLimitGroup: CourseLimitGroup) {
    this()
    this.courseLimitGroup = courseLimitGroup
  }
}
