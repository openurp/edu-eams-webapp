package org.openurp.edu.eams.teach.program.share.web.action


import java.util.Date



import org.apache.struts2.ServletActionContext
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.seq.MultiLevelSeqGenerator
import org.beangle.commons.text.seq.SeqNumStyle
import org.beangle.commons.text.seq.SeqPattern
import org.beangle.commons.transfer.exporter.Context
import org.beangle.commons.transfer.exporter.Exporter
import com.ekingstar.eams.system.doc.service.DocPath
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.exporter.FloatNumFormat
import org.openurp.edu.eams.teach.program.share.SharePlan
import org.openurp.edu.eams.teach.program.share.SharePlanCourseGroup
import org.openurp.edu.eams.teach.program.share.model.SharePlanBean
import com.ekingstar.eams.web.action.common.ProjectSupportAction
//remove if not needed


class SharePlanAction extends ProjectSupportAction {

  protected override def getEntityName(): String = classOf[SharePlan].getName

  protected def indexSetting() {
    put("educations", getEducations)
  }

  override def search(): String = {
    val builder = getQueryBuilder.asInstanceOf[OqlBuilder[_]]
    builder.where("project =:project", getProject)
    put(getShortName + "s", search(builder))
    forward()
  }

  protected def editSetting(teacher: Entity[_]) {
    put("educations", getEducations)
  }

  protected def saveAndForward(entity: Entity[_]): String = {
    val plan = entity.asInstanceOf[SharePlanBean]
    if (null == plan.getProject) plan.setProject(getProject)
    val params = new HashMap[String, Any]()
    params.put("name", plan.getName)
    params.put("education", plan.getEducation)
    if (!entityDao.duplicate(classOf[SharePlan].getName, plan.id, params)) {
      return redirect("edit", "error.model.existed", "sharePlan.id=" + plan.id)
    }
    if (plan.isTransient) {
      plan.setCreatedAt(new Date())
    }
    plan.setUpdatedAt(new Date())
    try {
      entityDao.saveOrUpdate(plan)
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForward failure for:" + e.getMessage)
        redirect("search", "info.save.failure")
      }
    }
  }

  def copy(): String = {
    val planId = getLong("planId")
    val plan = entityDao.get(classOf[SharePlan], planId)
    val params = new HashMap[String, Any]()
    params.put("name", get("grade"))
    params.put("education", plan.getEducation)
    if (!entityDao.duplicate(classOf[SharePlanBean].getName, null, params)) {
      return redirect("search", "error.model.existed")
    }
    val planClone = plan.clone().asInstanceOf[SharePlan]
    planClone.setName(get("grade"))
    entityDao.saveOrUpdate(planClone)
    redirect("search", "info.save.success")
  }

  def infoData(): String = {
    if (null == get("node") || "root" == get("node")) {
      val orderBy = get("orderBy")
      val query = OqlBuilder.from(classOf[SharePlanCourseGroup], "sharePlanCourseGroup")
        .where(new Condition("sharePlanCourseGroup.plan.id=:planId", getLong("sharePlan.id")))
        .where(new Condition("sharePlanCourseGroup.parent is null"))
        .orderBy(orderBy)
      populateConditions(query)
      put("sharePlanCourseGroups", entityDao.search(query))
      forward("infoGroupData")
    } else {
      val sharePlanCourseGroup = entityDao.get(classOf[SharePlanCourseGroup], getLong("node")).asInstanceOf[SharePlanCourseGroup]
      put("sharePlanCourses", sharePlanCourseGroup.getPlanCourses)
      forward("infoCourseData")
    }
  }

  def groupList(): String = {
    var planId = getLong("sharePlan.id")
    if (null == planId) {
      planId = getLong("planId")
    }
    val plan = entityDao.get(classOf[SharePlan], planId).asInstanceOf[SharePlan]
    put("plan", plan)
    forward()
  }

  def groupData(): String = {
    var planId = getLong("sharePlan.id")
    if (null == planId) {
      planId = getLong("planId")
    }
    val orderBy = get("orderBy")
    val query = OqlBuilder.from(classOf[SharePlanCourseGroup], "sharePlanCourseGroup")
      .where(new Condition("sharePlanCourseGroup.plan.id=:planId", planId))
      .where(new Condition("sharePlanCourseGroup.parent is null"))
      .orderBy(Order.parse(orderBy))
    put("sharePlanCourseGroups", entityDao.search(query))
    forward()
  }

  protected override def configExporter(exporter: Exporter, context: Context) {
    val query = OqlBuilder.from(classOf[SharePlanCourseGroup], "sharePlanCourseGroup")
      .where(new Condition("sharePlanCourseGroup.parent is null"))
      .orderBy(Order.parse(get("orderBy")))
    populateConditions(query)
    val sharePlanCourseGroups = entityDao.search(query)
    val sg = new MultiLevelSeqGenerator()
    sg.add(new SeqPattern(SeqNumStyle.HANZI, "{1}"))
    sg.add(new SeqPattern(SeqNumStyle.HANZI, "({2})"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}.{8}"))
    sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}.{8}.{9}"))
    val indexList = new ArrayList[String]()
    val xlsTable = new ArrayList[SharePlanCourseGroup]()
    getXlsTree(sharePlanCourseGroups, sg, xlsTable, indexList, 1)
    val format = FloatNumFormat.getInstance
    format.getNumFormat.setMaximumFractionDigits(1)
    context.put("xlsTitle", sharePlanCourseGroups.get(0).getPlan.asInstanceOf[SharePlan]
      .getName + 
      sharePlanCourseGroups.get(0).getPlan.asInstanceOf[SharePlan]
      .getEducation
      .getName + 
      "公共培养方案")
    context.put("numFormat", format)
    context.put("xlsTable", xlsTable)
    context.put("indexList", indexList)
  }

  private def getXlsTree(list: List[SharePlanCourseGroup], 
      sg: MultiLevelSeqGenerator, 
      xlsTable: List[SharePlanCourseGroup], 
      indexList: List[String], 
      level: Int) {
    for (i <- 0 until list.size) {
      val sharePlanCourseGroup = list.get(i)
      if (null != sharePlanCourseGroup.getRemark && sharePlanCourseGroup.getRemark.trim() == "") {
        sharePlanCourseGroup.setRemark(null)
      }
      if (sharePlanCourseGroup.getPlanCourses.size == 0) {
        sharePlanCourseGroup.setPlanCourses(new ArrayList[PlanCourse]())
      }
      indexList.add(sg.next(level))
      xlsTable.add(sharePlanCourseGroup)
      val children = list.get(i).getChildren
      if (children.size != 0) {
        sg.reset(level + 1)
        getXlsTree(children, sg, xlsTable, indexList, level + 1)
      }
    }
  }

  protected def resolveTemplatePath(template: String): String = {
    if (Strings.isNotEmpty(template)) {
      val defaultPath = ServletActionContext.getServletContext.getRealPath(DocPath.fileDirectory)
      val filePath = DocPath.getRealPath(getConfig, DocPath.TEMPLATE_DOWNLOAD, defaultPath)
      template = filePath + template
    }
    template
  }
}
