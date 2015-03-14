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
import org.openurp.edu.eams.base.Semester
import org.openurp.code.edu.Education
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-8560491306359944914L)
@Entity(name = "org.openurp.edu.eams.teach.election.CourseTypeCreditConstraint")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "eams.teach")
@Table(name = "T_COURSE_TYPE_CREDIT_CONS")
class CourseTypeCreditConstraintBean extends LongIdObject with CourseTypeCreditConstraint {

  @NotNull
  @BeanProperty
  var grades: String = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var education: Education = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var courseType: CourseType = _

  @BeanProperty
  var limitCredit: Float = _
}
