package org.openurp.edu.eams.teach.program.common.copydao.coursegroup

import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
//remove if not needed


trait IPlanCourseGroupCopyDao {

  def copyCourseGroup(sourceCourseGroup: CourseGroup, parentAttachTo: CourseGroup, planAttachTo: CoursePlan): CourseGroup
}
