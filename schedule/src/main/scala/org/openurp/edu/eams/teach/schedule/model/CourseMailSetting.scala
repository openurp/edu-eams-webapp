package org.openurp.edu.eams.teach.schedule.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.NumberIdTimeObject
import org.beangle.security.blueprint.User




@SerialVersionUID(-234456414948729061L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.CourseMailSetting")
class CourseMailSetting extends NumberIdTimeObject[Long] {

  @NotNull
  @Column(unique = true)
  
  var name: String = _

  @NotNull
  @Size(max = 3000)
  
  var module: String = _

  @NotNull
  
  var title: String = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var creator: User = _
}
