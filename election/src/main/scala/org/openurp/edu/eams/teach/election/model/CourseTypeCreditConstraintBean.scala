package org.openurp.edu.eams.teach.election.model

import javax.persistence.Cacheable





import org.beangle.data.model.bean.LongIdBean
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.openurp.base.Semester
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint




@SerialVersionUID(-8560491306359944914L)



class CourseTypeCreditConstraintBean extends LongIdBean with CourseTypeCreditConstraint {

  
  
  var grades: String = _

  
  
  
  var semester: Semester = _

  
  
  
  var education: Education = _

  
  
  
  var courseType: CourseType = _

  
  var limitCredit: Float = _
}
