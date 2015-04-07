package org.openurp.edu.eams.teach.lesson.task.model




import org.beangle.data.model.bean.LongIdBean

import org.openurp.base.Semester
import org.openurp.edu.base.Project




//@SerialVersionUID(722650431966747311L)

class LessonCollegeSwitch extends LongIdBean {

  var project: Project = _

  var semester: Semester = _

  var open: Boolean = _
}
