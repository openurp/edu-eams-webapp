package org.openurp.edu.eams.teach.program.major.dao


import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.teach.plan.MajorPlan



trait MajorPlanDao {

  def getMajorPlanList(grade: String, major: Major, level: Education): List[MajorPlan]

  def getMajorPlans(planIds: Array[Long]): List[MajorPlan]
}
