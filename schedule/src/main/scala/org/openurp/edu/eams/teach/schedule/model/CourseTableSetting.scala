package org.openurp.edu.eams.teach.schedule.model




import org.beangle.data.model.bean.LongIdBean
import org.openurp.base.Semester
import CourseTableSetting._




object CourseTableSetting {

  val VERTICAL = "vertical"

  val HORIZONTAL = "horizontal"

  val ALLINONE = "single"
}

@SerialVersionUID(1L)
class CourseTableSetting extends LongIdBean() {

  
  var tablePerPage: Int = 1

  
  var fontSize: Int = 12

  
  var style: String = HORIZONTAL

  
  var kind: String = _

  
  
  var semester: Semester = _

  
  var weekdays: List[WeekDay] = _

  
  var displaySemesterTime: Boolean = _

  
  var forSemester: Boolean = true

  
  var ignoreTask: Boolean = false

  
  var times: Array[CourseTime] = _

  
  var orderBy: String = _

  def this(semester: Semester) {
    this()
    setSemester(semester)
  }
}
