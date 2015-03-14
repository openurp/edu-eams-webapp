package org.openurp.edu.eams.teach.program.personal.service.impl

import java.util.List
import org.apache.commons.beanutils.PropertyUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import com.ekingstar.eams.core.Student
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.StudentProgram
import org.openurp.edu.eams.teach.program.common.copydao.plan.IPlanCopyDao
import org.openurp.edu.eams.teach.program.helper.ProgramNamingHelper
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.service.PersonalPlanService
import org.openurp.edu.eams.teach.program.service.AmbiguousMajorProgramException
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.teach.program.service.NoMajorProgramException
import org.openurp.edu.eams.teach.program.service.StudentProgramBindService
//remove if not needed
import scala.collection.JavaConversions._

class PersonalPlanServiceImpl extends BaseServiceImpl with PersonalPlanService {

  private var personalPlanCopyDao: IPlanCopyDao = _

  private var coursePlanProvider: CoursePlanProvider = _

  private var studentProgramBindService: StudentProgramBindService = _

  def getMajorPlanForDiff(std: Student): MajorPlan = {
    val majorPlan = coursePlanProvider.getMajorPlan(std)
    if (majorPlan != null) return majorPlan
    coursePlanProvider.getMajorPlan(studentProgramBindService.matchMajorProgram(std, true, true))
  }

  def setPersonalPlanCopyDao(personalPlanCopyDao: IPlanCopyDao) {
    this.personalPlanCopyDao = personalPlanCopyDao
  }

  def genPersonalPlan(std: Student, majorProgram: Program): PersonalPlan = {
    val genParameter = new MajorPlanGenParameter()
    genParameter.setStudent(std)
    var majorPlan: MajorPlan = null
    if (majorProgram != null) {
      PropertyUtils.copyProperties(genParameter, majorProgram)
      majorPlan = coursePlanProvider.getMajorPlan(majorProgram)
    } else {
      genParameter.setGrade(std.getGrade)
      genParameter.setDepartment(std.getDepartment)
      genParameter.setDirection(std.getDirection)
      genParameter.setDuration(std.getDuration)
      genParameter.setEducation(std.getEducation)
      genParameter.setMajor(std.getMajor)
      genParameter.setStdType(std.getType)
      genParameter.setStudyType(std.getStudyType)
      genParameter.setEffectiveOn(std.getEnrollOn)
      genParameter.setInvalidOn(std.getGraduateOn)
    }
    genParameter.setName(ProgramNamingHelper.name(std))
    val result = personalPlanCopyDao.copyMajorPlan(majorPlan, genParameter).asInstanceOf[PersonalPlan]
    result
  }

  def genPersonalPlan(std: Student): PersonalPlan = {
    val query = OqlBuilder.from(classOf[StudentProgram], "sp")
    query.where("sp.std=:std", std)
    val sp = entityDao.uniqueResult(query)
    var majorProgram: Program = null
    if (sp == null) {
      try {
        majorProgram = studentProgramBindService.matchMajorProgram(std, true, true)
      } catch {
        case e: AmbiguousMajorProgramException => throw e
        case e: NoMajorProgramException => 
      }
    }
    genPersonalPlan(std, majorProgram)
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }

  def setStudentProgramBindService(studentProgramBindService: StudentProgramBindService) {
    this.studentProgramBindService = studentProgramBindService
  }
}
