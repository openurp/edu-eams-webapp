package org.openurp.edu.eams.teach.schedule.service


import org.openurp.edu.eams.system.security.DataRealm



trait StdStatService {

  def statOnCampusByStdType(dataRealm: DataRealm): Seq[_]

  def statOnCampusByDepart(dataRealm: DataRealm): Seq[_]

  def statOnCampusByStdTypeDepart(dataRealm: DataRealm): Seq[_]

  def statOnCampusByDepartStdType(dataRealm: DataRealm): Seq[_]
}
