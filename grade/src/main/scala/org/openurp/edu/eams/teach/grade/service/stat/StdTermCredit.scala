package org.openurp.edu.eams.teach.grade.service.stat

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.base.Semester
import org.openurp.edu.base.Student




@SerialVersionUID(5594296033065495823L)
@Entity(name = "org.openurp.edu.eams.teach.grade.service.stat.StdTermCredit")
class StdTermCredit extends LongIdObject {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var std: Student = _

  
  var totalCredits: Float = _

  
  var credits: Float = _
}
