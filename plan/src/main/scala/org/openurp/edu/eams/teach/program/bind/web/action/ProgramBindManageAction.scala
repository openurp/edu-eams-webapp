package org.openurp.edu.eams.teach.program.bind.web.action

import java.util.Date


import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.QueryHelper
import com.ekingstar.eams.base.Campus
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.core.Direction
import com.ekingstar.eams.core.Major
import com.ekingstar.eams.core.Student
import com.ekingstar.eams.core.code.industry.Education
import com.ekingstar.eams.core.code.industry.StdStatus
import com.ekingstar.eams.core.code.school.StdType
import com.ekingstar.eams.teach.code.school.CourseCategory
import com.ekingstar.eams.teach.major.helper.MajorPlanSearchHelper
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.StudentProgram
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.eams.teach.program.major.model.MajorPlanBean
import org.openurp.edu.eams.teach.program.model.ProgramBean
import org.openurp.edu.eams.teach.program.service.AmbiguousMajorProgramException
import org.openurp.edu.eams.teach.program.service.NoMajorProgramException
import org.openurp.edu.eams.teach.program.service.StudentProgramBindService
import com.ekingstar.eams.web.action.common.ProjectSupportAction
//remove if not needed


class ProgramBindManageAction extends ProjectSupportAction {

  var studentProgramBindService: StudentProgramBindService = _

  var majorPlanSearchHelper: MajorPlanSearchHelper = _

  protected override def indexSetting() {
    put("stdStatuses", baseCodeService.getCodes(classOf[StdStatus]))
    put("project", getProject)
  }

  def search(): String = {
    val students = entityDao.search(buidOql())
    put("students", students)
    val stdPrograms = Collections.newMap[Any]
    if (Collections.isNotEmpty(students)) {
      val programs = entityDao.get(classOf[StudentProgram], "std", students)
      for (studentProgram <- programs) {
        stdPrograms.put(studentProgram.getStd.id, studentProgram)
      }
      put("studentPrograms", stdPrograms)
    }
    put("stdTypes", baseCodeService.getCodes(classOf[StdType]))
    put("project", getProject)
    put("projects", getProjects)
    forward()
  }

  def autobind(): String = {
    val stdId = get("stdIds")
    val strStdIds = stdId.split(",")
    val stdIds = Array.ofDim[Long](strStdIds.length)
    for (i <- 0 until stdIds.length) {
      stdIds(i) = java.lang.Long.parseLong(strStdIds(i))
    }
    val stds = entityDao.get(classOf[Student], stdIds)
    val multiChoiceStds = Collections.newMap[Any]
    val noChoiceStds = Collections.newBuffer[Any]
    val conditions = get("condition")
    val withStdType = conditions.indexOf("stdType") != -1
    val withDirection = conditions.indexOf("direction") != -1
    for (std <- stds) {
      try {
        studentProgramBindService.autobind(std, withStdType, withDirection)
      } catch {
        case e: AmbiguousMajorProgramException => multiChoiceStds.put(std, e.getAmbiguousPrograms)
        case e: NoMajorProgramException => noChoiceStds.add(std)
      }
    }
    if (Collections.isNotEmpty(multiChoiceStds.keySet) || Collections.isNotEmpty(noChoiceStds)) {
      put("multiChoiceStds", multiChoiceStds)
      put("noChoiceStds", noChoiceStds)
      return forward("problemBind")
    }
    redirect("search", "自动绑定成功")
  }

  def halfAutoBind(): String = {
    val stdIds = getLongIds("std")
    for (stdId <- stdIds) {
      val myProgramId = getLong("student_program_" + stdId)
      studentProgramBindService.forcebind(entityDao.get(classOf[Student], stdId), entityDao.get(classOf[Program], 
        myProgramId))
    }
    redirect("search", "绑定成功")
  }

  def unbind(): String = {
    val stdIds = getLongIds("std")
    val stds = entityDao.get(classOf[Student], stdIds)
    for (std <- stds) {
      studentProgramBindService.unbind(std)
    }
    redirect("search", "取消绑定成功")
  }

  def manualbindPrompt(): String = {
    val stdIds = getLongIds("std")
    val stds = entityDao.get(classOf[Student], stdIds)
    put("stds", stds)
    val plan = new MajorPlanBean()
    val program = new ProgramBean()
    plan.setProgram(program)
    val education = stds.get(0).getEducation
    val stdType = stds.get(0).getType
    val department = stds.get(0).getDepartment
    val major = stds.get(0).getMajor
    val direction = stds.get(0).getDirection
    program.setEducation(education)
    program.setStdType(stdType)
    program.setDepartment(department)
    program.setMajor(major)
    program.setDirection(direction)
    val grade = stds.get(0).getGrade
    program.setGrade(grade)
    var campus = stds.get(0).getCampus
    for (i <- 0 until stds.size) {
      if (Objects.!=(stds.get(i).getGrade, grade)) program.setGrade(null)
      if (stds.get(i).getEducation != null && education.id != stds.get(i).getEducation.id) {
        program.setEducation(null)
      }
      if (stds.get(i).getType != null && stdType.id != stds.get(i).getType.id) {
        program.setStdType(null)
      }
      if (stds.get(i).getDepartment != null && 
        department.id != stds.get(i).getDepartment.id) {
        program.setDepartment(null)
      }
      if (stds.get(i).getMajor != null && major.id != stds.get(i).getMajor.id) {
        program.setMajor(null)
      }
      if (stds.get(i).getDirection != null && direction.id != stds.get(i).getDirection.id) {
        program.setDirection(null)
      }
      if (Objects.!=(stds.get(i).getCampus, campus)) {
        campus = null
      }
    }
    put("plan", plan)
    put("campus", campus)
    put("stateList", CommonAuditState.values)
    put("courseCategorys", baseCodeService.getCodes(classOf[CourseCategory]))
    put("majors", entityDao.getAll(classOf[Major]))
    put("educations", baseCodeService.getCodes(classOf[Education]))
    put("project", getProject)
    put("projects", getProjects)
    forward("manualPrompt/index")
  }

  def manualbind_search(): String = {
    if (Collections.isEmpty(getProjects) || Collections.isEmpty(getDeparts) || 
      Collections.isEmpty(getStdTypes)) {
      return forwardError("对不起，您没有权限！")
    }
    val query = majorPlanSearchHelper.buildPlanQuery()
    query.where("plan.program.major.project in (:projects)", getProjects)
      .where("plan.program.department in (:departs)", getDeparts)
      .where("plan.program.stdType in (:stdTypes)", getStdTypes)
    if (Collections.isNotEmpty(getEducations)) {
      query.where("plan.program.education in (:educations)", getEducations)
    }
    val plans = entityDao.search(query)
    put("plans", plans)
    forward("manualPrompt/list")
  }

  def manualbind(): String = {
    val stdIds = getLongIds("std")
    val planId = getLongId("plan")
    val plan = entityDao.get(classOf[MajorPlan], planId)
    for (stdId <- stdIds) {
      studentProgramBindService.forcebind(entityDao.get(classOf[Student], stdId), plan.getProgram)
    }
    redirect("search", "手动绑定成功")
  }

  private def buidOql(): OqlBuilder[Student] = {
    val query = OqlBuilder.from(classOf[Student], "std")
    populateConditions(query)
    val haveProgram = get("fake.haveProgram")
    if ("HAS" == haveProgram) {
      query.where("exists (from org.openurp.edu.eams.teach.program.StudentProgram program where program.std =std)")
    } else if ("HASNT" == haveProgram) {
      query.where("not exists (from org.openurp.edu.eams.teach.program.StudentProgram program where program.std =std)")
    }
    val active = getBoolean("fake.std.active")
    val inSchool = getBoolean("fake.std.inSchool")
    val stdStatusId = getLong("fake.std.status.id")
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
    if (Collections.isNotEmpty(getStdTypes)) {
      query.where("std.type in (:stdTypes)", getStdTypes)
    }
    if (Collections.isNotEmpty(getEducations)) {
      query.where("std.education in (:educations)", getEducations)
    }
    if (Strings.isNotBlank(get("orderBy"))) {
      query.orderBy(get("orderBy"))
    } else {
      query.orderBy("std.grade, std.code")
    }
    query.limit(QueryHelper.getPageLimit)
    query
  }
}
