package org.openurp.edu.eams.teach.program.major.dao

import java.util.List
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.teach.plan.MajorPlan

import scala.collection.JavaConversions._

trait MajorPlanDao {

  def getMajorPlanList(grade: String, major: Major, level: Education): List[MajorPlan]

  def getMajorPlans(planIds: Array[Long]): List[MajorPlan]
}
