package org.openurp.edu.eams.teach.program.personal.web.action

import java.io.UnsupportedEncodingException

import java.util.Comparator


import org.beangle.commons.bean.comparators.MultiPropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.page.PageLimit
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.code.school.CourseHourType
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.common.copydao.coursegroup.IPlanCourseGroupCopyDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.model.ReferenceGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorCourseGroupService
import org.openurp.edu.eams.teach.program.personal.PersonalPlan
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourse
import org.openurp.edu.eams.teach.program.personal.PersonalPlanCourseGroup
import org.openurp.edu.eams.teach.program.personal.model.PersonalPlanCourseGroupBean
import org.openurp.edu.eams.teach.program.share.SharePlanCourseGroup
import com.ekingstar.eams.web.action.common.RestrictionSupportAction
//remove if not needed


class PersonalPlanCourseGroupAction extends RestrictionSupportAction {

  var planCommonDao: PlanCommonDao = _

  var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  var personalPlanCourseGroupCopyDao: IPlanCourseGroupCopyDao = _

  var MajorCourseGroupService: MajorCourseGroupService = _

  def arrangeGroupCourses(): String = {
    val groupId = getLong("courseGroup.id")
    val planId = getLong("planId")
    val plan = entityDao.get(classOf[PersonalPlan], planId)
    val group = entityDao.get(classOf[PersonalPlanCourseGroup], groupId)
    val planCoursesList = new ArrayList(group.getPlanCourses)
    val propertry = new MultiPropertyComparator("terms,course.code")
    Collections.sort(planCoursesList, propertry)
    put("plan", plan)
    put("courseGroup", group)
    put("planCoursesList", planCoursesList)
    putTeachingDepartments()
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
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
    putTeachingDepartments()
    put("courseList", courseList)
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("notAddCodes", notAddCodes)
    forward()
  }

  def batchEditCourses(): String = {
    var planCourseIds = get("planCourseIds")
    if (Strings.isNotEmpty(planCourseIds)) {
      planCourseIds = planCourseIds.replaceAll("[\\s;，；]", ",").replaceAll(",,", ",")
      val ids = Strings.split(planCourseIds)
      putTeachingDepartments()
      put("planCourses_", entityDao.get(classOf[PersonalPlanCourse], Strings.transformToLong(ids)))
    }
    forward()
  }

  def advanceEdit(): String = {
    val courseGroupId = getLong("personalPlanCourseGroupId")
    val personalPlanId = getLong("planId")
    if (null == courseGroupId || null == personalPlanId) {
      return forwardError("error.model.notExist")
    }
    val personalPlan = entityDao.get(classOf[PersonalPlan], personalPlanId)
    if (null == personalPlan) {
      return forwardError("error.model.notExist")
    }
    val params = CollectUtils.newHashMap()
    var grade = personalPlan.getStd.getGrade
    if (Strings.isNotEmpty(grade) && grade.length > 4) {
      grade = grade.substring(0, 4)
    }
    params.put("grade", grade)
    params.put("education", personalPlan.getStd.getEducation)
    val query = OqlBuilder.from(classOf[SharePlanCourseGroup], "sharePlanCourseGroup")
      .where(new Condition("sharePlanCourseGroup.plan.name =:grade and sharePlanCourseGroup.plan.education =:education"))
      .params(params)
    put("shareCourseGroups", entityDao.search(query))
    val planCourseGroup = entityDao.get(classOf[PersonalPlanCourseGroupBean], courseGroupId)
    var excludeCourses = new ArrayList[Course]()
    var requiredCourses = new ArrayList[Course]()
    if (null != planCourseGroup) {
      excludeCourses = if (null == planCourseGroup.getReferenceGroup) null else planCourseGroup.getReferenceGroup.getExcludeCourses
      requiredCourses = if (null == planCourseGroup.getReferenceGroup) null else planCourseGroup.getReferenceGroup.getRequiredCourses
    }
    put("excludeCourses", excludeCourses)
    put("requiredCourses", requiredCourses)
    put("personalPlanCourseGroup", planCourseGroup)
    put("plan", personalPlan)
    forward()
  }

  def edit(): String = {
    val planId = getLong("plan.id")
    if (null == planId) {
      return forwardError("error.model.notExist")
    }
    val plan = entityDao.get(classOf[PersonalPlan], planId)
    val unusedCourseTypeList = planCommonDao.getUnusedCourseTypes(plan)
    val courseGroupId = getLong("courseGroupId")
    if (null != courseGroupId) {
      val group = entityDao.get(classOf[PersonalPlanCourseGroup], courseGroupId)
      unusedCourseTypeList.add(group.getCourseType)
      put("courseGroup", group)
    } else {
      put("courseGroup", new PersonalPlanCourseGroupBean())
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
      return forwardError("error.model.notExist")
    }
    val group = entityDao.get(classOf[PersonalPlanCourseGroupBean], groupId)
    val extra = "&toGroupPane=1&planId=" + planId
    try {
      val shareCourseGroupId = get("personalPlanCourseGroup.referenceGroup.shareCourseGroup.id")
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
      redirect(new Action("personalPlan", "edit", extra), "info.save.success")
    } catch {
      case e: Exception => {
        getFlash.put("params", get("params"))
        redirect(new Action("personalPlan", "edit", extra), "info.save.failure")
      }
    }
  }

  def copyCourseGroupSetting(): String = {
    val planId = getLong("planId")
    val query = OqlBuilder.from(classOf[PersonalPlan], "personalPlan1")
      .where("personalPlan1.major.project.id = :projectId", getSession.get("projectId"))
      .orderBy("personalPlan1.grade")
    populateConditions(query)
    put("personalPlans", entityDao.search(query))
    put("planId", planId)
    forward()
  }

  def copyCourseCroup(): String = {
    val planId = getLong("planId")
    if (null == planId) {
      addMessage("error.model.notExist")
      return forward()
    }
    val extra = "&toGroupPane=1&planId=" + planId
    val personalPlanCourseGroupId = getLong("personalPlanCourseGroupId")
    val personalPlanIds = Strings.splitToLong(get("personalPlanIds"))
    if (personalPlanIds.length == 0) {
      return redirect(new Action("personalPlan", "edit", extra), "info.save.failure")
    }
    val personalPlanCourseGroup = entityDao.get(classOf[PersonalPlanCourseGroup], personalPlanCourseGroupId)
    val personalPlans = entityDao.search(OqlBuilder.from(classOf[PersonalPlan], "personalPlan")
      .where("personalPlan.id in (:planIds)", personalPlanIds))
    val newList = new ArrayList[PersonalPlan]()
    for (personalPlan <- personalPlans) {
      planCourseGroupCommonDao.updateGroupTreeCredits(personalPlanCourseGroup)
      personalPlan.setCredits(planCommonDao.statPlanCredits(personalPlan))
      newList.add(personalPlan)
    }
    if (CollectUtils.isNotEmpty(newList)) {
      entityDao.saveOrUpdate(newList)
    }
    getFlash.put("params", get("params"))
    redirect(new Action("personalPlan", "edit", extra), "info.save.success")
  }

  def save(): String = {
    val planId = getLong("planId")
    if (null == planId) {
      addMessage("error.model.notExist")
      return forward()
    }
    val extra = "&toGroupPane=1&planId=" + planId
    val plan = entityDao.get(classOf[PersonalPlan], planId)
    val group = populateEntity(classOf[PersonalPlanCourseGroupBean], "courseGroup")
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
    plan.setCredits(planCommonDao.statPlanCredits(plan))
    entityDao.saveOrUpdate(plan)
    redirect(new Action("personalPlan", "edit", extra), "info.save.success")
  }

  def removeGroup(): String = {
    val groupId = getLong("courseGroup.id")
    val planId = getLong("planId")
    if (null == planId || null == groupId) {
      return forwardError("error.model.notExist")
    }
    val group = entityDao.get(classOf[PersonalPlanCourseGroup], groupId)
    planCourseGroupCommonDao.removeCourseGroup(group)
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + planId
    redirect(new Action("personalPlan", "edit", extra), "info.save.success")
  }

  def groupMoveUp(): String = {
    val groupId = getLong("courseGroup.id")
    if (null == groupId) {
      return forwardError("error.model.notExist")
    }
    val group = entityDao.get(classOf[PersonalPlanCourseGroup], groupId)
    planCourseGroupCommonDao.updateCourseGroupMoveUp(group)
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + get("planId")
    redirect(new Action(classOf[PersonalPlanAction], "edit", extra), "info.save.success")
  }

  def groupMoveDown(): String = {
    val groupId = getLong("courseGroup.id")
    if (null == groupId) {
      return forwardError("error.model.notExist")
    }
    val group = entityDao.get(classOf[PersonalPlanCourseGroup], groupId)
    planCourseGroupCommonDao.updateCourseGroupMoveDown(group)
    getFlash.put("params", get("params"))
    val extra = "&toGroupPane=1&planId=" + get("planId")
    redirect(new Action(classOf[PersonalPlanAction], "edit", extra), "info.save.success")
  }
}
