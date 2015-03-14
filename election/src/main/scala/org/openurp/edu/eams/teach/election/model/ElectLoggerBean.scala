package org.openurp.edu.eams.teach.election.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import org.beangle.commons.entity.pojo.NumberIdTimeObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.ElectLogger
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.CourseTake
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

@SerialVersionUID(1L)
@Entity(name = "org.openurp.edu.eams.teach.election.ElectLogger")
class ElectLoggerBean extends NumberIdTimeObject[Long] with ElectLogger {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var project: Project = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NotNull
  @BeanProperty
  var lessonNo: String = _

  @NotNull
  @BeanProperty
  var courseType: String = _

  @NotNull
  @BeanProperty
  var courseCode: String = _

  @NotNull
  @BeanProperty
  var courseName: String = _

  @BeanProperty
  var credits: Float = _

  @BeanProperty
  var turn: java.lang.Integer = _

  @NotNull
  @BeanProperty
  var stdCode: String = _

  @NotNull
  @BeanProperty
  var stdName: String = _

  @NotNull
  @BeanProperty
  var operatorCode: String = _

  @NotNull
  @BeanProperty
  var operatorName: String = _

  @NotNull
  @BeanProperty
  var ipAddress: String = _

  @NotNull
  @BeanProperty
  var `type`: ElectRuleType = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var courseTakeType: CourseTakeType = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
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
