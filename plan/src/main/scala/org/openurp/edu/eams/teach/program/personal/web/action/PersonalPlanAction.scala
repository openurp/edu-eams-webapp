package org.openurp.edu.eams.teach.program.personal.web.action





import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.core.Student
import com.ekingstar.eams.exception.EamsException
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.model.ProgramBean
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.exception.PersonalPlanSyncException
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanBean
import org.openurp.edu.eams.teach.program.personal.service.PersonalPlanService
import org.openurp.edu.eams.teach.program.service.AmbiguousMajorProgramException
import org.openurp.edu.eams.teach.program.service.NoMajorProgramException
//remove if not needed


class PersonalPlanAction extends PersonalPlanSearchAction {

  var personalPlanService: PersonalPlanService = _

  def remove(): String = {
    val stdIds = getLongIds("std")
    val query = OqlBuilder.from(classOf[PersonalPlan], "personalPlan")
    query.where("personalPlan.std.id in (:stdIds)", stdIds)
    entityDao.remove(entityDao.search(query))
    redirect(new Action(classOf[PersonalPlanAction], "search", get("params")), "info.delete.success")
  }

  def syncMajorPlan(): String = {
    val personalPlanIds = get("personalPlanIds").split(",")
    for (prefix <- personalPlanIds) {
      val majorPlanId = getLong(prefix + "majorPlanId")
      val personalPlanId = getLong(prefix + "personalPlanId")
      val majorPlan = entityDao.get(classOf[MajorPlan], majorPlanId)
      val personalPlan = entityDao.get(classOf[PersonalPlan], personalPlanId)
      if (majorPlan == null || personalPlan == null) {
        return forwardError("error.model.notExist")
      }
      val copyPlanCourses = get(prefix + "copyPlanCourses")
      val copyCourseTypes = get(prefix + "copyCourseTypes")
      val delPlanCourses = get(prefix + "delPlanCourses")
      val delCourseTypes = get(prefix + "delCourseTypes")
      val copyCourseTypePlanCourseIds = makeCourseTypePlanCourseIdsList(copyPlanCourses)
      val delCourseTypePlanCourseIds = makeCourseTypePlanCourseIdsList(delPlanCourses)
      val copyCourseTypeIds = makeCourseTypeIdsList(copyCourseTypes)
      val delCourseTypeIds = makeCourseTypeIdsList(delCourseTypes)
      try {
        personalPlanCompareService.copyPlanCourses(majorPlan, personalPlan, copyCourseTypePlanCourseIds)
        personalPlanCompareService.copyCourseGroups(majorPlan, personalPlan, copyCourseTypeIds)
        personalPlanCompareService.deletePlanCourses(personalPlan, delCourseTypePlanCourseIds)
        personalPlanCompareService.deleteCourseGroups(personalPlan, delCourseTypeIds)
      } catch {
        case e: PersonalPlanSyncException => return forwardError(e.getMessage)
      }
    }
    redirect(new Action(classOf[PersonalPlanAction], "search"), "info.update.success")
  }

  private def makeCourseTypePlanCourseIdsList(input: String): List[Array[Number]] = {
    val tmp = input.split(";")
    val courseTypePlanCourseIds = new ArrayList[Array[Number]]()
    for (i <- 0 until tmp.length) {
      val typeIdCourseId = tmp(i)
      if (!Strings.isEmpty(typeIdCourseId)) {
        val courseTypeId = java.lang.Integer.valueOf(typeIdCourseId.split(",")(0))
        val planCourseId = java.lang.Long.valueOf(typeIdCourseId.split(",")(1))
        courseTypePlanCourseIds.add(Array(courseTypeId, planCourseId))
      }
    }
    courseTypePlanCourseIds
  }

  private def makeCourseTypeIdsList(input: String): List[Integer] = {
    val tmp = input.split(";")
    val courseTypeIds = new ArrayList[Integer]()
    for (i <- 0 until tmp.length) {
      val typeId = tmp(i)
      if (!Strings.isEmpty(typeId)) {
        courseTypeIds.add(java.lang.Integer.valueOf(typeId))
      }
    }
    courseTypeIds
  }

  def compareWithMajorPlan(): String = {
    val stdIds = getLongIds("std")
    if (ArrayUtils.isEmpty(stdIds)) {
      return forwardError("error.parameters.needed")
    }
    val multiComparisonResult = new HashMap[String, Map[String, Any]]()
    val stds = entityDao.get(classOf[Student], stdIds)
    val stdToAmbiguousMajorPrograms = Collections.newMap[Any]
    val noMmajorProgramStds = Collections.newBuffer[Any]
    for (std <- stds) {
      val personalPlan = coursePlanProvider.getPersonalPlan(std)
      val oneComparisonResult = new HashMap[String, Any]()
      multiComparisonResult.put(personalPlan.id.toString, oneComparisonResult)
      oneComparisonResult.put("personalPlan", personalPlan)
      try {
        val majorPlan = personalPlanService.getMajorPlanForDiff(std)
        oneComparisonResult.put("majorPlan", majorPlan)
        oneComparisonResult.put("diffResult", personalPlanCompareService.diffPersonalAndMajorPlan(majorPlan, 
          personalPlan))
        oneComparisonResult.put("stdCourseTypes", planCommonDao.getUsedCourseTypes(personalPlan))
        oneComparisonResult.put("majorCourseTypes", planCommonDao.getUsedCourseTypes(majorPlan))
      } catch {
        case e: NoMajorProgramException => noMmajorProgramStds.add(std)
        case e: AmbiguousMajorProgramException => stdToAmbiguousMajorPrograms.put(std, e.getAmbiguousPrograms)
      }
    }
    if (Collections.isNotEmpty(stdToAmbiguousMajorPrograms.keySet) || 
      Collections.isNotEmpty(noMmajorProgramStds)) {
      put("stdToAmbiguousMajorPrograms", stdToAmbiguousMajorPrograms)
      put("noMajorProgramStds", noMmajorProgramStds)
      return forward("compareProblem")
    }
    put("multiComparisonResult", multiComparisonResult)
    forward()
  }

  def compairWithSpecifiedMajorPlan(): String = {
    val stdIds = Collections.newBuffer[Any](getLongIds("std"))
    if (Collections.isEmpty(stdIds)) {
      return forwardError("error.parameters.needed")
    }
    var ambiguousProgramStdIds = Collections.newBuffer[Any]
    if (getLongIds("ambiguousProgramStd") != null) {
      ambiguousProgramStdIds = Collections.newBuffer[Any](getLongIds("ambiguousProgramStd"))
    }
    var noProgramStdIds = Collections.newBuffer[Any]
    if (getLongIds("noProgramStd") != null) {
      noProgramStdIds = Collections.newBuffer[Any](getLongIds("noProgramStd"))
    }
    stdIds.removeAll(ambiguousProgramStdIds)
    stdIds.removeAll(noProgramStdIds)
    val multiComparisonResult = new HashMap[String, Map[String, Any]]()
    val stds = entityDao.get(classOf[Student], stdIds)
    for (std <- stds) {
      val personalPlan = coursePlanProvider.getPersonalPlan(std)
      val oneComparisonResult = new HashMap[String, Any]()
      multiComparisonResult.put(personalPlan.id.toString, oneComparisonResult)
      oneComparisonResult.put("personalPlan", personalPlan)
      val majorPlan = coursePlanProvider.getMajorPlan(std)
      oneComparisonResult.put("majorPlan", majorPlan)
      oneComparisonResult.put("diffResult", personalPlanCompareService.diffPersonalAndMajorPlan(majorPlan, 
        personalPlan))
      oneComparisonResult.put("stdCourseTypes", planCommonDao.getUsedCourseTypes(personalPlan))
      oneComparisonResult.put("majorCourseTypes", planCommonDao.getUsedCourseTypes(majorPlan))
    }
    val ambiguousProgramStds = entityDao.get(classOf[Student], ambiguousProgramStdIds)
    for (std <- ambiguousProgramStds) {
      val personalPlan = coursePlanProvider.getPersonalPlan(std)
      val oneComparisonResult = new HashMap[String, Any]()
      multiComparisonResult.put(personalPlan.id.toString, oneComparisonResult)
      oneComparisonResult.put("personalPlan", personalPlan)
      val programId = getLong("student_program_" + std.id)
      val majorPlan = coursePlanProvider.getMajorPlan(new ProgramBean(programId))
      oneComparisonResult.put("majorPlan", majorPlan)
      oneComparisonResult.put("diffResult", personalPlanCompareService.diffPersonalAndMajorPlan(majorPlan, 
        personalPlan))
      oneComparisonResult.put("stdCourseTypes", planCommonDao.getUsedCourseTypes(personalPlan))
      oneComparisonResult.put("majorCourseTypes", planCommonDao.getUsedCourseTypes(majorPlan))
    }
    put("multiComparisonResult", multiComparisonResult)
    forward("compareWithMajorPlan")
  }

  def edit(): String = {
    val stdId = getLongId("std")
    val planId = getLongId("plan")
    var plan: PersonalPlan = null
    var std: Student = null
    if (stdId != null) {
      std = entityDao.get(classOf[Student], stdId)
      if (getProject.id != std.getProject.id || 
        (Collections.isEmpty(getEducations) || !getEducations.contains(std.getEducation)) || 
        !getStdTypes.contains(std.getType) || 
        !getDeparts.contains(std.getDepartment)) {
        return redirect("search", "error.dataRealm.insufficient")
      }
      plan = coursePlanProvider.getPersonalPlan(std)
    } else if (planId != null) {
      plan = entityDao.get(classOf[PersonalPlan], planId)
      if (getProject.id != plan.getStd.getMajor.getProject.id || 
        (Collections.isEmpty(getEducations) || !getEducations.contains(plan.getStd.getEducation)) || 
        !getStdTypes.contains(plan.getStd.getType) || 
        !getDeparts.contains(plan.getStd.getDepartment)) {
        return redirect("search", "error.dataRealm.insufficient")
      }
    }
    if (plan == null) {
      throw new EamsException("该生还没有个人培养计划")
    }
    put("std", std)
    put("plan", plan)
    put("stdTypeList", getStdTypes)
    put("departmentList", getDeparts)
    forward()
  }

  def gen(): String = {
    if (Strings.isEmpty(get("stdCode"))) {
      return forwardError("error.parameters.needed")
    }
    getFlash.put("params", get("params"))
    val query = OqlBuilder.from(classOf[Student], "std")
    query.where("std.code = :code", get("stdCode"))
    val stds = entityDao.search(query)
    if (Collections.isEmpty(stds)) {
      return forwardError("该生不存在")
    }
    val std = stds.get(0)
    if (getProject.id != std.getProject.id || 
      (Collections.isEmpty(getEducations) || !getEducations.contains(std.getEducation)) || 
      !getStdTypes.contains(std.getType) || 
      !getDeparts.contains(std.getDepartment)) {
      return redirect("search", "error.dataRealm.insufficient")
    }
    if (coursePlanProvider.getPersonalPlan(std) != null) {
      return redirect("edit", "info.personalPlan.isExists", "&stdId=" + std.id)
    }
    var plan: PersonalPlan = null
    try {
      plan = personalPlanService.genPersonalPlan(std)
    } catch {
      case e: AmbiguousMajorProgramException => {
        put("std", std)
        put("ambiguousPrograms", e.getAmbiguousPrograms)
        return forward("problemGen")
      }
    }
    redirect("edit", "info.success.genPersonalPlan", "&stdId=" + plan.getStd.id)
  }

  def assignedGen(): String = {
    val stdId = getLongId("std")
    val programId = getLong("student_program_" + stdId)
    val plan = personalPlanService.genPersonalPlan(entityDao.get(classOf[Student], stdId), entityDao.get(classOf[Program], 
      programId))
    redirect("edit", "info.success.genPersonalPlan", "&planId=" + plan.id)
  }

  def save(): String = {
    val std = entityDao.get(classOf[Student], getLongId("std"))
    var plan = coursePlanProvider.getPersonalPlan(std)
    val termsCount = getInt("plan.termsCount")
    if (plan == null) {
      plan = new PersonalPlanBean()
      plan.setStd(std)
    } else {
      if (get("oldTermsCount") != get("plan.termsCount")) {
        PlanTermCreditTool.updateTermsCount(plan, getInt("oldTermsCount"), getInt("plan.termsCount"), 
          entityDao)
      }
    }
    plan.setTermsCount(termsCount)
    plan.setEffectiveOn(getDate("plan.effectiveOn"))
    plan.setInvalidOn(getDate("plan.invalidOn"))
    plan.setRemark(get("plan.remark"))
    entityDao.saveOrUpdate(plan)
    redirect("search", "info.save.success")
  }
}
