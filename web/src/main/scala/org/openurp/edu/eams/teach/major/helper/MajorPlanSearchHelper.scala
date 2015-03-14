package org.openurp.edu.eams.teach.major.helper

import java.util.Date
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.base.Program
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.web.helper.SearchHelper

import scala.collection.JavaConversions._

class MajorPlanSearchHelper extends SearchHelper {

  def buildProgramQuery(): OqlBuilder[Program] = {
    val query = OqlBuilder.from(classOf[Program], "program").where("program.majorProgram = true")
    QueryHelper.populateConditions(query)
    if (Strings.isEmpty(Params.get(Order.ORDER_STR))) {
      query.orderBy(new Order("program.grade desc"))
    } else {
      query.orderBy(Params.get(Order.ORDER_STR))
    }
    query.limit(QueryHelper.getPageLimit)
    val changeApplyState = Params.get("changeApplyState")
    if ("HAVE_UNPROCESSED" == changeApplyState) {
      val auditStateCond = new StringBuilder()
      auditStateCond.append("(").append("exists(\n").append("   select apply.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean apply \n")
        .append("   where apply.majorPlan.id=(select plan.id from org.openurp.edu.teach.plan.MajorPlan plan where plan.program=program) and apply.flag= :flag\n")
        .append(") or \n")
        .append("exists(\n")
        .append("   select apply.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean apply \n")
        .append("   where apply.majorPlan.id=(select plan.id from org.openurp.edu.teach.plan.MajorPlan plan where plan.program=program) and apply.flag= :flag")
        .append(")")
        .append(")")
      query.where(auditStateCond.toString, -1)
    }
    val auditState = Params.get("fake.auditState")
    if (Strings.isNotBlank(auditState)) {
      query.where("program.auditState = :auditState", CommonAuditState.valueOf(auditState.toUpperCase()))
    }
    val now = new Date()
    val valid = Params.getBoolean("fake.valid")
    if (valid != null && valid) {
      query.where("(program.invalidOn is null or :now1 < program.invalidOn)", now)
    } else if (valid != null && !valid) {
      query.where(":now1 >= program.invalidOn", now)
    }
    query
  }

  def buildPlanQuery(): OqlBuilder[MajorPlan] = {
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    QueryHelper.populateConditions(query)
    if (Strings.isEmpty(Params.get(Order.ORDER_STR))) {
      query.orderBy(new Order("plan.program.grade desc"))
    } else {
      query.orderBy(Params.get(Order.ORDER_STR))
    }
    query.limit(QueryHelper.getPageLimit)
    val changeApplyState = Params.get("changeApplyState")
    if ("HAVE_UNPROCESSED" == changeApplyState) {
      val auditStateCond = new StringBuilder()
      auditStateCond.append("(").append("exists(\n").append("	select apply.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean apply \n")
        .append("	where apply.majorPlan.id=plan.id and apply.flag= :flag\n")
        .append(") or \n")
        .append("exists(\n")
        .append("	select apply.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseGroupModifyBean apply \n")
        .append("	where apply.majorPlan.id=plan.id and apply.flag= :flag")
        .append(")")
        .append(")")
      query.where(auditStateCond.toString, -1)
    }
    val auditState = Params.get("fake.auditState")
    if (Strings.isNotBlank(auditState)) {
      query.where("plan.program.auditState = :auditState", CommonAuditState.valueOf(auditState.toUpperCase()))
    }
    val now = new Date()
    val valid = Params.getBoolean("fake.valid")
    if (valid != null && valid) {
      query.where("(plan.program.invalidOn is null or :now1 < plan.program.invalidOn)", now)
    } else if (valid != null && !valid) {
      query.where(":now1 >= plan.program.invalidOn", now)
    }
    query.join("left outer", "plan.program.direction", "direction")
    query
  }

  def addSemesterActiveCondition(query: OqlBuilder[MajorPlan], semester: Semester) {
    val semesterBeg = semester.beginOn
    val semesterEnd = semester.getEndOn
    query.where("(" + 
      "(plan.program.invalidOn is null     and :semesterEnd1 >= plan.program.effectiveOn) or " + 
      "(plan.program.invalidOn is not null and :semesterEnd2 >= plan.program.effectiveOn and :semesterBeg <= plan.program.invalidOn)" + 
      ")", semesterEnd, semesterEnd, semesterBeg)
  }
}
