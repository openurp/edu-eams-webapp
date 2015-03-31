package org.openurp.edu.eams.teach.election.model






import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.fee.model.AbstractFeeConfigBean
import org.openurp.edu.eams.teach.election.RetakeFeeConfig




@SerialVersionUID(5754331927077968187L)

class RetakeFeeConfigBean extends AbstractFeeConfigBean with RetakeFeeConfig {

  
  
  
  
  var semester: Semester = _

  
  
  
  
  var project: Project = _

  
  var pricePerCredit: Int = _
}
