package org.openurp.edu.eams.teach.planaudit.service

import java.util.Map
import org.beangle.ems.rule.model.SimpleContext
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.planaudit.PlanAuditResult
import org.openurp.edu.eams.teach.planaudit.model.PlanAuditStandard
import org.openurp.edu.teach.plan.CoursePlan
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class PlanAuditContext extends SimpleContext() {

  @BeanProperty
  var std: Student = _

  @BeanProperty
  var standard: PlanAuditStandard = _

  @BeanProperty
  var coursePlan: CoursePlan = _

  @BeanProperty
  var result: PlanAuditResult = _

  @BeanProperty
  var stdGrade: StdGrade = _

  @BooleanBeanProperty
  var instantAudit: Boolean = _

  @BooleanBeanProperty
  var auto: Boolean = _

  @BooleanBeanProperty
  var partial: Boolean = _

  @BeanProperty
  var auditTerms: Array[String] = _

  def getParam[T](paramName: String, clazz: Class[T]): T = {
    getParams.get(paramName).asInstanceOf[T]
  }

  def this(params: Map[String, Any]) {
    super()
    getParams.putAll(params)
  }

  def this(coursePlan: CoursePlan, stdGrade: StdGrade, result: PlanAuditResult) {
    super()
    this.coursePlan = coursePlan
    this.stdGrade = stdGrade
    this.result = result
  }

  def setAuditTerms(auditTerms: Array[String]) {
    this.auditTerms = auditTerms
    this.partial = if (auditTerms == null || auditTerms.length == 0) false else true
  }
}
