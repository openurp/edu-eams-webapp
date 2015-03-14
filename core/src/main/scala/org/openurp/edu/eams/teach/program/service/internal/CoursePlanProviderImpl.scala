package org.openurp.edu.eams.teach.program.service.internal

import java.util.Collection
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.teach.plan.CoursePlan
import org.openurp.edu.base.Program
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.teach.plan.StdPlan
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider

import scala.collection.JavaConversions._

class CoursePlanProviderImpl extends BaseServiceImpl with CoursePlanProvider {

  def getMajorPlan(student: Student): MajorPlan = {
    getMajorPlan(student.program)
  }

  def getMajorPlan(program: Program): MajorPlan = {
    if (null == program) return null
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.where("plan.program.id = :programId", program.getId)
      .cacheable()
    entityDao.uniqueResult(query)
  }

  def getPersonalPlan(std: Student): PersonalPlan = {
    val query = OqlBuilder.from(classOf[PersonalPlan], "plan")
    query.where("plan.std = :std", std)
    entityDao.uniqueResult(query)
  }

  def getCoursePlans(students: Collection[Student]): Map[Student, CoursePlan] = {
    val result = CollectUtils.newHashMap()
    for (student <- students) result.put(student, getCoursePlan(student))
    result
  }

  def getCoursePlan(studentProgram: StudentProgram): CoursePlan = {
    var plan = getPersonalPlan(studentProgram.getStd)
    if (null == plan) plan = getMajorPlan(studentProgram.getProgram)
    plan
  }

  def getCoursePlan(student: Student): CoursePlan = {
    var plan = getPersonalPlan(student)
    if (null == plan) plan = getMajorPlan(student)
    plan
  }
}
