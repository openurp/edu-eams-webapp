package org.openurp.edu.eams.teach.program.major.dao.hibernate


import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.eams.teach.program.major.dao.MajorPlanCourseDao
//remove if not needed


class MajorPlanCourseDaoHibernate extends HibernateEntityDao with MajorPlanCourseDao {

  def getPlanCourseByTerm(planId: java.lang.Long, term: java.lang.Integer): List[MajorPlanCourse] = {
    val query = OqlBuilder.from(classOf[MajorPlanCourse], "planCourse")
    query.join("planCourse.courseGroup", "courseGroup")
    query.where(new Condition("(:term is null or instr(planCourse.terms, :term) > 0)", term.toString))
    query.where(new Condition("courseGroup.coursePlan.id=:planId", planId))
    search(query)
  }
}
