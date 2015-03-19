package org.openurp.edu.eams.teach.program.common.copydao.coursegroup

import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanCourseGroupBean
//remove if not needed


class PersonalPlanCourseGroupCopyDaoHibernate extends AbstractPlanCourseGroupCopyDao {

  override def newCourseGroup(sourceCourseGroup: CourseGroup): CourseGroup = new PersonalPlanCourseGroupBean()
}
