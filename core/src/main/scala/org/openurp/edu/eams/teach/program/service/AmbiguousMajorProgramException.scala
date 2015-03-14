package org.openurp.edu.eams.teach.program.service

import java.util.List
import org.openurp.edu.base.Program
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-6050167055646192463L)
class AmbiguousMajorProgramException(@BeanProperty val ambiguousPrograms: List[Program])
    extends Exception()
