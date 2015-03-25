package org.openurp.edu.eams.teach.program.personal.web.action

import java.util.Date


import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.struts2.helper.QueryHelper
import com.ekingstar.eams.core.Student
import com.ekingstar.eams.core.code.industry.StdStatus
import com.ekingstar.eams.teach.code.school.CourseHourType
import org.openurp.edu.eams.teach.program.MajorCourseSubstitution
import org.openurp.edu.eams.teach.program.StdCourseSubstitution
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.service.PersonalPlanCompareService
import org.openurp.edu.eams.teach.program.personal.service.PersonalPlanService
import org.openurp.edu.eams.teach.program.service.AmbiguousMajorProgramException
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.teach.program.service.NoMajorProgramException
import com.ekingstar.eams.web.action.common.ProjectSupportAction
//remove if not needed


class PersonalPlanSearchAction extends ProjectSupportAction {

  var planCommonDao: PlanCommonDao = _

  var personalPlanCompareService: PersonalPlanCompareService = _

  var personalPlanService: PersonalPlanService = _

  var coursePlanProvider: CoursePlanProvider = _

  def index(): String = {
    put("journalStatuses", baseCodeService.getCodes(classOf[StdStatus]))
    put("stdTypes", getStdTypes)
    put("departs", getDeparts)
    forward()
  }

  def info(): String = {
    val stdId = getLong("stdId")
    if (null == stdId) {
      return forwardError("error.model.id.needed")
    }
    val std = entityDao.get(classOf[Student], stdId)
    val plan = coursePlanProvider.getPersonalPlan(std)
    put("plan", plan)
    val builder = OqlBuilder.from(classOf[StdCourseSubstitution], "stdCourseSubstitution")
    builder.where("stdCourseSubstitution.std = :std", std)
    put("stdCourseSubstitutions", entityDao.search(builder))
    val oqlBuilder = OqlBuilder.from(classOf[MajorCourseSubstitution], "majorCourseSubstitution")
    oqlBuilder.where("majorCourseSubstitution.grades =:grade", plan.getStd.getGrade)
      .where("majorCourseSubstitution.department =:deparment", plan.getStd.getDepartment)
      .where("majorCourseSubstitution.major =:major", plan.getStd.getMajor)
      .where("majorCourseSubstitution.direction =:direction", plan.getStd.getDirection)
      .where("majorCourseSubstitution.education =:education", plan.getStd.getEducation)
    put("majorCourseSubstitutions", entityDao.search(oqlBuilder))
    val toXLS = getBoolean("toXLS")
    if (true == toXLS) {
      return null
    }
    put("weekHour", get("weekHour"))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("isPersonlPlan", "1")
    forward()
  }

  def print(): String = {
    val planId = getLongId("plan")
    if (null == planId) {
      return forwardError("error.model.id.needed")
    }
    val plan = entityDao.get(classOf[PersonalPlan], planId)
    put("plan", plan)
    forward()
  }

  def search(): String = {
    put("students", entityDao.search(buidOql()))
    forward()
  }

  private def buidOql(): OqlBuilder[Student] = {
    val query = OqlBuilder.from(classOf[Student], "std")
    query.where("exists (from org.openurp.edu.eams.teach.program.personal.PersonalPlan perPlan where perPlan.std = std)")
    populateConditions(query)
    val active = getBoolean("active")
    val inSchool = getBoolean("inSchool")
    val stdStatusId = getLong("stdStatusId")
    val date = new Date()
    if (null != active) {
      if (active) {
        query.where("std.registOn<= :now and std.graduateOn>=:now and std.registed=1", date)
      } else {
        query.where("std.registOn> :now or std.graduateOn<:now or std.registed=0", date)
      }
    }
    if (null != inSchool) {
      if (inSchool) {
        query.where("exists (from com.ekingstar.eams.core.StudentJournal journal where journal.std=std and journal.beginOn<=:now and journal.endOn >:now and journal.inschool =:inschool)", 
          new java.sql.Date(System.currentTimeMillis()), true)
      } else {
        query.where("not exists (from com.ekingstar.eams.core.StudentJournal journal where journal.std=std and journal.beginOn<=:now and journal.endOn >:now and journal.inschool =:inschool)", 
          new java.sql.Date(System.currentTimeMillis()), true)
      }
    }
    if (stdStatusId != null) {
      query.where("exists (from com.ekingstar.eams.core.StudentJournal journal where journal.std=std and journal.status.id = :stdStatusId)", 
        stdStatusId)
    }
    query.where("std.project = :project", getProject)
    query.where("std.department in (:departments)", getDeparts)
    if (CollectUtils.isNotEmpty(getStdTypes)) {
      query.where("std.type in (:stdTypes)", getStdTypes)
    }
    if (CollectUtils.isNotEmpty(getEducations)) {
      query.where("std.education in (:educations)", getEducations)
    }
    query.limit(QueryHelper.getPageLimit)
    query
  }

  def compareWithMajorPlan(): String = {
    val stdIds = getLongIds("std")
    if (ArrayUtils.isEmpty(stdIds)) {
      return forwardError("error.parameters.needed")
    }
    val multiComparisonResult = new HashMap[String, Map[String, Any]]()
    for (stdId <- stdIds) {
      val std = entityDao.get(classOf[Student], stdId)
      val personalPlan = coursePlanProvider.getPersonalPlan(std)
      val oneComparisonResult = new HashMap[String, Any]()
      multiComparisonResult.put(personalPlan.id.toString, oneComparisonResult)
      oneComparisonResult.put("personalPlan", personalPlan)
      try {
        val majorTeachPlan = personalPlanService.getMajorPlanForDiff(std)
        oneComparisonResult.put("majorPlan", majorTeachPlan)
        oneComparisonResult.put("diffResult", personalPlanCompareService.diffPersonalAndMajorPlan(majorTeachPlan, 
          personalPlan))
        oneComparisonResult.put("stdCourseTypes", planCommonDao.getUsedCourseTypes(personalPlan))
        oneComparisonResult.put("majorCourseTypes", planCommonDao.getUsedCourseTypes(majorTeachPlan))
      } catch {
        case e: NoMajorProgramException => oneComparisonResult.put("errors", "无法找到和该生匹配的专业培养计划！")
        case e: AmbiguousMajorProgramException => oneComparisonResult.put("errors", "找到多个和该生个人培养计划匹配的专业培养计划！")
      }
    }
    put("multiComparisonResult", multiComparisonResult)
    forward()
  }
}
