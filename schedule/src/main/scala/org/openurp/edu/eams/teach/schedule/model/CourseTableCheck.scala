package org.openurp.edu.eams.teach.schedule.model

import java.util.Date
import java.util.List
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Student
import org.openurp.edu.teach.lesson.Lesson
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(-8117374983277305725L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.CourseTableCheck")
class CourseTableCheck extends LongIdObject() {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var std: Student = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @BeanProperty
  var courseNum: Int = _

  @BeanProperty
  var credits: Float = _

  @BooleanBeanProperty
  var confirm: Boolean = true

  @Size(max = 500)
  @BeanProperty
  var remark: String = _

  @BeanProperty
  var confirmAt: Date = _

  def this(std: Student, semester: Semester) {
    super()
    this.std = std
    this.semester = semester
    this.confirm = false
    this.courseNum = 0
    this.credits = 0
  }

  def updateCredit(lessons: List[Lesson]): Boolean = {
    var updated = false
    var newCourseNum = 0
    var newCredits = 0
    for (lesson <- lessons) {
      newCourseNum += 1
      newCredits += lesson.getCourse.getCredits
    }
    if (getCourseNum != newCourseNum) {
      setCourseNum(newCourseNum)
      updated = true
    }
    if (getCredits != newCredits) {
      setCredits(newCredits)
      updated = true
    }
    updated
  }
}
