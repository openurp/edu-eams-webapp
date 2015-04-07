package org.openurp.edu.eams.teach.lesson.task.service



import org.beangle.data.model.bean.LongIdBean
import org.openurp.base.Semester
import org.openurp.edu.base.Course
import org.openurp.edu.base.code.CourseType
import org.openurp.base.code.RoomType
import scala.collection.mutable.HashSet
import org.beangle.commons.collection.Collections




@SerialVersionUID(7046399461690215375L)
class TaskGenParams extends LongIdBean {

  
  var semester: Semester = _

  
  var courseUnits: Int = _

  
  var weeks: Int = _

  
  var startWeek: Int = _

  
  var removeGenerated: Boolean = _

  
  var allowNoAdminclass: Boolean = _

  
  var omitSmallTerm: Boolean = true

  
  var onlyGenCourseTypes: collection.mutable.Set[CourseType] = Collections.newSet[CourseType]

  
  var dontGenCourses: collection.mutable.Set[Course] = Collections.newSet[Course]

  
  var onlyGenCourses: collection.mutable.Set[Course] = Collections.newSet[Course]

  
  var ignoreCloseRequest: Boolean = false

  var classroomType: RoomType = _

}
