package org.openurp.edu.eams.teach.schedule.model







import org.beangle.commons.entity.pojo.NumberIdTimeObject
import org.beangle.security.blueprint.User




@SerialVersionUID(-234456414948729061L)

class CourseMailSetting extends NumberIdTimeObject[Long] {

  
  
  
  var name: String = _

  
  
  
  var module: String = _

  
  
  var title: String = _

  
  
  
  var creator: User = _
}
