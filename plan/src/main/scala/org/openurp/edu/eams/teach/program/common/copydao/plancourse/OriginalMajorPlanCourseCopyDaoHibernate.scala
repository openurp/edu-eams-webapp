package org.openurp.edu.eams.teach.program.common.copydao.plancourse

import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.original.model.OriginalPlanCourseBean
//remove if not needed


class OriginalMajorPlanCourseCopyDaoHibernate extends AbstractPlanCourseCopyDao {

  protected override def newPlanCourse(): MajorPlanCourse = new OriginalPlanCourseBean()
}
