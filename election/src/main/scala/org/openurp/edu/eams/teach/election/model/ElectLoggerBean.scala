package org.openurp.edu.eams.teach.election.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
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
@Entity(name = "org.openurp.edu.eams.teach.election.ElectLogger")
class ElectLoggerBean extends NumberIdTimeObject[Long] with ElectLogger {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var project: Project = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  @NotNull
  
  var lessonNo: String = _

  @NotNull
  
  var courseType: String = _

  @NotNull
  
  var courseCode: String = _

  @NotNull
  
  var courseName: String = _

  
  var credits: Float = _

  
  var turn: java.lang.Integer = _

  @NotNull
  
  var stdCode: String = _

  @NotNull
  
  var stdName: String = _

  @NotNull
  
  var operatorCode: String = _

  @NotNull
  
  var operatorName: String = _

  @NotNull
  
  var ipAddress: String = _

  @NotNull
  
  var `type`: ElectRuleType = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var courseTakeType: CourseTakeType = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
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
