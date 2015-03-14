package org.openurp.edu.eams.teach.program.major.dao.hibernate

import java.util.List
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.orm.hibernate.HibernateEntityDao
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.dao.MajorPlanCourseGroupDao
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupDaoHibernate extends HibernateEntityDao with MajorPlanCourseGroupDao {

  def getCourseType(planId: java.lang.Long, courseId: java.lang.Long): CourseType = {
    val query = OqlBuilder.from(classOf[MajorPlanCourseGroup], "courseGroup")
    query.select("select courseGroup.courseType").join("courseGroup.planCourses", "planCourse")
      .join("courseGroup.coursePlan", "plan")
      .where("plan.id=:planId", planId)
      .where("planCourse.course.id=:courseId", courseId)
    val rs = search(query)
    (if (rs.isEmpty) null else rs.get(0)).asInstanceOf[CourseType]
  }

  private def swap(anyList: List[_], index1: Int, index2: Int) {
    val o1 = anyList.get(index1)
    val o2 = anyList.get(index2)
    anyList.set(index2, o1)
    anyList.set(index1, o2)
  }
}
