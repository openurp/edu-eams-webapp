package org.openurp.edu.eams.teach.election.model.constraint

import java.io.Serializable

import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.MapKeyJoinColumn
import javax.persistence.Table
import javax.validation.constraints.NotNull
import org.beangle.commons.collection.Collections
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.NaturalId
import org.openurp.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.base.code.CourseType




@SerialVersionUID(-8807239284763266997L)
@Entity(name = "org.openurp.edu.eams.teach.election.model.constraint.StdCourseCountConstraint")
@Table(name = "T_STD_COURSE_COUNT_CONS")
class StdCourseCountConstraint extends LongIdObject with Serializable {

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var std: Student = _

  @NotNull
  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  
  var maxCourseCount: java.lang.Integer = _

  @ElementCollection
  @MapKeyJoinColumn(name = "COURSE_TYPE_ID")
  @Column(name = "COURSE_COUNT")
  @CollectionTable(name = "T_CONS_COURS_TYPE_MAX_COUNT")
  
  var courseTypeMaxCourseCount: Map[CourseType, Integer] = Collections.newMap[Any]
}
