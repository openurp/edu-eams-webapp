package org.openurp.edu.eams.teach.grade.course.model




import org.beangle.data.model.bean.LongIdBean
import org.openurp.edu.base.Student




@SerialVersionUID(-3227647149353440137L)

class TotalCreditStats extends LongIdBean {

  
  
  var std: Student = _

  
  var tolCredit: java.lang.Float = _
}
