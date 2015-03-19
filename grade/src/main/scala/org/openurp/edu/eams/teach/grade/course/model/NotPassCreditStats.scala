package org.openurp.edu.eams.teach.grade.course.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.CourseType




@SerialVersionUID(-6958826194584656198L)
@Entity(name = "org.openurp.edu.eams.teach.grade.course.model.NotPassCreditStats")
class NotPassCreditStats extends LongIdObject {

  @ManyToOne(fetch = FetchType.LAZY)
  
  var std: Student = _

  @ManyToOne(fetch = FetchType.LAZY)
  
  var course: Course = _

  
  var credit: java.lang.Float = _

  @ManyToOne(fetch = FetchType.LAZY)
  
  var courseType: CourseType = _
}
