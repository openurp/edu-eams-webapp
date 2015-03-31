package org.openurp.edu.eams.teach.grade.lesson.model

import java.util.Date



import javax.persistence.ManyToMany


import org.beangle.commons.collection.Collections
import org.beangle.data.model.bean.LongIdBean
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.teach.code.GradeType




@SerialVersionUID(6765368922449105678L)

class GradeInputSwitch extends LongIdBean {

  
  
  
  var project: Project = _

  
  
  
  var semester: Semester = _

  
  var startAt: Date = _

  
  var endAt: Date = _

  
  
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
