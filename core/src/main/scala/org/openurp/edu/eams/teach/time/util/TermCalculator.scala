package org.openurp.edu.eams.teach.time.util

import java.sql.Date
import java.util.regex.Pattern

import scala.annotation.migration

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.openurp.base.Semester
import org.openurp.edu.eams.core.service.SemesterService

object TermCalculator {

  private var termMap = Collections.newMap[String, Set[Integer]]

  val autumn = Set(1, 3, 5, 7, 9, 11)

  val spring = Set(2, 3, 4, 8, 10, 12)

  val all = Set(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

  termMap.put("春", spring.map { a => Integer.valueOf(a) })

  termMap.put("秋", autumn.map { a => Integer.valueOf(a) })

  termMap.put("春季", spring.map { a => Integer.valueOf(a) })

  termMap.put("春秋", all.map { a => Integer.valueOf(a) })

  termMap.put("春,秋", all.map { a => Integer.valueOf(a) })

  def inTerm(termStr: String, term: java.lang.Integer): Boolean = {
    if (Strings.contains(termStr, "*")) return true
    var termSet = termMap.get(termStr).orNull
    if (null == termSet) {
      val terms = Strings.split(termStr, ",")
      val newSet = Collections.newSet[Integer]
      for (one <- terms) {
        newSet += Numbers.convert2Int(one)
      }
      termMap.put(termStr, newSet.toSet)
    }
    termSet.contains(term)
  }

  def lessOrEqualTerm(termStr: String, term: java.lang.Integer): Boolean = {
    if (Strings.contains(termStr, "*")) return true
    var termSet = termMap.get(termStr).orNull
    if (null == termSet) {
      val terms = Strings.split(termStr, ",")
      val newtermSet = Collections.newSet[Integer]
      for (one <- terms) {
        newtermSet += Numbers.convert2Int(one)
      }
      termMap.put(termStr, newtermSet.toSet)
    }
    termSet.exists { t => t.compareTo(term) <= 0 }
  }
}

class TermCalculator(private var semesterService: SemesterService, private var semester: Semester) extends Logging {

  private var termCalcCache = Collections.newMap[String, Integer]

  def getTermBetween(pre: Semester, post: Semester, omitSmallTerm: Boolean): Int = {
    semesterService.getTermsBetween(pre, post, omitSmallTerm)
  }

  def getTerm(begOn: java.util.Date, endOn: java.util.Date, omitSmallTerm: Boolean): Int = {
    var term = termCalcCache.get(begOn.toString + "~" + endOn.toString).orNull
    if (term != null) {
      return term
    }
    val enrollSemester = semesterService.getSemester(semester.calendar, new Date(begOn.getTime), new Date(endOn.getTime))
    debug(s"calculate a term for [$begOn~$endOn]")
    if (null == enrollSemester) {
      info(s"cannot find enrollterm for grade $begOn~$endOn")
      term = new java.lang.Integer(-1)
    } else {
      term = new java.lang.Integer(semesterService.getTermsBetween(enrollSemester, semester, omitSmallTerm))
    }
    termCalcCache.put(begOn.toString + "~" + endOn.toString, term)
    if (term == null) -1 else term
  }

  def getTerm(date: java.util.Date, omitSmallTerm: Boolean): Int = {
    var term = termCalcCache.get(date.toString).orNull
    //    termCalcCache.clear()
    if (term != null) {
      return term
    }
    val enrollSemester = semesterService.getSemester(semester.calendar, new Date(date.getTime))
    debug("calculate a term for [$date]")
    if (null == enrollSemester) {
      info("cannot find enrollterm for grade $date")
      term = new java.lang.Integer(-1)
    } else {
      term = new java.lang.Integer(semesterService.getTermsBetween(enrollSemester, semester, omitSmallTerm))
    }
    termCalcCache.put(date.toString, term)
    if (term == null) -1 else term
  }

  @Deprecated
  def getTerm(grade: String, omitSmallTerm: Boolean): Int = {
    var dateString = grade + "-29"
    var term = termCalcCache.get(grade).asInstanceOf[java.lang.Integer]
    if (term != null) {
      return term
    }
    val datePattern = Pattern.compile("(\\d+)-(\\d+)-(\\d+)")
    val matcher = datePattern.matcher(dateString)
    matcher.matches()
    val month = matcher.group(2)
    if (1 == month.length) {
      dateString = if (month == "2") matcher.group(1) + "-0" + month + '-' + "28" else matcher.group(1) + "-0" + month + '-' + matcher.group(3)
    }
    val date = Date.valueOf(dateString)
    val enrollSemester = semesterService.getSemester(semester.calendar, date)
    debug(s"calculate a term for [$grade]")
    if (null == enrollSemester) {
      info(s"cannot find enrollterm for grade grade")
      term = new java.lang.Integer(-1)
    } else {
      term = new java.lang.Integer(semesterService.getTermsBetween(enrollSemester, semester, omitSmallTerm))
    }
    termCalcCache.put(grade, term)
    if (term == null) -1 else term
  }
}
