package org.openurp.edu.eams.teach.service


import org.openurp.edu.eams.teach.service.wrapper.TimeZone



trait StdOccupyProvider {

  def getOccupyCount(stdSource: StudentSource, zone: TimeZone): Map[_,_]

  def getOccupyInfo(stdSource: StudentSource, zone: TimeZone): Map[_,_]
}
