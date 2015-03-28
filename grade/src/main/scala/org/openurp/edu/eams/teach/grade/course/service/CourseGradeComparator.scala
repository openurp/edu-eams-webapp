package org.openurp.edu.eams.teach.grade.course.service

import java.text.Collator
import java.util.Comparator



import org.apache.commons.beanutils.PropertyUtils
import org.beangle.commons.collection.Collections
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.web.helper.TeachClassGradeHelper
import org.openurp.edu.eams.teach.grade.service.stat.StdGrade
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.ExamGrade



class CourseGradeComparator(var cmpWhat: String, var isAsc: Boolean, gradeTypes: List[GradeType])
    extends Comparator[Any] {

  var gradeTypeMap: Map[String, GradeType] = Collections.newMap[Any]

  var iter = gradeTypes.iterator()
  while (iter.hasNext) {
    val gradeType = iter.next()
    gradeTypeMap.put(gradeType.id.toString, gradeType)
  }

  def compare(arg0: AnyRef, arg1: AnyRef): Int = {
    val g0 = arg0.asInstanceOf[CourseGrade]
    val g1 = arg1.asInstanceOf[CourseGrade]
    if (cmpWhat.startsWith("gradeType")) {
      val cmp = cmpWhat.substring(cmpWhat.indexOf(".") + 1)
      val gradeType = gradeTypeMap.get(cmp).asInstanceOf[GradeType]
      val eg0 = g0.getExamGrade(gradeType)
      val eg1 = g1.getExamGrade(gradeType)
      cmpScore((if (null == eg0) null else eg0.getScore), (if (eg1 == null) null else eg1.getScore), 
        isAsc)
    } else {
      val myCollator = Collator.getInstance
      val what0 = PropertyUtils.getProperty(arg0, cmpWhat)
      val what1 = PropertyUtils.getProperty(arg1, cmpWhat)
      if (isAsc) {
        myCollator.compare(if ((null == what0)) "" else what0.toString, if ((null == what1)) "" else what1.toString)
      } else {
        myCollator.compare(if ((null == what1)) "" else what1.toString, if ((null == what0)) "" else what0.toString)
      }
    }
  }

  private def cmpScore(score0: java.lang.Float, score1: java.lang.Float, isAsc: Boolean): Int = {
    val fs0 = if ((null == score0)) 0 else score0.floatValue()
    val fs1 = if ((null == score1)) 0 else score1.floatValue()
    if (isAsc) {
      java.lang.Float.compare(fs0, fs1)
    } else {
      java.lang.Float.compare(fs1, fs0)
    }
  }
}
