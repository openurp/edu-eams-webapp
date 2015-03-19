package org.openurp.edu.eams.teach.program.major.dao


import org.openurp.edu.teach.plan.MajorPlanCourse



trait MajorPlanCourseDao {

  def getPlanCourseByTerm(planId: java.lang.Long, term: java.lang.Integer): List[MajorPlanCourse]
}
