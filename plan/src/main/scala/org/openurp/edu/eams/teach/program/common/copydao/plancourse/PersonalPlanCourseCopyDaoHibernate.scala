package org.openurp.edu.eams.teach.program.common.copydao.plancourse

import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanCourseBean
//remove if not needed


class PersonalPlanCourseCopyDaoHibernate extends AbstractPlanCourseCopyDao {

  protected override def newPlanCourse(): MajorPlanCourse = new PersonalPlanCourseBean()
}
