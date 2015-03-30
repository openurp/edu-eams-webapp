package org.openurp.edu.eams.teach.lesson.service

import java.io.Serializable


import org.beangle.data.model.Entity
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Teacher
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson



trait LessonService {

  def teachDepartsOfSemester(projects: Seq[Project], departments: Seq[Department], semester: Semester): Seq[Department]

  def courseTypesOfSemester(projects: Seq[Project], departments: Seq[Department], semester: Semester): Seq[CourseType]

  def attendDepartsOfSemester(projects: Seq[Project], semester: Semester): Seq[Department]

  def canAttendDepartsOfSemester(projects: Seq[Project], departments: Seq[Department], semester: Semester): Seq[Department]

  def getProjectsForTeacher(teacher: Teacher): Seq[Project]

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semesters: Iterable[Semester]): Seq[Lesson]

  def getLessonByCategory(id: Serializable, strategy: LessonFilterStrategy, semester: Semester): Seq[Lesson]

  def copy(lessons: Seq[Lesson], params: TaskCopyParams): Seq[Lesson]

  def getLessons[T <: Entity[_]](semester: Semester, entity: T): Seq[Lesson]

  def fillTeachers(teacherIds: Array[java.lang.Long], lesson: Lesson): Unit
}
