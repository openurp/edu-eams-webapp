package org.openurp.edu.eams.teach.util

import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.plan.MajorPlan



object AdminclassQueryBuilder {

  def build(plan: MajorPlan): OqlBuilder[Adminclass] = {
    val adminClassQuery = OqlBuilder.from(classOf[Adminclass], "adminClass")
    adminClassQuery.where("adminClass.grade = :grade", plan.getProgram.grade)
    adminClassQuery.where("adminClass.stdType = :stdType", plan.getProgram.stdType)
    adminClassQuery.where("adminClass.department = :department", plan.getProgram.department)
    adminClassQuery.where("adminClass.major = :major", plan.getProgram.major)
    if (plan.getProgram.direction != null && plan.getProgram.direction.isPersisted) {
      adminClassQuery.where("adminClass.direction=:direction", plan.getProgram.direction)
    } else {
      adminClassQuery.where("adminClass.direction is null")
    }
    adminClassQuery
  }
}
