package org.openurp.edu.eams.teach.lesson.task.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import org.beangle.commons.entity.pojo.LongIdObject
import org.hibernate.annotations.NaturalId
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(722650431966747311L)
@Entity(name = "org.openurp.edu.eams.teach.lesson.task.model.LessonCollegeSwitch")
class LessonCollegeSwitch extends LongIdObject {

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var project: Project = _

  @NaturalId
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @BooleanBeanProperty
  var open: Boolean = _
}
