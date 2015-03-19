package org.openurp.edu.eams.teach.program.major.dao


import org.openurp.edu.eams.system.security.DataRealm



trait MajorPlanStatDao {

  def statByDepart(realm: DataRealm, grade: String): List[_]

  def statByStdType(realm: DataRealm, grade: String): List[_]

  def getGrades(realm: DataRealm): List[_]
}
