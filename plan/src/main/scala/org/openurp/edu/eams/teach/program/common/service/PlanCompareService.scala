package org.openurp.edu.eams.teach.program.common.service

import java.util.List
import java.util.Map
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
//remove if not needed
import scala.collection.JavaConversions._

trait PlanCompareService {

  def diff(leftPlan: CoursePlan, rightPlan: CoursePlan): Map[CourseType, Array[List[_ <: PlanCourse]]]
}
