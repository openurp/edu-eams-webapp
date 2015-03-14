package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-6253573939352498929L)
class ElectCourseSubstitution extends Serializable {

  @BeanProperty
  var origins: Set[Long] = CollectUtils.newHashSet()

  @BeanProperty
  var substitutes: Set[Long] = CollectUtils.newHashSet()
}
