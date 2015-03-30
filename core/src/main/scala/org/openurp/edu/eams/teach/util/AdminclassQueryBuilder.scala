package org.openurp.edu.eams.teach.util

import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.plan.MajorPlan

object AdminclassQueryBuilder {

  def build(plan: MajorPlan): OqlBuilder[Adminclass] = {
    val adminClassQuery = OqlBuilder.from(classOf[Adminclass], "adminClass")
    adminClassQuery.where("adminClass.grade = :grade", plan.program.grade)
    adminClassQuery.where("adminClass.stdType = :stdType", plan.program.stdType)
    adminClassQuery.where("adminClass.department = :department", plan.program.department)
    adminClassQuery.where("adminClass.major = :major", plan.program.major)
    if (plan.program.direction != null && plan.program.direction.persisted) {
      adminClassQuery.where("adminClass.direction=:direction", plan.program.direction)
    } else {
      adminClassQuery.where("adminClass.direction is null")
    }
    adminClassQuery
  }
}
