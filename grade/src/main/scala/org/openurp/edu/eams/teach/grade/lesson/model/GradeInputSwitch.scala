package org.openurp.edu.eams.teach.grade.lesson.model

import java.util.Date

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.beangle.commons.collection.Collections
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.teach.code.GradeType




@SerialVersionUID(6765368922449105678L)
@Entity(name = "org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch")
class GradeInputSwitch extends LongIdObject {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var project: Project = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  
  var startAt: Date = _

  
  var endAt: Date = _

  @ManyToMany
  
  var types: Set[GradeType] = Collections.newSet[Any]

  
  var opened: Boolean = _

  
  var needValidate: Boolean = false

  
  var remark: String = _

  def checkOpen(date: Date): Boolean = {
    if (null == getStartAt || null == getEndAt) {
      return false
    }
    if (date.after(getEndAt) || getStartAt.after(date)) {
      false
    } else {
      opened
    }
  }

  def checkOpen(): Boolean = checkOpen(new Date())
}
