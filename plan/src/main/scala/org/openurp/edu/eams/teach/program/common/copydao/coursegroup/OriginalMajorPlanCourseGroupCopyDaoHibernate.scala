package org.openurp.edu.eams.teach.program.common.copydao.coursegroup

import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.original.model.OriginalPlanCourseGroupBean
//remove if not needed


class OriginalMajorCourseGroupCopyDaoHibernate extends AbstractPlanCourseGroupCopyDao {

  override def newCourseGroup(sourceCourseGroup: CourseGroup): CourseGroup = new OriginalPlanCourseGroupBean()
}
