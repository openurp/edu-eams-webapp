package org.openurp.edu.eams.teach.grade.course.model

import javax.persistence.Entity
import org.beangle.commons.entity.pojo.LongIdObject




@SerialVersionUID(428221810871042136L)
@Entity(name = "org.openurp.edu.eams.teach.grade.course.model.ScoreSection")
class ScoreSection extends LongIdObject {

  
  var fromScore: Float = _

  
  var toScore: Float = _
}
