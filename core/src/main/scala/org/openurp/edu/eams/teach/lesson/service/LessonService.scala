package org.openurp.edu.eams.teach.lesson.service

import java.io.Serializable


import org.beangle.data.model.Entity
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson



trait LessonService {

  def teachDepartsOfSemester(projects: List[Project], departments: List[Department], semester: Semester): List[Department]

  def courseTypesOfSemester(projects: List[Project], departments: List[Department], semester: Semester): List[CourseType]

  def attendDepartsOfSemester(projects: List[Project], semester: Semester): List[Department]

  def canAttendDepartsOfSemester(projects: List[Project], departments: List[Department], semester: Semester): List[Department]

  def getProjectsForTeacher(teacher: Teacher): List[Project]

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semesters: Iterable[Semester]): List[Lesson]

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semester: Semester): List[Lesson]

  def copy(lessons: List[Lesson], params: TaskCopyParams): List[Lesson]

  def getLessons[T <: Entity[_]](semester: Semester, entity: T): List[Lesson]

  def fillTeachers(teacherIds: Array[Long], lesson: Lesson): Unit
}
