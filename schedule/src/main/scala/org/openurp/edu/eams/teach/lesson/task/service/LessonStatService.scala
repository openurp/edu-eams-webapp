package org.openurp.edu.eams.teach.lesson.task.service



import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.lesson.task.util.TaskOfCourseType
import org.openurp.edu.eams.util.stat.StatGroup



trait LessonStatService {

  def countByAdminclass(project: Project, semester: Semester, dataRealm: DataRealm): List[_]

  def countByTeacher(project: Project, semester: Semester, dataRealm: DataRealm): List[_]

  def countByCourseType(project: Project, semester: Semester, dataRealm: DataRealm): List[_]

  def countByStdType(project: Project, semester: Semester, dataRealm: DataRealm): List[_]

  def countByTeachDepart(project: Project, semester: Semester, dataRealm: DataRealm): List[_]

  def statTeachDepartConfirm(project: Project, semester: Semester, dataRealm: DataRealm): List[_]

  def statCourseTypeConfirm(project: Project, semester: Semester, dataRealm: DataRealm): List[_]

  def statTeacherTitle(project: Project, semesters: List[_]): List[_]

  def getTaskOfCourseTypes(project: Project, 
      semester: Semester, 
      dataRealm: DataRealm, 
      courseTypes: Iterable[_]): List[TaskOfCourseType]
}
