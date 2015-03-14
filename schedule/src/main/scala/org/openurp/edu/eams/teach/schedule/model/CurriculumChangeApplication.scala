package org.openurp.edu.eams.teach.schedule.model

import java.util.Date
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(828477765227607522L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.CurriculumChangeApplication")
class CurriculumChangeApplication extends LongIdObject {

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var teacher: Teacher = _

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var lesson: Lesson = _

  @Size(max = 300)
  @BeanProperty
  var requisition: String = _

  @NotNull
  @BeanProperty
  var time: Date = _

  @BeanProperty
  var passed: java.lang.Boolean = _

  @BeanProperty
  var schoolHours: Float = _

  @Size(max = 2000)
  @BeanProperty
  var remark: String = _
}
