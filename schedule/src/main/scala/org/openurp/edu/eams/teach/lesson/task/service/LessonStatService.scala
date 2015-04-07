package org.openurp.edu.eams.teach.lesson.task.service



import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.lesson.task.util.TaskOfCourseType
import org.openurp.edu.eams.util.stat.StatGroup



trait LessonStatService {

  def countByAdminclass(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_]

  def countByTeacher(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_]

  def countByCourseType(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_]

  def countByStdType(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_]

  def countByTeachDepart(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_]

  def statTeachDepartConfirm(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_]

  def statCourseTypeConfirm(project: Project, semester: Semester, dataRealm: DataRealm): Seq[_]

  def statTeacherTitle(project: Project, semesters: Seq[_]): Seq[_]

  def getTaskOfCourseTypes(project: Project, 
      semester: Semester, 
      dataRealm: DataRealm, 
      courseTypes: Iterable[_]): Seq[TaskOfCourseType]
}
