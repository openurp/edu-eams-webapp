package org.openurp.edu.eams.teach.election.model

import java.util.Date


import org.beangle.data.model.bean.LongIdBean
import org.openurp.edu.eams.teach.election.model.Enum.StdApplyType




@SerialVersionUID(6692048840481469549L)

class StdApplyLog extends LongIdBean {

  
  
  var semesterId: java.lang.Integer = _

  
  
  var stdId: java.lang.Long = _

  
  
  var stdCode: String = _

  
  
  var stdName: String = _

  
  
  var courseCredit: java.lang.Float = _

  
  
  var courseCode: String = _

  
  
  var courseName: String = _

  
  
  var ip: String = _

  
  
  var applyOn: Date = _

  
  
  var applyType: StdApplyType = _

  
  
  var resultType: Int = _

  
  var remark: String = _

  
  
  var taskId: java.lang.Long = _
}
