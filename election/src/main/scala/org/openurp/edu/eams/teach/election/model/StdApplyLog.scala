package org.openurp.edu.eams.teach.election.model

import java.util.Date
import javax.persistence.Entity
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.teach.election.model.Enum.StdApplyType




@SerialVersionUID(6692048840481469549L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.StdApplyLog")
class StdApplyLog extends LongIdObject {

  @NotNull
  
  var semesterId: java.lang.Integer = _

  @NotNull
  
  var stdId: java.lang.Long = _

  @NotNull
  
  var stdCode: String = _

  @NotNull
  
  var stdName: String = _

  @NotNull
  
  var courseCredit: java.lang.Float = _

  @NotNull
  
  var courseCode: String = _

  @NotNull
  
  var courseName: String = _

  @NotNull
  
  var ip: String = _

  @NotNull
  
  var applyOn: Date = _

  @NotNull
  
  var applyType: StdApplyType = _

  @NotNull
  
  var resultType: Int = _

  
  var remark: String = _

  @NotNull
  
  var taskId: java.lang.Long = _
}
