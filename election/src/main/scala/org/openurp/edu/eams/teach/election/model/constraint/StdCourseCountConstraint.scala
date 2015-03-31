package org.openurp.edu.eams.teach.election.model.constraint

import java.io.Serializable


import org.beangle.commons.collection.Collections
import org.beangle.data.model.bean.LongIdBean

import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.base.code.CourseType



@SerialVersionUID(-8807239284763266997L)

class StdCourseCountConstraint extends LongIdBean with Serializable {

  
  
  
  
  var std: Student = _

  
  
  
  
  var semester: Semester = _

  
  var maxCourseCount: java.lang.Integer = _

  var courseTypeMaxCourseCount =  Collections.newMap[CourseType, Integer]
}
