package org.openurp.edu.eams.teach.program.major.service

import java.sql.Date
import org.openurp.base.Department
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Student
import org.openurp.code.edu.Education
import org.openurp.edu.eams.core.code.nation.Degree
import org.openurp.edu.eams.core.code.nation.StudyType
import org.openurp.edu.base.code.StdType
import org.openurp.edu.base.Program
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class MajorPlanGenParameter {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var grade: String = _

  @BeanProperty
  var education: Education = _

  @BeanProperty
  var stdType: StdType = _

  @BeanProperty
  var department: Department = _

  @BeanProperty
  var major: Major = _

  @BeanProperty
  var direction: Direction = _

  @BeanProperty
  var effectiveOn: Date = _

  @BeanProperty
  var invalidOn: Date = _

  @BeanProperty
  var duration: Float = _

  @BeanProperty
  var studyType: StudyType = _

  @BeanProperty
  var degree: Degree = _

  @BeanProperty
  var student: Student = _

  @BeanProperty
  var termsCount: Int = _
}
