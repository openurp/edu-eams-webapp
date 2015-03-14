package org.openurp.edu.eams.teach.grade.course

import org.beangle.commons.entity.Entity
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait TeachQuality extends Entity[Long] {

  def getSemester(): Semester

  def setSemester(semester: Semester): Unit

  def getProject(): Project

  def setProject(project: Project): Unit

  def getTeacher(): Teacher

  def setTeacher(teacher: Teacher): Unit

  def getLesson(): Lesson

  def setLesson(lesson: Lesson): Unit

  def getState(): java.lang.Integer

  def setState(state: java.lang.Integer): Unit

  def getTest(): String

  def setTest(test: String): Unit

  def getStdLearn(): String

  def setStdLearn(stdLearn: String): Unit

  def getTeach(): String

  def setTeach(teach: String): Unit

  def getTeachExperience(): String

  def setTeachExperience(teachExperience: String): Unit
}
