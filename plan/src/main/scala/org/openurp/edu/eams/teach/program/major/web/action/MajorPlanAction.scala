package org.openurp.edu.eams.teach.program.major.web.action

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import javax.persistence.EntityNotFoundException
import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.core.Direction
import com.ekingstar.eams.core.Major
import com.ekingstar.eams.core.code.industry.Education
import com.ekingstar.eams.core.code.nation.Degree
import com.ekingstar.eams.core.code.nation.StudyType
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.code.school.CourseCategory
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.StudentProgram
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.common.helper.ProgramHibernateClassGetter
import org.openurp.edu.eams.teach.program.doc.model.ProgramDocBean
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
import org.openurp.edu.eams.teach.program.helper.ProgramCollector
import org.openurp.edu.eams.teach.program.helper.ProgramNamingHelper
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.flexible.MajorProgramTextTitleProvider
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateGuard
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateType
import org.openurp.edu.eams.teach.program.major.model.MajorPlanBean
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseBean
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorPlanAuditService
import org.openurp.edu.eams.teach.program.major.service.MajorPlanCourseGroupService
import org.openurp.edu.eams.teach.program.major.service.MajorPlanGenParameter
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import com.google.gson.Gson
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanAction extends MajorPlanSearchAction {

  private var majorPlanCourseGroupService: MajorPlanCourseGroupService = _

  private var planCommonDao: PlanCommonDao = _

  private var planCourseCommonDao: PlanCourseCommonDao = _

  private var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  private var majorPlanAuditService: MajorPlanAuditService = _

  private var coursePlanProvider: CoursePlanProvider = _

  private var guards: List[MajorProgramOperateGuard] = CollectUtils.newArrayList()

  private var textTitleProvider: MajorProgramTextTitleProvider = _

  def index(): String = {
    setDataRealm(hasStdTypeDepart)
    indexSetting()
    forward()
  }

  protected def indexSetting() {
    put("stateList", CommonAuditState.values)
    put("courseCategorys", baseCodeService.getCodes(classOf[CourseCategory]))
    put("educations", baseCodeService.getCodes(classOf[Education]))
    put("auditLimitDescription", "只能对状态为『未提交』和『未通过』的计划提交审核")
    put("SUBMITTED", CommonAuditState.SUBMITTED)
    put("ACCEPTED", CommonAuditState.ACCEPTED)
  }

  def save(): String = {
    val program = populateEntity(classOf[Program], "plan.program")
    program.setMajor(entityDao.get(classOf[Major], program.getMajor.getId))
    if (program.getDirection != null) {
      program.setDirection(entityDao.get(classOf[Direction], program.getDirection.getId))
    }
    program.setCreatedAt(new Date())
    program.setUpdatedAt(new Date())
    if (getBool("fake.autoname")) {
      program.setName(ProgramNamingHelper.name(program))
    }
    entityDao.saveOrUpdate(program)
    var plan = coursePlanProvider.getMajorPlan(program)
    val termsCount = getInt("plan.termsCount")
    if (plan != null) {
      plan.setTermsCount(termsCount)
      if (get("oldTermsCount") != get("plan.termsCount")) {
        PlanTermCreditTool.updateTermsCount(plan, getInt("oldTermsCount"), getInt("plan.termsCount"), 
          entityDao)
      }
      majorPlanService.saveOrUpdateMajorPlan(plan)
      return redirect("search", "info.save.success")
    }
    plan = new MajorPlanBean()
    plan.setProgram(program)
    plan.setTermsCount(termsCount)
    majorPlanService.saveOrUpdateMajorPlan(plan)
    redirect(new Action(classOf[MajorPlanAction], "search", "toGroupPane=1&planId=" + plan.getId), "info.save.success")
  }

  def remove(): String = {
    val planIds = getLongIds("plan")
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    guard(MajorProgramOperateType.DELETE, plans)
    val removeEntities = CollectUtils.newArrayList()
    val programs = CollectUtils.newArrayList()
    for (plan <- plans) {
      removeEntities.add(plan)
      programs.add(plan.getProgram)
      removeEntities.add(plan.getProgram)
    }
    val count = entityDao.count(classOf[StudentProgram], "program", programs)
    if (count > 0) {
      return forwardError("删除失败，培养计划已关联")
    }
    removeEntities.addAll(entityDao.get(classOf[ProgramDocBean], "program", programs))
    removeEntities.addAll(entityDao.get(classOf[StudentProgram], "program", programs))
    try {
      entityDao.remove(removeEntities)
    } catch {
      case e: ConstraintViolationException => {
        logger.info("info.delete.failure", e)
        return forwardError("删除失败，请查看是否有教学任务引用了该培养计划")
      }
      case e: DataIntegrityViolationException => {
        logger.info("info.delete.failure", e)
        return forwardError("删除失败，请查看是否有教学任务引用了该培养计划")
      }
      case e: Exception => {
        logger.info("info.delete.failure", e)
        return redirect("search", "info.delete.failure")
      }
    }
    redirect("search", "info.delete.success")
  }

  def edit(): String = {
    val planId = getLongId("plan")
    val plan = entityDao.get(classOf[MajorPlan], planId)
    if (null == plan) {
      return forwardError("error.model.notExist")
    }
    guard(MajorProgramOperateType.UPDATE, plan)
    put("titles", textTitleProvider.provideTitle(plan.getProgram))
    put("isAutoName", plan.getProgram.getName == ProgramNamingHelper.name(plan.getProgram))
    put("plan", plan)
    put("studyTypes", baseCodeService.getCodes(classOf[StudyType]))
    put("degrees", baseCodeService.getCodes(classOf[Degree]))
    put("educations", baseCodeService.getCodes(classOf[Education]))
    put("projects", getProjects)
    forward()
  }

  def newPlan(): String = {
    setDataRealm(hasStdTypeCollege)
    put("studyTypes", baseCodeService.getCodes(classOf[StudyType]))
    put("degrees", baseCodeService.getCodes(classOf[Degree]))
    put("educations", baseCodeService.getCodes(classOf[Education]))
    forward()
  }

  def copyPrompt(): String = {
    val planId = getLongId("plan")
    if (null == planId) {
      throw new EntityNotFoundException("plan")
    }
    setDataRealm(hasStdTypeCollege)
    put("plan", entityDao.get(classOf[MajorPlan], planId))
    put("studyTypes", baseCodeService.getCodes(classOf[StudyType]))
    forward()
  }

  def copy(): String = {
    val sourcePlanId = getLongId("sourcePlan")
    if (null == sourcePlanId) {
      throw new EntityNotFoundException("majorPlan")
    }
    val sourcePlan = entityDao.get(classOf[MajorPlan], sourcePlanId)
    guard(MajorProgramOperateType.COPY, sourcePlan)
    val genPlans = new ArrayList[MajorPlan]()
    val genParameter = populate(classOf[MajorPlanGenParameter], "plan.program")
    if (Strings.isBlank(genParameter.getName)) {
      genParameter.setName(ProgramNamingHelper.name(genParameter, this.entityDao))
    }
    val generatedPlan = majorPlanService.genMajorPlan(sourcePlan, genParameter).asInstanceOf[MajorPlan]
    entityDao.refresh(generatedPlan.getProgram)
    genPlans.add(generatedPlan)
    put("planList", genPlans)
    addMessage("info.gen.success")
    forward("copyCompleteList")
  }

  def duplicateCheck(): String = {
    val program = populate(classOf[Program], "plan.program")
    val response = getResponse
    val duplicatePrograms = planCommonDao.getDuplicatePrograms(program)
    val result = CollectUtils.newHashMap()
    result.put("duplicated", false)
    result.put("duplicatePrograms", Array())
    if (CollectUtils.isNotEmpty(duplicatePrograms)) {
      val names = Array.ofDim[String](duplicatePrograms.size)
      for (i <- 0 until duplicatePrograms.size) {
        names(i) = duplicatePrograms.get(i).getName
      }
      result.put("duplicated", true)
      result.put("duplicatePrograms", names)
    }
    response.setContentType("text/plain;charset=UTF-8")
    response.getWriter.append(new Gson().toJson(result))
    response.getWriter.flush()
    null
  }

  def duplicateNameCheck(): String = {
    val program = populate(classOf[Program], "plan.program")
    var name: String = null
    program.setMajor(entityDao.get(classOf[Major], program.getMajor.getId))
    if (program.getDirection != null) {
      program.setDirection(entityDao.get(classOf[Direction], program.getDirection.getId))
    }
    name = if (getBool("fake.autoname")) ProgramNamingHelper.name(program) else program.getName
    name = Strings.trim(name)
    val query = OqlBuilder.from(classOf[Program], "program")
    query.where("program.name = :name and program.grade = :grade", name, program.getGrade)
      .where("program.major.project = :project", program.getMajor.getProject)
    if (program.isPersisted) {
      query.where("program.id <> :meId", program.getId)
    }
    val programs = entityDao.search(query)
    val result = CollectUtils.newHashMap()
    result.put("name", name)
    result.put("duplicated", false)
    if (CollectUtils.isNotEmpty(programs)) {
      result.put("duplicated", true)
    }
    val response = getResponse
    response.setContentType("text/plain;charset=UTF-8")
    response.getWriter.append(new Gson().toJson(result))
    response.getWriter.flush()
    null
  }

  def batchCopyPrompt(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.parameters.needed")
    }
    put("plans", entityDao.get(classOf[MajorPlan], planIds))
    forward()
  }

  def batchCopy(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.parameters.needed")
    }
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    val genParameter = populate(classOf[MajorPlanGenParameter], "param")
    val genPlans = majorPlanService.genMajorPlans(plans, genParameter)
    addMessage("info.gen.success")
    put("planList", genPlans)
    forward("copyCompleteList")
  }

  def batchProcess(): String = {
    val plans = entityDao.get(classOf[MajorPlan], getLongIds("plan"))
    put("plans", plans)
    var termsCount = java.lang.Integer.MAX_VALUE
    for (plan <- plans if plan.getTermsCount < termsCount) {
      termsCount = plan.getTermsCount
    }
    if (java.lang.Integer.MAX_VALUE != termsCount) {
      put("termsCount", termsCount)
    }
    val delCourseTypes = new HashSet[CourseType]()
    for (plan <- plans; group <- plan.getGroups) {
      delCourseTypes.add(group.getCourseType)
    }
    put("delCourseTypes", delCourseTypes)
    put("courseTypes", baseCodeService.getCodes(classOf[CourseType]))
    val query = OqlBuilder.from(classOf[Department], "department")
    query.where("department.teaching = true")
    put("departmentList", this.entityDao.search(query))
    forward()
  }

  def batchProcessGroup(): String = {
    val action = get("batchAction")
    val plans = entityDao.get(classOf[MajorPlan], Strings.splitToLong(get("planIds")))
    if ("remove" == action) {
      val courseTypeId = getInt("courseType.id")
      val courseType = entityDao.get(classOf[CourseType], courseTypeId)
      for (plan <- plans) {
        val group = plan.getGroup(courseType)
        if (null != group) {
          majorPlanCourseGroupService.removeCourseGroup(group.getId)
        }
      }
      entityDao.saveOrUpdate(plans)
    } else if ("add" == action) {
      for (plan <- plans) {
        val courseGroup = populateEntity(classOf[MajorPlanCourseGroupBean], "courseGroup")
        val existed = plan.getGroup(courseGroup.getCourseType).asInstanceOf[MajorPlanCourseGroup]
        if (null == existed) {
          planCourseGroupCommonDao.addCourseGroupToPlan(courseGroup, plan)
        }
      }
      entityDao.saveOrUpdate(plans)
    }
    redirect(new Action(classOf[MajorPlanAction], "search"), "info.action.success")
  }

  def batchProcessCourseSetting(): String = forward()

  def batchAddCoursePrompt(): String = {
    val courseIds = Strings.splitToLong(get("courseIds"))
    val courseList = entityDao.get(classOf[Course], courseIds)
    put("plans", entityDao.get(classOf[MajorPlan], Strings.splitToLong(get("planIds"))))
    put("planIds", get("planIds"))
    put("courseType", entityDao.get(classOf[CourseType], getInt("courseType.id")))
    put("courseIds", get("courseIds"))
    put("courseList", courseList)
    put("department", entityDao.get(classOf[Department], getInt("department.id")))
    put("terms", get("terms"))
    forward()
  }

  def batchDeleteCoursePrompt(): String = {
    val courseIds = Strings.splitToLong(get("courseIds"))
    val courseList = entityDao.get(classOf[Course], courseIds)
    put("plans", entityDao.get(classOf[MajorPlan], Strings.splitToLong(get("planIds"))))
    put("courseType", entityDao.get(classOf[CourseType], getInt("courseType.id")))
    put("courseList", courseList)
    put("courseIds", get("courseIds"))
    forward()
  }

  def batchAddCourse(): String = {
    val plans = entityDao.get(classOf[MajorPlan], Strings.splitToLong(get("planIds")))
    val courseType = entityDao.get(classOf[CourseType], getInt("courseType.id"))
    val department = entityDao.get(classOf[Department], getInt("department.id"))
    val terms = get("terms")
    val compulsory = getBool("compulsory")
    val courseIds = Strings.splitToLong(get("courseIds"))
    for (plan <- plans) {
      val alreadyExistCourseCodes = new HashSet[String]()

      for (group <- plan.getGroups; planCourse <- group.getPlanCourses if -1 != 
        Arrays.binarySearch(courseIds, planCourse.getCourse.getId) && 
        group.getCourseType == courseType) {
        alreadyExistCourseCodes.add(planCourse.getCourse.getCode)
        //break
      }
      var cgroup: CourseGroup = null
      for (group <- plan.getGroups if group.getCourseType.getId == courseType.getId) {
        cgroup = group
        //break
      }
      if (cgroup == null) {
        //continue
      }
      for (courseId <- courseIds) {
        if (alreadyExistCourseCodes.contains(courseId)) {
          //continue
        }
        val course = entityDao.get(classOf[Course], courseId)
        if (course == null) {
          //continue
        }
        val planCourse = new MajorPlanCourseBean()
        planCourse.setCourse(course)
        planCourse.setTerms(PlanTermCreditTool.normalizeTerms(terms))
        planCourse.setDepartment(department)
        planCourse.setCourseGroup(cgroup)
        planCourse.setCompulsory(compulsory)
        cgroup.getPlanCourses.add(planCourse)
        planCourseCommonDao.addPlanCourse(planCourse, plan)
      }
    }
    redirect(new Action(classOf[MajorPlanAction], "search"), "info.action.success")
  }

  def batchRemoveCourse(): String = {
    val majorPlans = entityDao.get(classOf[MajorPlan], Strings.splitToLong(get("planIds")))
    val courseType = entityDao.get(classOf[CourseType], getInt("courseType.id"))
    val courseIds = Strings.splitToLong(get("courseIds"))
    for (plan <- majorPlans) {
      var cgroup: CourseGroup = null
      for (group <- plan.getGroups if group.getCourseType.getId == courseType.getId) {
        cgroup = group
        //break
      }
      if (cgroup == null) {
        //continue
      }
      val removePlanCourses = new ArrayList[PlanCourse]()
      for (planCourse <- cgroup.getPlanCourses; courseId <- courseIds if planCourse.getCourse.getId == courseId) {
        removePlanCourses.add(planCourse)
      }
      for (planCourse <- removePlanCourses) {
        planCourseCommonDao.removePlanCourse(planCourse.asInstanceOf[MajorPlanCourse], plan)
      }
    }
    redirect(new Action(classOf[MajorPlanAction], "search"), "info.action.success")
  }

  def applyAudit(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.model.ids.needed")
    }
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    guard(MajorProgramOperateType.AUDIT, plans)
    majorPlanAuditService.submit(plans)
    redirect(new Action(classOf[MajorPlanAction], "search"), "提交成功")
  }

  def revokeSubmitted(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("error.model.ids.needed")
    }
    val plans = entityDao.get(classOf[MajorPlan], planIds)
    guard(MajorProgramOperateType.AUDIT, plans)
    val programs = CollectUtils.collect(plans, ProgramCollector.INSTANCE).asInstanceOf[List[_]]
    majorPlanAuditService.revokeSubmitted(programs)
    redirect("search", "操作成功")
  }

  override def search(): String = {
    if (CollectUtils.isEmpty(getProjects) || CollectUtils.isEmpty(getDeparts) || 
      CollectUtils.isEmpty(getStdTypes)) {
      return forwardError("对不起，您没有权限！")
    }
    val query = majorPlanSearchHelper.buildPlanQuery()
    query.where("plan.program.major.project in (:projects)", getProjects)
      .where("plan.program.department in (:departs)", getDeparts)
      .where("plan.program.stdType in (:stdTypes)", getStdTypes)
    if (CollectUtils.isNotEmpty(getEducations)) {
      query.where("plan.program.education in (:educations)", getEducations)
    }
    val plans = entityDao.search(query)
    put("plans", plans)
    put("UNSUBMITTED", CommonAuditState.UNSUBMITTED)
    put("SUBMITTED", CommonAuditState.SUBMITTED)
    put("REJECTED", CommonAuditState.REJECTED)
    put("ACCEPTED", CommonAuditState.ACCEPTED)
    forward()
  }

  def getMajorDefaultDegree(): String = {
    val response = getResponse
    val majorId = getInt("majorId")
    if (majorId == null) {
      response.setContentType("text/plain;charset=UTF-8")
      response.getWriter.write("")
      response.getWriter.close()
      return null
    }
    val major = entityDao.get(classOf[Major], majorId)
    if (major == null) {
      response.setContentType("text/plain;charset=UTF-8")
      response.getWriter.write("")
      response.getWriter.close()
      return null
    }
    response.setContentType("text/plain;charset=UTF-8")
    response.getWriter.close()
    null
  }

  def getMajorDuration(): String = {
    val response = getResponse
    val majorId = getInt("majorId")
    val start = getDate("start")
    if (majorId == null || start == null) {
      response.setContentType("text/plain;charset=UTF-8")
      response.getWriter.write("")
      response.getWriter.close()
      return null
    }
    val major = entityDao.get(classOf[Major], majorId)
    val duration = major.getDuration
    if (major == null || duration == null) {
      response.setContentType("text/plain;charset=UTF-8")
      response.getWriter.write("")
      response.getWriter.close()
      return null
    }
    val mnum = (duration.floatValue() * 12).toInt
    val c = Calendar.getInstance
    c.setTime(start)
    c.add(Calendar.MONTH, mnum)
    val end = c.getTime
    val sdf = new SimpleDateFormat("yyyy-MM-dd")
    val result = CollectUtils.newHashMap()
    result.put("invalidOn", sdf.format(end))
    result.put("duration", duration)
    response.setContentType("text/plain;charset=UTF-8")
    response.getWriter.write(new Gson().toJson(result))
    response.getWriter.close()
    null
  }

  def setMajorPlanCourseGroupService(majorPlanCourseGroupService: MajorPlanCourseGroupService) {
    this.majorPlanCourseGroupService = majorPlanCourseGroupService
  }

  def setMajorPlanAuditService(majorPlanAuditService: MajorPlanAuditService) {
    this.majorPlanAuditService = majorPlanAuditService
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  def setPlanCourseCommonDao(planCourseCommonDao: PlanCourseCommonDao) {
    this.planCourseCommonDao = planCourseCommonDao
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }

  def setCoursePlanProvider(coursePlanProvider: CoursePlanProvider) {
    this.coursePlanProvider = coursePlanProvider
  }

  def setGuards(guards: List[MajorProgramOperateGuard]) {
    this.guards = guards
  }

  private def guard(operType: MajorProgramOperateType, plans: List[MajorPlan]) {
    val context = CollectUtils.newHashMap()
    fillDataRealmContext(context)
    val programs = CollectUtils.collect(plans, ProgramCollector.INSTANCE).asInstanceOf[List[_]]
    for (preDo <- guards) {
      preDo.guard(operType, programs, context)
    }
  }

  private def guard(operType: MajorProgramOperateType, plan: MajorPlan) {
    val context = CollectUtils.newHashMap()
    fillDataRealmContext(context)
    for (preDo <- guards) {
      preDo.guard(operType, plan.getProgram, context)
    }
  }

  private def fillDataRealmContext(context: Map[String, Any]) {
    context.put("realm/checkMe", true)
    context.put("realm/user", entityDao.get(classOf[User], getUserId))
    context.put("realm/project", getProject)
    context.put("realm/stdTypes", getStdTypes)
    context.put("realm/departs", getDeparts)
    context.put("realm/educations", getEducations)
  }

  def setTextTitleProvider(textTitleProvider: MajorProgramTextTitleProvider) {
    this.textTitleProvider = textTitleProvider
  }
}
