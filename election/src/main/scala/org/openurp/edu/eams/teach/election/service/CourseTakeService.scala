package org.openurp.edu.eams.teach.election.service

import java.util.Collection
import java.util.List
import java.util.Map
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.event.Event
import org.beangle.commons.text.i18n.Message
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.base.CourseUnit
import org.openurp.edu.eams.base.Semester
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.eams.teach.election.model.Enum.AssignStdType
import org.openurp.edu.eams.teach.election.service.context.CourseTakeStat
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.util.OutputObserver

import scala.collection.JavaConversions._

trait CourseTakeService {

  def publish(event: Event): Unit

  def assignStds(lessons: Collection[Lesson], 
      `type`: AssignStdType, 
      semester: Semester, 
      observer: OutputObserver): Unit

  def assignStds(tasks: Collection[Lesson], semester: Semester, observer: OutputObserver): Unit

  def getCourseTakes(students: Collection[Student], semester: Semester): Map[Student, List[CourseTake]]

  def getCourseTakes(student: Student, semester: Semester, week: Int): List[CourseTake]

  def getCourseTakes(student: Student, semesters: Semester*): List[CourseTake]

  def getCourseTable(student: Student, 
      semester: Semester, 
      week: Int, 
      units: List[CourseUnit]): Array[Array[List[CourseTake]]]

  def getCourseTable(courseTakes: List[CourseTake], units: List[CourseUnit]): Array[Array[List[CourseTake]]]

  def election(student: Student, 
      existedCourseTakes: Collection[CourseTake], 
      lessonCollection: Collection[Lesson], 
      unCheckTimeConflict: Boolean): List[Message]

  def election(students: Collection[Student], 
      electedCourseTakes: Collection[CourseTake], 
      lesson: Lesson, 
      unCheckTimeConflict: Boolean): List[Message]

  def withdraw(courseTakes: List[CourseTake], sender: User): List[Message]

  def filter(amount: Int, takes: List[CourseTake], params: Map[String, Any]): List[Message]

  def getCourseTakesByTeacher(teacher: Teacher, 
      semester: Semester, 
      weekCondition: Condition, 
      project: Project): List[CourseTake]

  def getCourseTakesByAdminclass(semester: Semester, 
      weekCondition: Condition, 
      project: Project, 
      adminclasses: Adminclass*): List[CourseTake]

  def getCourseTakesByAdminclass(semester: Semester, 
      weekCondition: Condition, 
      project: Project, 
      adminclasses: Collection[Adminclass]): List[CourseTake]

  def getCourseTakesByAdminclassId(semester: Semester, 
      weekCondition: Condition, 
      project: Project, 
      adminclassIds: java.lang.Integer*): List[CourseTake]

  def getSystemMessageService(): SystemMessageService

  def stateGender(project: Project, semester: Semester, ids: java.lang.Long*): List[CourseTakeStat[String]]

  def stateGender(project: Project, semester: List[Semester], ids: java.lang.Long*): List[CourseTakeStat[String]]

  def stateGender(project: Project, 
      genders: List[Gender], 
      semesters: List[Semester], 
      ids: java.lang.Long*): List[CourseTakeStat[String]]
}
