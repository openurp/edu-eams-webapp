package org.openurp.edu.eams.teach.planaudit.service

import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.planaudit.PlanAuditResult

import scala.collection.JavaConversions._

trait PlanAuditService {

  def audit(student: Student, context: PlanAuditContext): PlanAuditResult

  def getResult(std: Student): PlanAuditResult

  def getSeriousResult(std: Student): PlanAuditResult
}
