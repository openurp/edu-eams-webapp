package org.openurp.edu.eams.teach.program.major.web.action

import java.io.UnsupportedEncodingException

import java.util.Comparator

import org.beangle.commons.bean.comparators.MultiPropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.core.code.industry.HSKDegree
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.code.school.CourseHourType
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.IPlanCourseGroupCopyDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorCourseGroupBean
import org.openurp.edu.eams.teach.program.major.model.ReferenceGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorCourseGroupService
import org.openurp.edu.eams.teach.program.major.service.MajorPlanCourseService
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.program.share.SharePlanCourseGroup
import com.ekingstar.eams.web.action.common.RestrictionSupportAction
//remove if not needed


class MajorCourseGroupAction extends RestrictionSupportAction {

  protected var majorPlanCourseService: MajorPlanCourseService = _

  protected var MajorCourseGroupService: MajorCourseGroupService = _

  protected var majorPlanService: MajorPlanService = _

  protected var planCommonDao: PlanCommonDao = _

  protected var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  protected var MajorCourseGroupCopyDao: IPlanCourseGroupCopyDao = _

  def arrangeGroupCourses(): String = {
    val groupId = getLong("courseGroup.id")
    val group = entityDao.get(classOf[MajorCourseGroup], groupId)
    val planCoursesList = new ArrayList(group.getPlanCourses)
    val propertry = new MultiPropertyComparator("terms,course.code")
    Collections.sort(planCoursesList, propertry)
    if (getLong("planId") != null) put("plan", entityDao.get(classOf[MajorPlan], getLong("planId")))
    put("courseGroup", group)
    put("planCoursesList", planCoursesList)
    putTeachingDepartments()
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("HSKDegreeList", baseCodeService.getCodes(classOf[HSKDegree]))
    forward()
  }

  private def putTeachingDepartments() {
    put("departments", getProject.getTeachingDeparts)
  }

  def courses(): String = {
    put("projects", getProjects)
    val query = OqlBuilder.from(classOf[Course], "course")
    populateConditions(query)
    query.where("course.project in (:projects)", getProjects)
      .where("course.enabled = :enabled", true)
    val limit = getPageLimit
    limit.setPageSize(10)
    query.limit(limit)
    put("courseList", entityDao.search(query))
    if (getLong("course.project.id") != null) {
      put("projectId", getLong("course.project.id"))
    }
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def batchCourses(): String = {
    var codes = get("courseCodes")
    codes = codes.replaceAll("[\\s;，；]", ",").replaceAll(",,", ",")
    val projectId = getInt("projectId")
    val courseList = new ArrayList[Course]()
    val notAddCodes = new ArrayList[String]()
    val codeArr = Strings.split(codes)
    for (code <- codeArr) {
      val t = entityDao.get(classOf[Course], "code", code)
      var b = false
      if (CollectUtils.isNotEmpty(t)) {
        val course = t.get(0)
        if (course.isEnabled && course.getProject.id == projectId && 
          !courseList.contains(course)) {
          courseList.add(course)
          b = true
        }
      } else {
        val t1 = entityDao.search(OqlBuilder.from(classOf[Course], "c").where("c.name like :name", "%" + code.trim() + "%"))
        for (course <- t1 if course.isEnabled && course.getProject.id == projectId && 
          !courseList.contains(course)) {
          courseList.add(course)
          b = true
        }
      }
      if (!b) {
        notAddCodes.add(code)
      }
    }
    Collections.sort(courseList, new Comparator[Course]() {

      def compare(o1: Course, o2: Course): Int = return o1.getCode.compareTo(o2.getCode)
    })
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    putTeachingDepartments()
    put("courseList", courseList)
    put("notAddCodes", notAddCodes)
    forward()
  }

  def batchEditCourses(): String = {
    var planCourseIds = get("planCourseIds")
    if (Strings.isNotEmpty(planCourseIds)) {
      planCourseIds = planCourseIds.replaceAll("[\\s;，；]", ",").replaceAll(",,", ",")
      val ids = Strings.split(planCourseIds)
      putTeachingDepartments()
      put("planCourses_", entityDao.get(classOf[MajorPlanCourse], Strings.transformToLong(ids)))
    }
    forward()
  }

  def advanceEdit(): String = {
    val courseGroupId = getLong("MajorCourseGroupId")
    val majorPlanId = getLong("planId")
    if (null == courseGroupId || null == majorPlanId) {
      return forwardError("error.model.notExist")
    }
    val majorPlan = entityDao.get(classOf[MajorPlan], majorPlanId)
    if (null == majorPlan) {
      return forwardError("error.model.notExist")
    }
    val query = OqlBuilder.from(classOf[SharePlanCourseGroup], "sharePlanCourseGroup")
      .where("sharePlanCourseGroup.plan.education =:education", majorPlan.getProgram.getEducation)
    put("shareCourseGroups", entityDao.search(query))
    val planCourseGroup = entityDao.get(classOf[MajorCourseGroup], courseGroupId).asInstanceOf[MajorCourseGroupBean]
    var excludeCourses = new ArrayList[Course]()
    var requiredCourses = new ArrayList[Course]()
    if (null != planCourseGroup) {
      excludeCourses = if (null == planCourseGroup.getReferenceGroup) null else planCourseGroup.getReferenceGroup.getExcludeCourses
      requiredCourses = if (null == planCourseGroup.getReferenceGroup) null else planCourseGroup.getReferenceGroup.getRequiredCourses
    }
    put("excludeCourses", excludeCourses)
    put("requiredCourses", requiredCourses)
    put("MajorCourseGroup", planCourseGroup)
    put("plan", majorPlan)
    forward()
  }

  def edit(): String = {
    val planId = getLong("plan.id")
    if (null == planId) {
      return forwardError("error.model.notExist")
    }
    val plan = entityDao.get(classOf[MajorPlan], planId)
    val unusedCourseTypeList = baseCodeService.getCodes(classOf[CourseType])
    val courseGroupId = getLong("courseGroupId")
    if (null != courseGroupId) {
      val group = entityDao.get(classOf[MajorCourseGroup], courseGroupId)
      put("courseGroup", group)
    } else {
      put("courseGroup", new MajorCourseGroupBean())
    }
    put("unusedCourseTypeList", unusedCourseTypeList)
    put("parentCourseGroupList", plan.getGroups)
    put("plan", plan)
    forward()
  }

  def saveAdvance(): String = {
    val planId = getLong("planId")
    val groupId = getLong("groupId")
    if (null == planId || null == groupId) {
      addMessage("error.model.notExist")
      return forward()
    }
    val group = entityDao.get(classOf[MajorCourseGroup], groupId).asInstanceOf[MajorCourseGroupBean]
    val extra = "&toGroupPane=1&planId=" + planId
    try {
      val shareCourseGroupId = get("MajorCourseGroup.referenceGroup.shareCourseGroup.id")
      if (Strings.isNotEmpty(shareCourseGroupId)) {
        if (null == group.getReferenceGroup) {
          group.setReferenceGroup(new ReferenceGroupBean())
        }
        var excludeCourses = new ArrayList[Course]()
        var requiredCourses = new ArrayList[Course]()
        var courseIds = get("excludeCourses")
        if (Strings.isNotBlank(courseIds)) {
          excludeCourses = entityDao.get(classOf[Course], Strings.splitToLong(courseIds))
          group.getReferenceGroup.getExcludeCourses.clear()
          group.getReferenceGroup.getExcludeCourses.addAll(excludeCourses)
        } else {
          if (null != group.getReferenceGroup.getExcludeCourses) group.getReferenceGroup.getExcludeCourses.clear()
        }
        courseIds = get("requiredCourses")
        if (Strings.isNotBlank(courseIds)) {
          requiredCourses = entityDao.get(classOf[Course], Strings.splitToLong(courseIds))
          group.getReferenceGroup.getRequiredCourses.clear()
          group.getReferenceGroup.getRequiredCourses.addAll(requiredCourses)
        } else {
          if (null != group.getReferenceGroup.getRequiredCourses) group.getReferenceGroup.getRequiredCourses.clear()
        }
        group.getReferenceGroup.setShareCourseGroup(entityDao.get(classOf[SharePlanCourseGroup], java.lang.Long.valueOf(shareCourseGroupId)))
      }
      if (Strings.isEmpty(shareCourseGroupId) && group.getReferenceGroup != null) {
        group.setReferenceGroup(null)
      }
      planCourseGroupCommonDao.saveOrUpdateCourseGroup(group)
      getFlash.put("params", get("params"))
      redirect(new Action("majorPlan", "edit", extra), "info.save.success")
    } catch {
      case e: Exception => {
        getFlash.put("params", get("params"))
        redirect(new Action("majorPlan", "edit", extra), "info.save.failure")
      }
    }
  }

  def copyCourseGroupSetting(): String = {
    val planId = getLong("planId")
    val query = OqlBuilder.from(classOf[MajorPlan], "majorPlan1").where("majorPlan1.program.auditState is null or majorPlan1.program.auditState <> :auditStateId1", 
      CommonAuditState.ACCEPTED)
      .where("majorPlan1.program.auditState is null or majorPlan1.program.auditState <> :auditStateId2", 
      CommonAuditState.SUBMITTED)
      .where("majorPlan1.program.major.project.id = :projectId", getSession.get("projectId"))
      .where("majorPlan1.program.department in (:departs)", getDeparts)
      .where("majorPlan1.id <> :meId", planId)
    if (CollectUtils.isNotEmpty(getEducations)) {
      query.where("majorPlan1.program.education in (:educations)", getEducations)
    }
    if (Strings.isEmpty(get("orderBy"))) {
      query.orderBy("majorPlan1.program.grade")
    } else {
      query.orderBy(get("orderBy"))
    }
    populateConditions(query)
    put("majorPlans", entityDao.search(query))
    put("planId", planId)
    forward()
  }

  def copyCourseCroup(): String = {
    val planId = getLong("planId")
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + planId
    if (null == planId) {
      return redirect(new Action("majorPlan", "edit", extra), "error.model.notExist")
    }
    val MajorCourseGroupId = getLong("MajorCourseGroupId")
    val majorPlanIds = Strings.splitToLong(get("majorPlanIds"))
    if (majorPlanIds.length == 0) {
      return redirect(new Action("majorPlan", "edit", extra), "info.save.failure")
    }
    val MajorCourseGroup = entityDao.get(classOf[MajorCourseGroup], MajorCourseGroupId)
    val majorPlans = entityDao.search(OqlBuilder.from(classOf[MajorPlan], "majorPlan").where("majorPlan.id in (:majorPlanIds)", 
      majorPlanIds))
    val newList = CollectUtils.newArrayList()
    val failure = CollectUtils.newArrayList()
    for (majorPlan <- majorPlans) {
      if (MajorCourseGroupService.hasSameGroupInOneLevel(MajorCourseGroup, majorPlan, null)) {
        failure.add(majorPlan)
        //continue
      }
      val clone = MajorCourseGroupCopyDao.copyCourseGroup(MajorCourseGroup, null, majorPlan)
      MajorCourseGroupService.move(clone, null, majorPlan.getTopCourseGroups.size + 1)
      planCourseGroupCommonDao.updateGroupTreeCredits(MajorCourseGroup)
      majorPlan.setCredits(planCommonDao.statPlanCredits(majorPlan))
      newList.add(majorPlan)
    }
    if (CollectUtils.isNotEmpty(newList)) {
      entityDao.saveOrUpdate(newList)
    }
    redirect(new Action("majorPlan", "edit", extra), if (failure.isEmpty) "info.action.success" else failure.size + "个培养方案复制失败,其顶层组已有相同类别课程组")
  }

  def save(): String = {
    val planId = getLong("planId")
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + planId
    if (null == planId) {
      return redirect(new Action("majorPlan", "edit", extra), "error.model.notExist")
    }
    val plan = entityDao.get(classOf[MajorPlan], planId)
    val group = populateEntity(classOf[MajorCourseGroupBean], "courseGroup")
    val oldParent = group.getParent
    val parentId = getLong("newParentId")
    var parent: CourseGroup = null
    var indexno = 0
    if (parentId != null) {
      parent = entityDao.get(classOf[MajorCourseGroup], parentId)
      indexno = parent.getChildren.size + 1
    } else {
      indexno = plan.getTopCourseGroups.size + 1
    }
    if (MajorCourseGroupService.hasSameGroupInOneLevel(group, plan, parent)) {
      return redirect(new Action("majorPlan", "edit", extra), "同层次下已有相同类别课程组")
    }
    if (group.isPersisted) {
      if ((parent != null && oldParent != null && parentId != oldParent.id) || 
        (parent == null && oldParent != null) || 
        (parent != null && oldParent == null)) {
        MajorCourseGroupService.move(group, parent, indexno)
      }
      if (oldParent != null) {
        planCourseGroupCommonDao.updateGroupTreeCredits(oldParent)
      }
      planCourseGroupCommonDao.saveOrUpdateCourseGroup(group)
    } else {
      group.setIndexno("--")
      planCourseGroupCommonDao.addCourseGroupToPlan(group, parent, plan)
      MajorCourseGroupService.move(group, parent, indexno)
    }
    plan.setCredits(majorPlanService.statPlanCredits(plan))
    entityDao.saveOrUpdate(plan)
    redirect(new Action("majorPlan", "edit", extra), "info.save.success")
  }

  def removeGroup(): String = {
    val groupId = getLong("courseGroup.id")
    val planId = getLong("planId")
    if (null == planId || null == groupId) {
      return forwardError("error.model.notExist")
    }
    MajorCourseGroupService.removeCourseGroup(groupId)
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + planId
    redirect(new Action("majorPlan", "edit", extra), "info.save.success")
  }

  def groupMoveUp(): String = {
    val groupId = getLong("courseGroup.id")
    if (null == groupId) {
      return forwardError("error.model.notExist")
    }
    val group = entityDao.get(classOf[MajorCourseGroup], groupId)
    MajorCourseGroupService.move(group, group.getParent, group.getIndex - 1)
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + get("planId")
    redirect(new Action(classOf[MajorPlanAction], "edit", extra), "info.save.success")
  }

  def groupMoveDown(): String = {
    val groupId = getLong("courseGroup.id")
    if (null == groupId) {
      return forwardError("error.model.notExist")
    }
    val group = entityDao.get(classOf[MajorCourseGroup], groupId)
    MajorCourseGroupService.move(group, group.getParent, group.getIndex + 1)
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + get("planId")
    redirect(new Action(classOf[MajorPlanAction], "edit", extra), "info.save.success")
  }

  def setMajorPlanCourseService(majorPlanCourseService: MajorPlanCourseService) {
    this.majorPlanCourseService = majorPlanCourseService
  }

  def setMajorCourseGroupService(MajorCourseGroupService: MajorCourseGroupService) {
    this.MajorCourseGroupService = MajorCourseGroupService
  }

  def setMajorPlanService(majorPlanService: MajorPlanService) {
    this.majorPlanService = majorPlanService
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }

  def setMajorCourseGroupCopyDao(MajorCourseGroupCopyDao: IPlanCourseGroupCopyDao) {
    this.MajorCourseGroupCopyDao = MajorCourseGroupCopyDao
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }

  def getEntityName(): String = classOf[MajorCourseGroup].getName
}
