package org.openurp.edu.eams.time.web.dwr





trait SemesterDaoDwr {

  def getTermsOrderByDistance(calendarId: java.lang.Integer, year: String): List[_]

  def getYearsOrderByDistance(calendarId: java.lang.Integer): List[_]
}
