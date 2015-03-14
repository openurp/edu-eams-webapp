package org.openurp.edu.eams.exception

import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class EamsException(@BeanProperty var i18nKey: String) extends RuntimeException()
