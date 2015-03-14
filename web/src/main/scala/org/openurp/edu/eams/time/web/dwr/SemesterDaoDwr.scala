package org.openurp.edu.eams.time.web.dwr

import java.util.List

import scala.collection.JavaConversions._

trait SemesterDaoDwr {

  def getTermsOrderByDistance(calendarId: java.lang.Integer, year: String): List[_]

  def getYearsOrderByDistance(calendarId: java.lang.Integer): List[_]
}
