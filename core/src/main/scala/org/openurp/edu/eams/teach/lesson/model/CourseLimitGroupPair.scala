package org.openurp.edu.eams.teach.lesson.model


import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operator


class LessonLimitGroupPair {

  
  var lessonLimitGroup: LessonLimitGroup = _

  
  var gradeLimit: Pair[Operator, List[_]] = _

  
  var genderLimit: Pair[Operator, List[_]] = _

  
  var educationLimit: Pair[Operator, List[_]] = _

  
  var majorLimit: Pair[Operator, List[_]] = _

  
  var departmentLimit: Pair[Operator, List[_]] = _

  
  var adminclassLimit: Pair[Operator, List[_]] = _

  
  var directionLimit: Pair[Operator, List[_]] = _

  
  var programLimit: Pair[Operator, List[_]] = _

  
  var stdTypeLimit: Pair[Operator, List[_]] = _

  
  var stdLabelLimit: Pair[Operator, List[_]] = _

  def this(lessonLimitGroup: LessonLimitGroup) {
    this()
    this.lessonLimitGroup = lessonLimitGroup
  }
}
