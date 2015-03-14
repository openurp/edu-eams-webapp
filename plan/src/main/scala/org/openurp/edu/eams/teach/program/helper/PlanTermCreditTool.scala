package org.openurp.edu.eams.teach.program.helper

import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator
import java.util.Iterator
import java.util.List
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
//remove if not needed
import scala.collection.JavaConversions._

object PlanTermCreditTool {

  def normalizeTerms(terms: String): String = {
    val arr = terms.replaceAll("\\s", "").replaceAll("^,", "").replaceAll(",$", "")
      .split(",")
    try {
      Arrays.sort(arr, new Comparator[String]() {

        def compare(o1: String, o2: String): Int = {
          return java.lang.Integer.valueOf(o1) - java.lang.Integer.valueOf(o2)
        }
      })
    } catch {
      case e: Exception => return ',' + Strings.join(arr, ',') + ','
    }
    if (arr.length == 1 && arr(0) == "*") {
      return arr(0)
    }
    ',' + Strings.join(arr, ',') + ','
  }

  def mergeTermCredits(termCredits1: String, termCredits2: String): String = {
    if (Strings.isEmpty(termCredits1)) {
      return termCredits2
    }
    if (Strings.isEmpty(termCredits2)) {
      return termCredits1
    }
    val credits1 = termCredits1.replaceAll("^,", "").replaceAll(",$", "")
      .split(",")
    val credits2 = termCredits2.replaceAll("^,", "").replaceAll(",$", "")
      .split(",")
    val credits3 = Array.ofDim[String](credits1.length)
    for (i <- 0 until credits1.length) {
      try {
        credits3(i) = String.valueOf(java.lang.Float.valueOf(credits1(i)) + java.lang.Float.valueOf(credits2(i)))
          .replaceAll(".0$", "")
      } catch {
        case e: Exception => credits3(i) = credits2(i)
      }
    }
    "," + Strings.join(credits3, ",") + ","
  }

  def buildCourseGroupTermCredits(termCredits: Array[Float], oldTermsCount: java.lang.Integer, newTermsCount: java.lang.Integer): String = {
    val result = new StringBuffer(",")
    if (newTermsCount < oldTermsCount) {
      val count = oldTermsCount - newTermsCount
      var remain = 0f
      for (i <- 0 until count) {
        remain += termCredits(newTermsCount + i)
      }
      for (i <- 0 until newTermsCount) {
        if (i == newTermsCount - 1) {
          result.append(termCredits(i) + remain + ",")
        } else {
          result.append(termCredits(i) + ",")
        }
      }
    } else {
      for (i <- 0 until oldTermsCount) {
        result.append(termCredits(i) + ",")
      }
      val count = newTermsCount - oldTermsCount
      for (i <- 0 until count) {
        result.append("0,")
      }
    }
    result.toString.replace(".0", "")
  }

  def buildPlanCourseTerms(terms: String, oldTermsCount: java.lang.Integer, newTermsCount: java.lang.Integer): String = {
    if (newTermsCount < oldTermsCount) {
      val termArr = Strings.splitToInt(terms)
      for (i <- 0 until termArr.length if termArr(i) > newTermsCount) {
        termArr(i) = newTermsCount
      }
      var prev = -1
      val termArr_ = new ArrayList[Integer]()
      for (i <- 0 until termArr.length if prev != termArr(i)) {
        termArr_.add(termArr(i))
        prev = termArr(i)
      }
      return "," + Strings.join(termArr_.toArray(Array()), ',') + ","
    }
    terms
  }

  def updateTermsCount(plan: CoursePlan, 
      oldTermsCount: java.lang.Integer, 
      newTermsCount: java.lang.Integer, 
      entityDao: EntityDao) {
    var it = plan.getGroups.iterator()
    while (it.hasNext) {
      val group = it.next().asInstanceOf[MajorPlanCourseGroup]
      val newCreditPerTerms = PlanTermCreditTool.buildCourseGroupTermCredits(transformToFloat(group.getTermCredits), 
        oldTermsCount, newTermsCount)
      group.setTermCredits(newCreditPerTerms)
      for (planCourse <- group.getPlanCourses) {
        val terms = PlanTermCreditTool.buildPlanCourseTerms(planCourse.getTerms, oldTermsCount, newTermsCount)
        planCourse.setTerms(PlanTermCreditTool.normalizeTerms(terms))
        entityDao.saveOrUpdate(planCourse)
      }
      entityDao.saveOrUpdate(group)
    }
  }

  def transformToFloat(strIds: String): Array[Float] = {
    val ids = Strings.split(strIds, ",")
    val idsOfFloat = Array.ofDim[Float](ids.length)
    for (i <- 0 until ids.length) {
      idsOfFloat(i) = new java.lang.Float(ids(i))
    }
    idsOfFloat
  }
}
