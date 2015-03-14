package org.openurp.edu.eams.teach.grade.service.impl

import java.util.Collections
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.script.ExpressionEvaluator
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

class ScriptGradeFilter extends GradeFilter() {

  var script: String = _

  var expressionEvaluator: ExpressionEvaluator = _

  def this(script: String, expressionEvaluator: ExpressionEvaluator) {
    super()
    this.script = script
    this.expressionEvaluator = expressionEvaluator
  }

  def filter(grades: List[CourseGrade]): List[CourseGrade] = {
    if (Strings.isEmpty(script)) return grades
    val newGrades = CollectUtils.newArrayList()
    for (grade <- grades) {
      val rs = expressionEvaluator.eval(script, Collections.singletonMap("grade", grade), classOf[Boolean])
      if (rs.booleanValue()) newGrades.add(grade)
    }
    newGrades
  }

  def setScript(script: String) {
    this.script = script
  }

  def setExpressionEvaluator(expressionEvaluator: ExpressionEvaluator) {
    this.expressionEvaluator = expressionEvaluator
  }

  def getScript(): String = script
}
