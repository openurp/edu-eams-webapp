package org.openurp.edu.eams.teach.program.common.copydao.coursegroup

import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
//remove if not needed
import scala.collection.JavaConversions._

trait IPlanCourseGroupCopyDao {

  def copyCourseGroup(sourceCourseGroup: CourseGroup, parentAttachTo: CourseGroup, planAttachTo: CoursePlan): CourseGroup
}
