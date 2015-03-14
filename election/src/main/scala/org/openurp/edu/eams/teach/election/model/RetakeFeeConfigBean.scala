package org.openurp.edu.eams.teach.election.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.hibernate.annotations.NaturalId
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.fee.model.AbstractFeeConfigBean
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(5754331927077968187L)
@Entity(name = "org.openurp.edu.eams.teach.election.RetakeFeeConfig")
class RetakeFeeConfigBean extends AbstractFeeConfigBean with RetakeFeeConfig {

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var project: Project = _

  @BeanProperty
  var pricePerCredit: Int = _
}
