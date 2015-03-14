package org.openurp.edu.eams.teach.schedule.model

import java.util.List
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.base.util.WeekDay
import org.openurp.edu.eams.teach.lesson.CourseTime
import CourseTableSetting._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object CourseTableSetting {

  val VERTICAL = "vertical"

  val HORIZONTAL = "horizontal"

  val ALLINONE = "single"
}

@SerialVersionUID(1L)
class CourseTableSetting extends LongIdObject() {

  @BeanProperty
  var tablePerPage: Int = 1

  @BeanProperty
  var fontSize: Int = 12

  @BeanProperty
  var style: String = HORIZONTAL

  @BeanProperty
  var kind: String = _

  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @BeanProperty
  var weekdays: List[WeekDay] = _

  @BeanProperty
  var displaySemesterTime: Boolean = _

  @BeanProperty
  var forSemester: Boolean = true

  @BeanProperty
  var ignoreTask: Boolean = false

  @BeanProperty
  var times: Array[CourseTime] = _

  @BeanProperty
  var orderBy: String = _

  def this(semester: Semester) {
    this()
    setSemester(semester)
  }
}
