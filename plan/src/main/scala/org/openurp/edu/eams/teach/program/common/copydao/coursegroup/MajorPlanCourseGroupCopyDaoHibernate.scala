package org.openurp.edu.eams.teach.program.common.copydao.coursegroup

import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.MajorCourseGroup
//remove if not needed


class MajorCourseGroupCopyDaoHibernate extends AbstractPlanCourseGroupCopyDao {

  override def newCourseGroup(sourceCourseGroup: CourseGroup): CourseGroup = {
    sourceCourseGroup.clone().asInstanceOf[MajorCourseGroup]
  }
}
