package org.openurp.edu.eams.teach.election.model

import javax.persistence.Cacheable
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.openurp.base.Semester
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint




@SerialVersionUID(-8560491306359944914L)
@Entity(name = "org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
@Table(name = "T_COURSE_TYPE_CREDIT_CONS")
class CourseTypeCreditConstraintBean extends LongIdObject with CourseTypeCreditConstraint {

  @NotNull
  
  var grades: String = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var education: Education = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var courseType: CourseType = _

  
  var limitCredit: Float = _
}
