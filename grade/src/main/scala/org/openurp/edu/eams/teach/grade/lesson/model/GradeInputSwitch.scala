package org.openurp.edu.eams.teach.grade.lesson.model

import java.util.Date
import java.util.Set
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.code.industry.GradeType
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(6765368922449105678L)
@Entity(name = "org.openurp.edu.eams.teach.grade.lesson.model.GradeInputSwitch")
class GradeInputSwitch extends LongIdObject {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var project: Project = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @BeanProperty
  var startAt: Date = _

  @BeanProperty
  var endAt: Date = _

  @ManyToMany
  @BeanProperty
  var types: Set[GradeType] = CollectUtils.newHashSet()

  @BooleanBeanProperty
  var opened: Boolean = _

  @BooleanBeanProperty
  var needValidate: Boolean = false

  @BeanProperty
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
