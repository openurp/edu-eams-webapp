package org.openurp.edu.eams.teach.service

import java.util.Map
import org.openurp.edu.eams.teach.service.wrapper.TimeZone

import scala.collection.JavaConversions._

trait StdOccupyProvider {

  def getOccupyCount(stdSource: StudentSource, zone: TimeZone): Map[_,_]

  def getOccupyInfo(stdSource: StudentSource, zone: TimeZone): Map[_,_]
}
