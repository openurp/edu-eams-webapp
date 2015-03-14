package org.openurp.edu.eams.teach.program.majorapply.web.action

import java.util.Arrays
import java.util.Collection
import java.util.Comparator
import java.util.HashMap
import java.util.List
import java.util.Map
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.beangle.security.blueprint.User
import org.beangle.struts2.convention.route.Action
import org.beangle.struts2.helper.Params
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.core.code.industry.HSKDegree
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.CourseHour
import com.ekingstar.eams.teach.code.school.CourseHourType
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.exporter.CourseModifyPropertyExtractor
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBeforeBean
import org.openurp.edu.eams.teach.program.majorapply.service.MajorPlanCourseModifyApplyService
import com.ekingstar.eams.web.action.common.ProjectSupportAction
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseModifyApplyAction extends ProjectSupportAction {

  private var majorPlanCourseModifyApplyService: MajorPlanCourseModifyApplyService = _

  private def applyAuthenticationCheck(): Boolean = false

  def checkDuplicated(): String = {
    val planId = getLong("planId")
    val oldCourseCode = get("oldCourseCode")
    val oldCourseGroupId = getLong("oldCourseGroupId")
    val courseCode = get("courseCode")
    val courseGroupId = getLong("courseGroupId")
    var duplicated = false
    if (Strings.isEmpty(oldCourseCode) || null == oldCourseGroupId) {
      val cgroup = entityDao.get(classOf[MajorPlanCourseGroup], courseGroupId)
      for (pcourse <- cgroup.getPlanCourses if pcourse.getCourse.getCode == courseCode) {
        duplicated = true
        //break
      }
    } else {
      if (oldCourseGroupId == courseGroupId) {
        if (oldCourseCode == courseCode) {
        } else {
          val cgroup = entityDao.get(classOf[MajorPlanCourseGroup], courseGroupId)
          for (pcourse <- cgroup.getPlanCourses if pcourse.getCourse.getCode == courseCode) {
            duplicated = true
            //break
          }
        }
      } else {
        val cgroup = entityDao.get(classOf[MajorPlanCourseGroup], courseGroupId)
        for (pcourse <- cgroup.getPlanCourses if pcourse.getCourse.getCode == courseCode) {
          duplicated = true
          //break
        }
      }
    }
    put("duplicated", duplicated)
    forward()
  }

  def applyAdd(): String = {
    val planId = getLong("planId")
    if (null == planId) {
      return forwardError("缺少参数")
    }
    put("plan", entityDao.get(classOf[MajorPlan], planId))
    put("departments", baseInfoService.getBaseInfos(classOf[Department]))
    put("HSKDegrees", baseCodeService.getCodes(classOf[HSKDegree]))
    forward()
  }

  def saveAddApply(): String = {
    val planId = getLong("planId")
    if (null == planId) {
      return forwardError("缺少参数")
    }
    majorPlanCourseModifyApplyService.saveModifyApply(populateApply(), null, populateAfter())
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorPlanModifyApplyAction], "apply", "planId=" + planId), "info.save.success")
  }

  def applyRemove(): String = {
    val planCourseId = getLong("planCourseId")
    if (null == planCourseId) {
      return forwardError("缺少参数")
    }
    val planCourse = entityDao.get(classOf[MajorPlanCourse], planCourseId)
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.join("plan.groups", "cgroup").join("cgroup.planCourses", "planCourse")
      .where("planCourse.id = :planCourseId", planCourseId)
    put("departments", baseInfoService.getBaseInfos(classOf[Department]))
    put("plan", entityDao.search(query).get(0))
    put("planCourse", planCourse)
    forward()
  }

  def saveRemoveApply(): String = {
    val planId = getLong("planId")
    val planCourseId = getLong("planCourse.id")
    if (null == planId || null == planCourseId) {
      return forwardError("缺少参数")
    }
    majorPlanCourseModifyApplyService.saveModifyApply(populateApply(), populateBefore(), null)
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorPlanModifyApplyAction], "apply", "planId=" + planId), "info.save.success")
  }

  def applyModify(): String = {
    val planCourseId = getLong("planCourseId")
    if (null == planCourseId) {
      return forwardError("缺少参数")
    }
    val planCourse = entityDao.get(classOf[MajorPlanCourse], planCourseId)
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.join("plan.groups", "cgroup").join("cgroup.planCourses", "planCourse")
      .where("planCourse.id=:planCourseId", planCourseId)
    put("departments", baseInfoService.getBaseInfos(classOf[Department]))
    put("plan", entityDao.search(query).get(0))
    put("planCourse", planCourse)
    put("HSKDegrees", baseCodeService.getCodes(classOf[HSKDegree]))
    forward()
  }

  def saveModifyApply(): String = {
    val planId = getLong("planId")
    val planCourseId = getLong("planCourse.id")
    if (null == planId || null == planCourseId) {
      return forwardError("缺少参数")
    }
    majorPlanCourseModifyApplyService.saveModifyApply(populateApply(), populateBefore(), populateAfter())
    getFlash.put("params", get("params"))
    redirect(new Action(classOf[MajorPlanModifyApplyAction], "apply", "planId=" + planId), "info.save.success")
  }

  private def populateApply(): MajorPlanCourseModifyBean = {
    val modifyApply = populateEntity(classOf[MajorPlanCourseModifyBean], "modifyApply")
    modifyApply.setProposer(entityDao.get(classOf[User], getUserId))
    modifyApply
  }

  private def populateBefore(): MajorPlanCourseModifyDetailBeforeBean = {
    val planCourseId = getLong("planCourse.id")
    val planCourse = entityDao.get(classOf[MajorPlanCourse], planCourseId)
    val before = new MajorPlanCourseModifyDetailBeforeBean(planCourse)
    before
  }

  private def populateAfter(): MajorPlanCourseModifyDetailAfterBean = {
    val after = populateEntity(classOf[MajorPlanCourseModifyDetailAfterBean], "newPlanCourse")
    after.getFakeCourseGroup.setCourseType(entityDao.get(classOf[MajorPlanCourseGroup], after.getFakeCourseGroup.getId)
      .getCourseType)
    val newCourseCode = get("newPlanCourse.course.code")
    val course = getCourseByCode(newCourseCode)
    after.setCourse(course)
    val courseHourMap = new HashMap[Integer, Integer]()
    for (courseHour <- course.getHours) {
      courseHourMap.put(courseHour.getType.getId, courseHour.getPeriod)
    }
    after.getCourseHours.putAll(courseHourMap)
    val terms = after.getTerms
    val arr = terms.replaceAll("\\s", "").replaceAll("^,", "").replaceAll(",$", "")
      .split(",")
    Arrays.sort(arr, new Comparator[String]() {

      def compare(o1: String, o2: String): Int = {
        return java.lang.Integer.valueOf(o1) - java.lang.Integer.valueOf(o2)
      }
    })
    after.setTerms(',' + Strings.join(arr, ',') + ',')
    after
  }

  def remove(): String = {
    val applyId = getLong("applyId")
    val apply = entityDao.get(classOf[MajorPlanCourseModifyBean], applyId)
    if (null == apply) {
      return forwardError("申请不存在")
    }
    if (apply.getProposer.getId != getUserId) {
      return forwardError("不能取消不是你的申请")
    }
    if (apply.getFlag.compareTo(MajorPlanCourseModifyBean.INITREQUEST) != 
      0) {
      return forwardError("不能取消已经审核过的申请")
    }
    entityDao.remove(apply)
    getFlash.put("params", get("params"))
    getFlash.put("backUrl", get("backUrl"))
    if (getBool("from_of_plan")) {
      return redirect(new Action(classOf[MajorPlanModifyApplyAction], "applicationsOfPlan", "&planId=" + get("planId") + "&tab_n=" + get("tab_n")), 
        "info.success.cancelApply")
    }
    redirect("myApplications", "info.success.cancelApply")
  }

  def info(): String = {
    val apply = entityDao.get(classOf[MajorPlanCourseModifyBean], getLong("applyId"))
    put("apply", apply)
    put("majorPlan", entityDao.get(classOf[MajorPlan], apply.getMajorPlan.getId))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("REQUISITIONTYPE", MajorPlanCourseModifyBean.REQUISITIONTYPE)
    forward()
  }

  def mySearch(): String = {
    put("departs", getDeparts)
    put("proposerId", getUserId)
    forward()
  }

  def myApplications(): String = {
    val query = getQueryBuilder
    query.where("apply.proposer.id = :userId", getUserId)
    put("applications", entityDao.search(query))
    put("param", get("param"))
    forward()
  }

  protected def getQueryBuilder(): OqlBuilder[MajorPlanCourseModifyBean] = {
    val query = OqlBuilder.from(classOf[MajorPlanCourseModifyBean], "apply")
    query.where("exists (select plan.id from org.openurp.edu.eams.teach.program.major.MajorPlan plan " + 
      "where plan.id=apply.majorPlan.id and plan.program.department in (:departs))", getDeparts)
    val requestDate = get("requestDate")
    if (requestDate != null && requestDate != "") {
      query.where("str(year(apply.applyDate)) like :requestDate", "%" + requestDate + "%")
    }
    var courseName = get("courseName")
    courseName = if (Strings.isBlank(courseName)) "%" else "%" + courseName + "%"
    var courseCode = get("courseCode")
    courseCode = if (Strings.isBlank(courseCode)) "%" else "%" + courseCode + "%"
    val subquery = new StringBuilder()
    subquery.append("(exists(\n").append(" select state.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBeforeBean state\n")
      .append(" where state.apply.id = apply.id\n")
      .append(" and state.course.name like :courseName1\n")
      .append(" and state.course.code like :courseCode1\n")
      .append(") or exists(\n")
      .append(" select state.id from org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailAfterBean state\n")
      .append(" where state.apply.id = apply.id\n")
      .append(" and state.course.name like :courseName2\n")
      .append(" and state.course.code like :courseCode2\n")
      .append("))")
    val cond = new Condition(subquery.toString)
    cond.getParams.add(courseName)
    cond.getParams.add(courseCode)
    cond.getParams.add(courseName)
    cond.getParams.add(courseCode)
    query.where(cond)
    val teachDepartId = getInt("teachDepartId")
    if (teachDepartId != null) {
      query.where("apply.department.id =:teachDepartId", teachDepartId)
    }
    val proposerId = getLong("apply.proposer.id")
    if (proposerId != null) {
      query.where("apply.proposer.id = :proposerId", proposerId)
    }
    val flag = getInt("apply.flag")
    if (null != flag) {
      query.where("apply.flag = :flag", flag)
    }
    if (Strings.isEmpty(Params.get(Order.ORDER_STR))) {
      query.orderBy(Order.desc("apply.applyDate"))
      query.orderBy(Order.asc("apply.flag"))
    } else {
      query.orderBy(Params.get(Order.ORDER_STR))
    }
    query.limit(getPageLimit)
    query
  }

  private def getCourseByCode(code: String): Course = {
    if (Strings.isNotBlank(code)) {
      val oql = OqlBuilder.from(classOf[Course], "course")
      oql.where("course.code =:code", code)
      val courseList = entityDao.search(oql)
      if (courseList != null && courseList.size > 0) {
        return courseList.get(0)
      }
    }
    null
  }

  protected def getExportDatas(): Collection[_] = {
    val query = getQueryBuilder
    query.limit(null)
    entityDao.search(query)
  }

  protected def getPropertyExtractor(): PropertyExtractor = {
    val extractor = new CourseModifyPropertyExtractor(getTextResource)
    extractor.setEntityDao(entityDao)
    extractor.setTextResource(getTextResource)
    extractor
  }

  def setMajorPlanCourseModifyApplyService(majorPlanCourseModifyApplyService: MajorPlanCourseModifyApplyService) {
    this.majorPlanCourseModifyApplyService = majorPlanCourseModifyApplyService
  }
}
