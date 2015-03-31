package org.openurp.edu.eams.teach.grade.course.model




import org.beangle.data.model.bean.LongIdBean
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType




@SerialVersionUID(-6958826194584656198L)

class NotPassCreditStats extends LongIdBean {

  
  
  var std: Student = _

  
  
  var course: Course = _

  
  var credit: java.lang.Float = _

  
  
  var courseType: CourseType = _
}
