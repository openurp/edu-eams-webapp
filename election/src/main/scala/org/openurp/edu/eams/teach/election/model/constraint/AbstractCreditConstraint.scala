package org.openurp.edu.eams.teach.election.model.constraint

import org.beangle.data.model.bean.LongIdBean

@SerialVersionUID(6763813672438837820L)
abstract class AbstractCreditConstraint extends LongIdBean {
  
  var maxCredit: java.lang.Float = _
}
