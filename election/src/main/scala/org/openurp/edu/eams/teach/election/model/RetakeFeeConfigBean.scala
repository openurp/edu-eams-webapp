package org.openurp.edu.eams.teach.election.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.hibernate.annotations.NaturalId
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.fee.model.AbstractFeeConfigBean
import org.openurp.edu.eams.teach.election.RetakeFeeConfig




@SerialVersionUID(5754331927077968187L)
@Entity(name = "org.openurp.edu.eams.teach.election.RetakeFeeConfig")
class RetakeFeeConfigBean extends AbstractFeeConfigBean with RetakeFeeConfig {

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var project: Project = _

  
  var pricePerCredit: Int = _
}
