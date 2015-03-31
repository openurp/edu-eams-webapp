package org.openurp.edu.eams.teach.election.model





import org.beangle.commons.entity.pojo.NumberIdTimeObject
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.CourseTake




@SerialVersionUID(1L)

class ElectLoggerBean extends NumberIdTimeObject[Long] with ElectLogger {

  
  
  
  var project: Project = _

  
  
  
  var semester: Semester = _

  
  
  var lessonNo: String = _

  
  
  var courseType: String = _

  
  
  var courseCode: String = _

  
  
  var courseName: String = _

  
  var credits: Float = _

  
  var turn: java.lang.Integer = _

  
  
  var stdCode: String = _

  
  
  var stdName: String = _

  
  
  var operatorCode: String = _

  
  
  var operatorName: String = _

  
  
  var ipAddress: String = _

  
  
  var `type`: ElectRuleType = _

  
  
  
  var courseTakeType: CourseTakeType = _

  
  
  
  var electionMode: ElectionMode = _

  def setLoggerData(courseTake: CourseTake) {
    val course = courseTake.getLesson.getCourse
    setProject(courseTake.getLesson.getProject)
    setCourseCode(course.getCode)
    setCourseName(course.getName)
    setCourseTakeType(courseTake.getCourseTakeType)
    setCourseType(courseTake.getLesson.getCourseType.getName)
    setCredits(course.getCredits)
    setLessonNo(courseTake.getLesson.getNo)
    setStdCode(courseTake.getStd.getCode)
    setStdName(courseTake.getStd.getName)
    setSemester(courseTake.getLesson.getSemester)
  }
}
