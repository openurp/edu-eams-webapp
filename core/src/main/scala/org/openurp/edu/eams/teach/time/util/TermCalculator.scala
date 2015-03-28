package org.openurp.edu.eams.teach.time.util

import java.sql.Date



import java.util.regex.Matcher
import java.util.regex.Pattern
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.openurp.base.Semester
import org.openurp.edu.eams.core.service.SemesterService
import TermCalculator._



object TermCalculator {

  private var termMap: Map[String, Set[Integer]] = Collections.newMap[Any]

  val autumn = Collections.newHashSet(1, 3, 5, 7, 9, 11)

  val spring = Collections.newHashSet(2, 3, 4, 8, 10, 12)

  val all = Collections.newHashSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

  termMap.put("春", spring)

  termMap.put("秋", autumn)

  termMap.put("春季", spring)

  termMap.put("春秋", all)

  termMap.put("春,秋", all)

  def inTerm(termStr: String, term: java.lang.Integer): Boolean = {
    if (Strings.contains(termStr, "*")) return true
    var termSet = termMap.get(termStr)
    if (null == termSet) {
      val terms = Strings.split(termStr, ",")
      termSet = new HashSet[Integer](3)
      for (one <- terms) {
        termSet.add(Numbers.toInt(one))
      }
      termMap.put(termStr, termSet)
    }
    termSet.contains(term)
  }

  def lessOrEqualTerm(termStr: String, term: java.lang.Integer): Boolean = {
    if (Strings.contains(termStr, "*")) return true
    var termSet = termMap.get(termStr)
    if (null == termSet) {
      val terms = Strings.split(termStr, ",")
      termSet = new HashSet[Integer](3)
      for (one <- terms) {
        termSet.add(Numbers.toInt(one))
      }
      termMap.put(termStr, termSet)
    }
    for (t <- termSet if t.compareTo(term) <= 0) return true
    false
  }
}

class TermCalculator(private var semesterService: SemesterService, private var semester: Semester)
    {

  protected val logger = LoggerFactory.getLogger(this.getClass)

  private var termCalcCache: Map[String, Integer] = Collections.newMap[Any]

  this.semester.calendar.id

  def getTermBetween(pre: Semester, post: Semester, omitSmallTerm: Boolean): Int = {
    semesterService.termsBetween(pre, post, omitSmallTerm)
  }

  def getTerm(begOn: java.util.Date, endOn: java.util.Date, omitSmallTerm: Boolean): Int = {
    var term = termCalcCache.get(begOn.toString + "~" + endOn.toString)
    if (term != null) {
      return term
    }
    val enrollSemester = semesterService.semester(semester.calendar, new Date(begOn.getTime), new Date(endOn.getTime))
    if (logger.isDebugEnabled) {
      logger.debug("calculate a term for [{}~{}]", begOn.toString, endOn.toString)
    }
    if (null == enrollSemester) {
      logger.info("cannot find enrollterm for grade {}~{}", begOn.toString, endOn.toString)
      term = new java.lang.Integer(-1)
    } else {
      term = new java.lang.Integer(semesterService.termsBetween(enrollSemester, semester, omitSmallTerm))
    }
    termCalcCache.put(begOn.toString + "~" + endOn.toString, term)
    if (term == null) -1 else term
  }

  def getTerm(date: java.util.Date, omitSmallTerm: Boolean): Int = {
    var term = termCalcCache.get(date.toString)
    termCalcCache.clear()
    if (term != null) {
      return term
    }
    val enrollSemester = semesterService.semester(semester.calendar, new Date(date.getTime))
    if (logger.isDebugEnabled) {
      logger.debug("calculate a term for [{}]", date.toString)
    }
    if (null == enrollSemester) {
      logger.info("cannot find enrollterm for grade {}", date.toString)
      term = new java.lang.Integer(-1)
    } else {
      term = new java.lang.Integer(semesterService.termsBetween(enrollSemester, semester, omitSmallTerm))
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
    val enrollSemester = semesterService.semester(semester.calendar, date)
    if (logger.isDebugEnabled) {
      logger.debug("calculate a term for [{}]", grade)
    }
    if (null == enrollSemester) {
      logger.info("cannot find enrollterm for grade {}", grade)
      term = new java.lang.Integer(-1)
    } else {
      term = new java.lang.Integer(semesterService.termsBetween(enrollSemester, semester, omitSmallTerm))
    }
    termCalcCache.put(grade, term)
    if (term == null) -1 else term
  }
}
