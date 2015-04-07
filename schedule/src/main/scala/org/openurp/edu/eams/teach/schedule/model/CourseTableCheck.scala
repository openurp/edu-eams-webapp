package org.openurp.edu.eams.teach.schedule.model

import java.util.Date






import org.beangle.data.model.bean.LongIdBean
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.lesson.Lesson




//@SerialVersionUID(-8117374983277305725L)

class CourseTableCheck extends LongIdBean() {

  
  
  
  var std: Student = _

  
  
  
  var semester: Semester = _

  
  var courseNum: Int = _

  
  var credits: Float = _

  
  var confirm: Boolean = true

  
  
  var remark: String = _

  
  var confirmAt: Date = _

  def this(std: Student, semester: Semester) {
    this()
    this.std = std
    this.semester = semester
    this.confirm = false
    this.courseNum = 0
    this.credits = 0
  }

  def updateCredit(lessons: List[Lesson]): Boolean = {
    var updated = false
    var newCourseNum = 0
    var newCredits = 0F
    for (lesson <- lessons) {
      newCourseNum += 1
      newCredits += lesson.course.credits
    }
    if (getCourseNum != newCourseNum) {
      setCourseNum(newCourseNum)
      updated = true
    }
    if (getCredits != newCredits) {
      setCredits(newCredits)
      updated = true
    }
    updated
  }
}
