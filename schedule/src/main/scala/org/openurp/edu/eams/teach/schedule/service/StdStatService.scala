package org.openurp.edu.eams.teach.schedule.service


import org.openurp.edu.eams.system.security.DataRealm



trait StdStatService {

  def statOnCampusByStdType(dataRealm: DataRealm): List[_]

  def statOnCampusByDepart(dataRealm: DataRealm): List[_]

  def statOnCampusByStdTypeDepart(dataRealm: DataRealm): List[_]

  def statOnCampusByDepartStdType(dataRealm: DataRealm): List[_]
}
