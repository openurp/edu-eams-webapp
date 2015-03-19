package org.openurp.edu.eams.teach.program.major.web.action

import org.beangle.commons.web.util.RequestUtils.encodeAttachName

import java.util.Comparator




import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.ArrayUtils
import org.apache.struts2.ServletActionContext
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.seq.MultiLevelSeqGenerator
import org.beangle.commons.text.seq.SeqNumStyle
import org.beangle.commons.text.seq.SeqPattern
import org.beangle.commons.transfer.TransferResult
import org.beangle.commons.transfer.exporter.Context
import org.beangle.commons.transfer.exporter.Exporter
import org.beangle.commons.transfer.io.TransferFormat
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.teach.code.school.CourseHourType
import com.ekingstar.eams.teach.code.school.CourseType
import com.ekingstar.eams.teach.major.helper.MajorPlanSearchHelper
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.exporter.FloatNumFormat
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorCourseGroupBean
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.program.share.SharePlan
import org.openurp.edu.eams.teach.program.share.SharePlanCourse
import org.openurp.edu.eams.teach.program.share.SharePlanCourseGroup
import com.ekingstar.eams.web.action.common.ProjectSupportAction
//remove if not needed


class MajorPlanSearchAction extends ProjectSupportAction {

  protected var majorPlanService: MajorPlanService = _

  protected var majorPlanSearchHelper: MajorPlanSearchHelper = _

  def index(): String = {
    put("ACCEPTED", CommonAuditState.ACCEPTED)
    forward()
  }

  def search(): String = {
    put("plans", entityDao.search(majorPlanSearchHelper.buildPlanQuery()))
    forward()
  }

  def info(): String = {
    val planId = getLongId("plan")
    if (null == planId) {
      return forwardError("error.model.id.needed")
    }
    put("plan", entityDao.get(classOf[MajorPlan], planId))
    put("UNSUBMITTED", CommonAuditState.UNSUBMITTED)
    put("ACCEPTED", CommonAuditState.ACCEPTED)
    put("REJECTED", CommonAuditState.REJECTED)
    put("SUBMITTED", CommonAuditState.SUBMITTED)
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def setMajorPlanService(majorPlanService: MajorPlanService) {
    this.majorPlanService = majorPlanService
  }

  def print(): String = {
    val planId = getLongId("plan")
    if (null == planId) {
      return forwardError("error.model.id.needed")
    }
    put("plan", entityDao.get(classOf[MajorPlan], planId))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def printPlans(): String = {
    val planIds = getLongIds("plan")
    if (ArrayUtils.isEmpty(planIds)) {
      return forwardError("plan war not found")
    }
    put("planList", entityDao.get(classOf[MajorPlan], planIds))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    forward()
  }

  def exportPlanCourses(): String = {
    val fileName = "课程导出结果"
    val template = get("template")
    val context = new Context()
    context.put("format", TransferFormat.Xls)
    context.put("exportFile", fileName)
    context.put("template", template)
    context.put(Context.KEYS, get("keys"))
    context.put(Context.TITLES, get("titles"))
    context.put(Context.EXTRACTOR, getPropertyExtractor)
    val response = ServletActionContext.getResponse
    val exporter = buildExporter(TransferFormat.Xls, context)
    exporter.getWriter.setOutputStream(response.getOutputStream)
    context.put("items", getPlanCourseDatas)
    response.setContentType("application/vnd.ms-excel;charset=GBK")
    response.setHeader("Content-Disposition", "attachment;filename=" + 
      encodeAttachName(ServletActionContext.getRequest, fileName + ".xls"))
    exporter.setContext(context)
    exporter.transfer(new TransferResult())
    null
  }

  protected def getPlanCourseDatas(): Iterable[_] = {
    val planIds = Strings.splitToLong(get("planIds")).asInstanceOf[Array[Long]]
    val query = OqlBuilder.from(classOf[MajorPlan], "plan")
    query.where("plan.id in (:ids)", planIds).join("left", "plan.program.major", "major")
      .join("left", "plan.program.direction", "direction")
      .join("plan.groups", "courseGroup")
      .join("courseGroup.planCourses", "planCourse")
    query.select("plan.program.grade,major.name,direction.name,planCourse.course.code,planCourse.course.name,planCourse.course.credits,planCourse.terms")
      .orderBy(Order.parse("major.name,direction.name"))
    entityDao.search(query)
  }

  def infoData(): String = {
    if (null == get("node") || "root" == get("node")) {
      var orderBy = get("orderBy")
      if (Strings.isEmpty(orderBy)) {
        orderBy = "MajorCourseGroup.courseType.priority"
      }
      val query = OqlBuilder.from(classOf[MajorCourseGroup], "MajorCourseGroup")
        .where(new Condition("MajorCourseGroup.plan.id=:planId", getLong("plan.id")))
        .where(new Condition("MajorCourseGroup.parent is null"))
        .orderBy(orderBy)
      populateConditions(query)
      put("MajorCourseGroups", entityDao.search(query))
      forward("fudan/infoGroupData")
    } else {
      val MajorCourseGroupBean = entityDao.get(classOf[MajorCourseGroupBean], getLong("node")).asInstanceOf[MajorCourseGroupBean]
      put("majorPlanCourses", MajorCourseGroupBean.getPlanCourses)
      forward("fudan/infoCourseData")
    }
  }

  protected def configExportContext(context: Context) {
    val majorPlanId = getLong("majorPlanId")
    val majorPlan = entityDao.get(classOf[MajorPlan], majorPlanId)
    val year = majorPlan.getProgram.getGrade
    val query1 = OqlBuilder.from(classOf[SharePlan], "sharePlan").where(new Condition("sharePlan.program.grade=:year", 
      year))
    val l = entityDao.search(query1)
    val ier = l.iterator()
    val shareCourseTypes = new ArrayList[CourseType]()
    if (ier.hasNext) {
      val sharePlan = ier.next().asInstanceOf[SharePlan]
      val sharePlanCourseGroups = sharePlan.getGroups
      val itera = sharePlanCourseGroups.iterator()
      while (itera.hasNext) {
        val sharePlanCourseGroup = itera.next().asInstanceOf[SharePlanCourseGroup]
        val courseType = sharePlanCourseGroup.getCourseType
        shareCourseTypes.add(courseType)
      }
    }
    val query = OqlBuilder.from(classOf[MajorCourseGroup], "MajorCourseGroup")
      .where(new Condition("MajorCourseGroup.parent is null"))
      .orderBy(Order.parse("MajorCourseGroup.courseType.priority"))
    populateConditions(query)
    if (majorPlanId != null && "" != majorPlanId) {
      query.where(new Condition("MajorCourseGroup.plan.id=:majorPlanId", majorPlanId))
    }
    val MajorCourseGroups = entityDao.search(query)
    val it = MajorCourseGroups.iterator()
    val sg = new MultiLevelSeqGenerator()
    sg.add(new SeqPattern(SeqNumStyle.HANZI, "{1}"))
    sg.add(new SeqPattern(SeqNumStyle.HANZI, "({2})"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}."))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}.{8}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}.{8}.{9}"))
    val indexMap = new HashMap[Long, String]()
    val depthMap = new HashMap[Long, Integer]()
    val planCoursesMap = new HashMap[Long, List[PlanCourse]]()
    val shareCourseGroupList = new ArrayList[MajorCourseGroup]()
    val topMajorCourseGroupList = new ArrayList[MajorCourseGroup]()
    while (it.hasNext) {
      val MajorCourseGroupBean = it.next().asInstanceOf[MajorCourseGroupBean]
      if (shareCourseTypes.contains(MajorCourseGroupBean.getCourseType)) {
        shareCourseGroupList.add(MajorCourseGroupBean)
      } else {
        topMajorCourseGroupList.add(MajorCourseGroupBean)
      }
    }
    for (g <- shareCourseGroupList) {
      indexMap.put(g.id, sg.getSytle(2).next())
      depthMap.put(g.id, new java.lang.Integer(2))
    }
    val majorCourseGroupList = new ArrayList[MajorCourseGroup]()
    for (g <- topMajorCourseGroupList) {
      addGroup(majorCourseGroupList, g, indexMap, depthMap, planCoursesMap, sg, 2)
    }
    val format = FloatNumFormat.getInstance
    format.getNumFormat.setMaximumFractionDigits(1)
    context.put("numFormat", format)
    context.put("indexMap", indexMap)
    context.put("depthMap", depthMap)
    context.put("planCoursesMap", planCoursesMap)
    context.put("majorPlan", majorPlan)
    context.put("shareCourseGroupList", shareCourseGroupList)
    context.put("majorCourseGroupList", majorCourseGroupList)
  }

  protected def addGroup(groups: List[MajorCourseGroup], 
      mpg: MajorCourseGroup, 
      indexMap: Map[Long, String], 
      depthMap: Map[Long, Integer], 
      planCoursesMap: Map[Long, List[PlanCourse]], 
      sg: MultiLevelSeqGenerator, 
      depth: Int) {
    groups.add(mpg)
    indexMap.put(mpg.id, sg.getSytle(depth).next())
    depthMap.put(mpg.id, new java.lang.Integer(depth))
    val courses = mpg.getPlanCourses
    if (mpg.isCompulsory) {
      Collections.sort(courses, new MyCmp())
    } else {
      Collections.sort(courses, new PropertyComparator("course.code"))
    }
    planCoursesMap.put(mpg.id, courses)
    val childrenGroup = mpg.getChildren
    if (mpg.getChildren.size > 0) {
      Collections.sort(childrenGroup, new PropertyComparator("courseType.priority"))
      var iter = mpg.getChildren.iterator()
      while (iter.hasNext) {
        val gg = iter.next().asInstanceOf[MajorCourseGroupBean]
        addGroup(groups, gg, indexMap, depthMap, planCoursesMap, sg, depth + 1)
      }
    }
  }

  private class MyCmp extends Comparator {

    def compare(arg0: AnyRef, arg1: AnyRef): Int = {
      val a = arg0.asInstanceOf[PlanCourse]
      val b = arg1.asInstanceOf[PlanCourse]
      if (a.getCourse.getWeekHour == 0) {
        if (b.getCourse.getWeekHour == 0) {
          if (a.getTerms == b.getTerms) {
            a.getCourse.getCode.compareTo(b.getCourse.getCode)
          } else {
            a.getTerms.compareTo(b.getTerms)
          }
        } else {
          1
        }
      } else if (b.getCourse.getWeekHour == 0) {
        -1
      } else {
        if (a.getTerms == b.getTerms) {
          a.getCourse.getCode.compareTo(b.getCourse.getCode)
        } else {
          a.getTerms.compareTo(b.getTerms)
        }
      }
    }
  }

  def sharePlanCourses(): String = {
    val courseTypeId = getInt("courseTypeId")
    val planId = getLong("planId")
    if (planId == null || courseTypeId == null) {
      return forwardError("缺少参数")
    }
    val majorPlan = entityDao.get(classOf[MajorPlan], planId)
    val builder = OqlBuilder.from(classOf[SharePlanCourse], "sc")
    builder.where("sc.courseGroup.courseType.id = :courseTypeId", courseTypeId)
    builder.where("(sc.courseGroup.plan.invalidOn =null or sc.courseGroup.plan.invalidOn>=:startOn) and sc.courseGroup.plan.effectiveOn<=:endOn", 
      majorPlan.getEffectiveOn, majorPlan.getInvalidOn)
    put("planCourses", entityDao.search(builder))
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("courseType", entityDao.get(classOf[CourseType], courseTypeId))
    forward()
  }

  def setMajorPlanSearchHelper(majorPlanSearchHelper: MajorPlanSearchHelper) {
    this.majorPlanSearchHelper = majorPlanSearchHelper
  }

  def getEntityName(): String = classOf[MajorPlan].getName
}
