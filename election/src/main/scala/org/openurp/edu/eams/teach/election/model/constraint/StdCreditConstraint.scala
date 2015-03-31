package org.openurp.edu.eams.teach.election.model.constraint






import org.openurp.base.Semester
import org.openurp.edu.base.Student




@SerialVersionUID(-6627564288570998553L)

class StdCreditConstraint extends AbstractCreditConstraint {

  
  
  
  
  var semester: Semester = _

  
  
  
  
  var std: Student = _

  
  var GPA: java.lang.Float = _
}
