package org.openurp.edu.eams.teach.election.model



import org.beangle.data.model.bean.LongIdBean
import ElectMailTemplate._




object ElectMailTemplate {

  val WITHDRAW = 1L
}

@SerialVersionUID(-4430290657221915091L)

class ElectMailTemplate extends LongIdBean {

  
  
  var title: String = _

  
  
  var content: String = _
}
