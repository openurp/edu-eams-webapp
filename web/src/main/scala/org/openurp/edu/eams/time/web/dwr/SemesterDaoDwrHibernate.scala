package org.openurp.edu.eams.time.web.dwr








import org.beangle.commons.entity.metadata.Model
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.edu.eams.base.Calendar



class SemesterDaoDwrHibernate extends HibernateEntityDao with SemesterDaoDwr {

  def getTermsOrderByDistance(calendarId: java.lang.Integer, year: String): List[_] = {
    val calendar = Model.newInstance(classOf[Calendar]).asInstanceOf[Calendar]
    calendar.setId(calendarId)
    val params = new HashMap()
    params.put("calendar", calendar)
    params.put("schoolYear", year)
    val rs = search("@getTermsOrderByDistance", params, true)
    rs
  }

  def getYearsOrderByDistance(calendarId: java.lang.Integer): List[_] = {
    val calendar = Model.newInstance(classOf[Calendar]).asInstanceOf[Calendar]
    calendar.setId(calendarId)
    val params = new HashMap()
    params.put("calendar", calendar)
    val rawYears = search("@getYearsOrderByDistance", params, true)
    val newYears = new ArrayList()
    val distinctYears = new HashSet()
    var iter = rawYears.iterator()
    while (iter.hasNext) {
      val schoolYear = iter.next().asInstanceOf[String]
      if (!distinctYears.contains(schoolYear)) {
        distinctYears.add(schoolYear)
        newYears.add(schoolYear)
      }
    }
    newYears
  }
}
