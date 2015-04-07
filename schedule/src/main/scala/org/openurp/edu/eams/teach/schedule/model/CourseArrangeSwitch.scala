package org.openurp.edu.eams.teach.schedule.model




import org.beangle.data.model.bean.LongIdBean

import org.openurp.base.Semester
import org.openurp.edu.base.Project




//@SerialVersionUID(-1830248177687127758L)

class CourseArrangeSwitch extends LongIdBean {

  
  
  
  var semester: Semester = _

  
  
  
  var project: Project = _

  
  var published: Boolean = _

  def this(semester: Semester) {
    this()
    this.published = false
    this.semester = semester
  }

  def this(semester: Semester, project: Project) {
    this()
    this.published = false
    this.semester = semester
    this.project = project
  }
}
