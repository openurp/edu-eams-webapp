package org.openurp.edu.eams.teach.election

import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class OblivionCourseStat {

  @BeanProperty
  var required: java.lang.Float = _

  @BeanProperty
  var completed: java.lang.Float = _

  @BeanProperty
  var oblivion: java.lang.Float = _
}
