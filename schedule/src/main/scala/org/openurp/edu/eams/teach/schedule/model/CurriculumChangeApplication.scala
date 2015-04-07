package org.openurp.edu.eams.teach.schedule.model

import java.util.Date





import org.beangle.data.model.bean.LongIdBean
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson




//@SerialVersionUID(828477765227607522L)

class CurriculumChangeApplication extends LongIdBean {

  
  
  var teacher: Teacher = _

  
  
  var lesson: Lesson = _

  
  
  var requisition: String = _

  
  
  var time: Date = _

  
  var passed: java.lang.Boolean = _

  
  var schoolHours: Float = _

  
  
  var remark: String = _
}
