package org.openurp.edu.eams.teach.lesson.model


import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator


class CourseLimitGroupPair {

  
  var courseLimitGroup: CourseLimitGroup = _

  
  var gradeLimit: Pair[Operator, List[_]] = _

  
  var genderLimit: Pair[Operator, List[_]] = _

  
  var educationLimit: Pair[Operator, List[_]] = _

  
  var majorLimit: Pair[Operator, List[_]] = _

  
  var departmentLimit: Pair[Operator, List[_]] = _

  
  var adminclassLimit: Pair[Operator, List[_]] = _

  
  var directionLimit: Pair[Operator, List[_]] = _

  
  var normalClassLimit: Pair[Operator, List[_]] = _

  
  var programLimit: Pair[Operator, List[_]] = _

  
  var stdTypeLimit: Pair[Operator, List[_]] = _

  
  var stdLabelLimit: Pair[Operator, List[_]] = _

  def this(courseLimitGroup: CourseLimitGroup) {
    this()
    this.courseLimitGroup = courseLimitGroup
  }
}
