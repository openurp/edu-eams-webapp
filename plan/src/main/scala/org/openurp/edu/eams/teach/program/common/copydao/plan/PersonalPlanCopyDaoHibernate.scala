package org.openurp.edu.eams.teach.program.common.copydao.plan

import java.util.ArrayList
import java.util.Date
import org.beangle.orm.hibernate.HibernateEntityDao
import com.ekingstar.eams.core.CommonAuditState
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.IPlanCourseGroupCopyDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import org.openurp.edu.eams.teach.program.model.ProgramBean
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanBean
//remove if not needed
import scala.collection.JavaConversions._

class PersonalPlanCopyDaoHibernate extends HibernateEntityDao with IPlanCopyDao {

  protected var courseGroupCopyDao: IPlanCourseGroupCopyDao = _

  protected var planCommonDao: PlanCommonDao = _

  def copyMajorPlan(sourcePlan: MajorPlan, genParameter: MajorPlanGenParameter): CoursePlan = {
    val newPlan = newPlan(sourcePlan)
    newPlan.setEffectiveOn(new java.sql.Date(System.currentTimeMillis()))
    newPlan.setGroups(new ArrayList[CourseGroup]())
    newPlan.setEffectiveOn(new java.sql.Date(System.currentTimeMillis()))
    newPlan.setStd(genParameter.getStudent)
    saveOrUpdate(newPlan)
    if (sourcePlan != null) {
      for (sourceCourseGroup <- sourcePlan.getTopCourseGroups) {
        courseGroupCopyDao.copyCourseGroup(sourceCourseGroup.asInstanceOf[MajorPlanCourseGroup], null, 
          newPlan)
      }
    }
    planCommonDao.saveOrUpdatePlan(newPlan)
    newPlan
  }

  def setCourseGroupCopyDao(courseGroupCopyDao: IPlanCourseGroupCopyDao) {
    this.courseGroupCopyDao = courseGroupCopyDao
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  protected def newProgram(originalProgram: Program, genParameter: MajorPlanGenParameter): Program = {
    var newProgram: Program = null
    newProgram = if (originalProgram == null) new ProgramBean() else originalProgram.asInstanceOf[ProgramBean].clone().asInstanceOf[Program]
    val now = new Date()
    newProgram.setId(null)
    newProgram.setName(genParameter.getName)
    newProgram.setAuditState(CommonAuditState.UNSUBMITTED)
    newProgram.setCreatedAt(now)
    newProgram.setUpdatedAt(now)
    newProgram.setDuration(genParameter.getDuration)
    newProgram.setGrade(genParameter.getGrade)
    newProgram.setEffectiveOn(genParameter.getEffectiveOn)
    newProgram.setInvalidOn(genParameter.getInvalidOn)
    if (genParameter.getDegree == null || genParameter.getDegree.isTransient) {
      newProgram.setDegree(null)
    } else {
      newProgram.setDegree(genParameter.getDegree)
    }
    if (genParameter.getDepartment == null || genParameter.getDepartment.isTransient) {
      newProgram.setDepartment(null)
    } else {
      newProgram.setDepartment(genParameter.getDepartment)
    }
    if (genParameter.getDirection == null || genParameter.getDirection.isTransient) {
      newProgram.setDirection(null)
    } else {
      newProgram.setDirection(genParameter.getDirection)
    }
    if (genParameter.getEducation == null || genParameter.getEducation.isTransient) {
      newProgram.setEducation(null)
    } else {
      newProgram.setEducation(genParameter.getEducation)
    }
    if (genParameter.getMajor == null || genParameter.getMajor.isTransient) {
      newProgram.setMajor(null)
    } else {
      newProgram.setMajor(genParameter.getMajor)
    }
    if (genParameter.getStdType == null || genParameter.getStdType.isTransient) {
      genParameter.setStdType(null)
    } else {
      newProgram.setStdType(genParameter.getStdType)
    }
    if (genParameter.getStudyType == null || genParameter.getStudyType.isTransient) {
      newProgram.setStudyType(null)
    } else {
      newProgram.setStudyType(genParameter.getStudyType)
    }
    newProgram
  }

  protected def newPlan(plan: MajorPlan): PersonalPlan = {
    val personalPlan = new PersonalPlanBean()
    if (plan != null) {
      personalPlan.setCredits(plan.getCredits)
      personalPlan.setTermsCount(plan.getTermsCount)
    }
    personalPlan
  }
}
