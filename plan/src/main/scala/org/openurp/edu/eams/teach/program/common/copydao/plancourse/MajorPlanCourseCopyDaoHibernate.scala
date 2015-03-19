package org.openurp.edu.eams.teach.program.common.copydao.plancourse

import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseBean
//remove if not needed


class MajorPlanCourseCopyDaoHibernate extends AbstractPlanCourseCopyDao {

  protected override def newPlanCourse(): MajorPlanCourse = new MajorPlanCourseBean()
}
