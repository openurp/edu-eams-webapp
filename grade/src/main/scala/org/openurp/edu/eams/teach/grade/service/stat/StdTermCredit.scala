package org.openurp.edu.eams.teach.grade.service.stat





import org.beangle.data.model.bean.LongIdBean
import org.openurp.base.Semester
import org.openurp.edu.base.Student




@SerialVersionUID(5594296033065495823L)

class StdTermCredit extends LongIdBean {

  
  
  
  var semester: Semester = _

  
  
  
  var std: Student = _

  
  var totalCredits: Float = _

  
  var credits: Float = _
}
