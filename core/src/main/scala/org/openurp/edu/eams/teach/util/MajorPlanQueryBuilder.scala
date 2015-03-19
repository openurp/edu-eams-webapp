package org.openurp.edu.eams.teach.util

import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.plan.MajorPlan



object MajorPlanQueryBuilder {

  def build(adminclass: Adminclass): OqlBuilder[MajorPlan] = {
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.where("plan.program.grade = :grade", adminclass.grade)
      .where("plan.program.stdType = :stdType", adminclass.stdType)
      .where("plan.program.major = :major", adminclass.major)
    if (null == adminclass.direction) {
      query.where("plan.program.direction is null")
    } else {
      query.where("plan.program.direction =:direction", adminclass.direction)
    }
    query
  }

  def build(grade: String, 
      stdType: StdType, 
      major: Major, 
      direction: Direction): OqlBuilder[MajorPlan] = {
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.where("plan.program.grade = :grade", grade).where("plan.program.major = :major", major)
    if (null != stdType) {
      query.where("plan.program.stdType = :stdType", stdType)
    }
    if (null == direction) {
    } else {
      query.where("plan.program.direction =:direction", direction)
    }
    query
  }
}
