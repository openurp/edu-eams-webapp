package org.openurp.edu.eams.teach.program.major.service


import com.ekingstar.eams.system.security.DataRealm
//remove if not needed


trait MajorPlanStatService {

  def statByDepart(realm: DataRealm, grade: String): List[_]

  def statByStdType(realm: DataRealm, grade: String): List[_]

  def getGrades(realm: DataRealm): List[_]
}
