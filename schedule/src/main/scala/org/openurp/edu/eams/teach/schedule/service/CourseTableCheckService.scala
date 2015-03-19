package org.openurp.edu.eams.teach.schedule.service


import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.security.DataRealm



trait CourseTableCheckService {

  def statCheckByDepart(semester: Semester, realm: DataRealm, project: Project): List[_]

  def statCheckBy(semester: Semester, 
      realm: DataRealm, 
      attr: String, 
      clazz: Class[_]): List[_]
}
