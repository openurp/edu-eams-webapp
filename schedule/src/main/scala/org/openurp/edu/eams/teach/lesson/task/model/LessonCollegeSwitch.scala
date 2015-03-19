package org.openurp.edu.eams.teach.lesson.task.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.NaturalId
import org.openurp.base.Semester
import org.openurp.edu.base.Project




@SerialVersionUID(722650431966747311L)
@Entity(name = "org.openurp.edu.eams.teach.lesson.task.model.LessonCollegeSwitch")
class LessonCollegeSwitch extends LongIdObject {

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var project: Project = _

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  
  var open: Boolean = _
}
