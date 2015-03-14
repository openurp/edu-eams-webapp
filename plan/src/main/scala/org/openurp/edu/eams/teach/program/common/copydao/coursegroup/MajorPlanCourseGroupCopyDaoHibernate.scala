package org.openurp.edu.eams.teach.program.common.copydao.coursegroup

import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupCopyDaoHibernate extends AbstractPlanCourseGroupCopyDao {

  override def newCourseGroup(sourceCourseGroup: CourseGroup): CourseGroup = {
    sourceCourseGroup.clone().asInstanceOf[MajorPlanCourseGroup]
  }
}
