package org.openurp.edu.eams.teach.program.service.internal



import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.base.Program
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.StdPlan
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider



class CoursePlanProviderImpl extends BaseServiceImpl with CoursePlanProvider {

  def getMajorPlan(student: Student): MajorPlan = {
    getMajorPlan(student.program)
  }

  def getMajorPlan(program: Program): MajorPlan = {
    if (null == program) return null
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.where("plan.program.id = :programId", program.id)
      .cacheable()
    entityDao.uniqueResult(query)
  }

  def getPersonalPlan(std: Student): StdPlan = {
    val query = OqlBuilder.from(classOf[StdPlan], "plan")
    query.where("plan.std = :std", std)
    entityDao.uniqueResult(query)
  }

  def getCoursePlans(students: Iterable[Student]): Map[Student, CoursePlan] = {
    val result = Collections.newMap[Student, CoursePlan] 
    for (student <- students) result.put(student, getCoursePlan(student))
    result.toMap
  }

  def getCoursePlan(student: Student): CoursePlan = {
    var plan:CoursePlan = getPersonalPlan(student)
    if (null == plan) plan = getMajorPlan(student)
    plan
  }
}
