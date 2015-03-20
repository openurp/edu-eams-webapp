package org.openurp.edu.eams.teach.lesson.task.service



import org.beangle.commons.entity.pojo.LongIdObject
import org.openurp.base.Semester
import org.openurp.edu.eams.base.code.school.RoomType
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType




@SerialVersionUID(7046399461690215375L)
class TaskGenParams extends LongIdObject {

  
  var semester: Semester = _

  
  var courseUnits: Int = _

  
  var weeks: Int = _

  
  var startWeek: Int = _

  
  var removeGenerated: Boolean = _

  
  var allowNoAdminclass: Boolean = _

  
  var omitSmallTerm: Boolean = true

  
  var onlyGenCourseTypes: Set[CourseType] = new HashSet[CourseType]()

  
  var dontGenCourses: Set[Course] = new HashSet[Course]()

  
  var onlyGenCourses: Set[Course] = new HashSet[Course]()

  
  var ignoreCloseRequest: Boolean = false

  private var classroomType: RoomType = _

  def getRoomType(): RoomType = classroomType

  def setRoomType(classroomType: RoomType) {
    this.classroomType = classroomType
  }
}
