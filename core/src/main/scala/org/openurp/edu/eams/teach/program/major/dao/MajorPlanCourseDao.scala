package org.openurp.edu.eams.teach.program.major.dao

import java.util.List
import org.openurp.edu.teach.plan.MajorPlanCourse

import scala.collection.JavaConversions._

trait MajorPlanCourseDao {

  def getPlanCourseByTerm(planId: java.lang.Long, term: java.lang.Integer): List[MajorPlanCourse]
}
