package org.openurp.edu.eams.teach.lesson.model


import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators._


class LessonLimitGroupPair {

  
  var lessonLimitGroup: LessonLimitGroup = _

  
  var gradeLimit: Pair[Operator, Seq[_]] = _

  
  var genderLimit: Pair[Operator, Seq[_]] = _

  
  var educationLimit: Pair[Operator, Seq[_]] = _

  
  var majorLimit: Pair[Operator, Seq[_]] = _

  
  var departmentLimit: Pair[Operator, Seq[_]] = _

  
  var adminclassLimit: Pair[Operator, Seq[_]] = _

  
  var directionLimit: Pair[Operator, Seq[_]] = _

  
  var programLimit: Pair[Operator, Seq[_]] = _

  
  var stdTypeLimit: Pair[Operator, Seq[_]] = _

  
  var stdLabelLimit: Pair[Operator, Seq[_]] = _

  def this(lessonLimitGroup: LessonLimitGroup) {
    this()
    this.lessonLimitGroup = lessonLimitGroup
  }
}
