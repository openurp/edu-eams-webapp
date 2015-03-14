package org.openurp.edu.eams.teach.program.common.copydao.plancourse

import java.util.List
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.PlanCourse
//remove if not needed
import scala.collection.JavaConversions._

trait IPlanCourseCopyDao {

  def copyPlanCourses(sourcePlanCourses: List[_ <: PlanCourse], courseGroupAttachTo: CourseGroup): List[_ <: PlanCourse]

  def copyPlanCourse(sourcePlanCourse: PlanCourse, courseGroupAttachTo: CourseGroup): PlanCourse
}
