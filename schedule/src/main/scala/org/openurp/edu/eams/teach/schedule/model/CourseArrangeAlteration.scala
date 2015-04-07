package org.openurp.edu.eams.teach.schedule.model

import java.util.Date

import org.beangle.data.model.bean.LongIdBean
import org.beangle.security.blueprint.User
import org.openurp.base.Semester

//@SerialVersionUID(1L)

class CourseArrangeAlteration extends LongIdBean {

  var lessonId: java.lang.Long = _

  var semester: Semester = _

  var alterationBefore: String = _

  var alterationAfter: String = _

  var alterBy: User = _

  var alterFrom: String = _

  var alterationAt: Date = _
}
