package org.openurp.edu.eams.teach.program.common.copydao.coursegroup

import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.original.model.OriginalPlanCourseGroupBean
//remove if not needed
import scala.collection.JavaConversions._

class OriginalMajorPlanCourseGroupCopyDaoHibernate extends AbstractPlanCourseGroupCopyDao {

  override def newCourseGroup(sourceCourseGroup: CourseGroup): CourseGroup = new OriginalPlanCourseGroupBean()
}
