package org.openurp.edu.eams.teach.schedule.service

import java.util.List
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.system.security.DataRealm

import scala.collection.JavaConversions._

trait CourseTableCheckService {

  def statCheckByDepart(semester: Semester, realm: DataRealm, project: Project): List[_]

  def statCheckBy(semester: Semester, 
      realm: DataRealm, 
      attr: String, 
      clazz: Class[_]): List[_]
}
