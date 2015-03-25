package org.openurp.edu.eams.teach.program.share.web.action




import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.base.code.nation.Language
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.major.service.MajorCourseGroupService
import org.openurp.edu.eams.teach.program.share.SharePlan
import org.openurp.edu.eams.teach.program.share.SharePlanCourseGroup
import com.ekingstar.eams.web.action.BaseAction
//remove if not needed


class SharePlanCourseGroupAction extends BaseAction {

  var MajorCourseGroupService: MajorCourseGroupService = _

  var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  protected override def getEntityName(): String = classOf[SharePlanCourseGroup].getName

  protected def editSetting(teacher: Entity[_]) {
    put("sharePlanCourseGroup.plan.id", getLong("sharePlanCourseGroup.plan.id"))
    val queryCourseType = OqlBuilder.from(classOf[CourseType], "courseType")
    val query = OqlBuilder.from(classOf[SharePlanCourseGroup], "sharePlanCourseGroup")
    val queryparams = new HashMap[String, Any]()
    queryparams.put("sharePlanCourseGroupPlanId", getLong("sharePlanCourseGroup.plan.id"))
    if (null == getLong("sharePlanCourseGroupId")) {
      queryCourseType.where(new Condition("not exists(from " + classOf[SharePlanCourseGroup].getName + 
        " group " + 
        "where group.courseType=courseType and group.plan.id=:planId)", getLong("sharePlanCourseGroup.plan.id")))
    } else {
      val sharePlanCourseGroup = entityDao.get(classOf[SharePlanCourseGroup], getLong("sharePlanCourseGroupId")).asInstanceOf[SharePlanCourseGroup]
      val params = new HashMap[String, Any]()
      params.put("planId", getLong("sharePlanCourseGroup.plan.id"))
      params.put("ctype", sharePlanCourseGroup.getCourseType.id)
      queryCourseType.where(new Condition("not exists(from " + classOf[SharePlanCourseGroup].getName + 
        " group " + 
        "where group.courseType=courseType and group.plan.id=:planId) or courseType.id=:ctype"))
      queryCourseType.params(params)
      queryparams.put("ctype", sharePlanCourseGroup.getCourseType.id)
    }
    if (null == getLong("sharePlanCourseGroupId")) {
      query.where(new Condition("sharePlanCourseGroup.plan.id=:sharePlanCourseGroupPlanId"))
    } else {
      query.where(new Condition("sharePlanCourseGroup.plan.id=:sharePlanCourseGroupPlanId and sharePlanCourseGroup.courseType.id !=:ctype"))
    }
    query.params(queryparams)
    put("courseTypes", entityDao.search(queryCourseType))
    put("parents", entityDao.search(query))
    addBaseCode("languages", classOf[Language])
  }

  protected def saveAndForward(entity: Entity[_]): String = {
    val group = entity.asInstanceOf[SharePlanCourseGroup]
    val plan = entityDao.get(classOf[SharePlan], group.getPlan.id)
    val oldParent = group.getParent
    val parentId = getLong("newParentId")
    var parent: CourseGroup = null
    var indexno = 0
    if (parentId != null) {
      parent = entityDao.get(classOf[SharePlanCourseGroup], parentId)
      indexno = parent.getChildren.size + 1
    } else {
      indexno = plan.getTopCourseGroups.size + 1
    }
    try {
      if (group.isPersisted) {
        if ((parent != null && oldParent != null && parentId != oldParent.id) || 
          (parent == null && oldParent != null) || 
          (parent != null && oldParent == null)) {
          MajorCourseGroupService.move(group, parent, indexno)
        }
        planCourseGroupCommonDao.saveOrUpdateCourseGroup(group)
      } else {
        group.setIndexno("--")
        planCourseGroupCommonDao.addCourseGroupToPlan(group, parent, plan)
        MajorCourseGroupService.move(group, parent, indexno)
      }
      redirect(new Action(classOf[SharePlanAction], "groupList"), "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForward failure for:" + e.getMessage)
        redirect(new Action(classOf[SharePlanAction], "groupList"), "info.save.failure")
      }
    }
  }

  protected def removeAndForward(entities: Iterable[_]): String = {
    try {
      remove(entities)
    } catch {
      case e: Exception => {
        logger.info("removeAndForward failure for:", e)
        return redirect(new Action(classOf[SharePlanAction], "groupList"), "info.delete.failure")
      }
    }
    redirect(new Action(classOf[SharePlanAction], "groupList"), "info.delete.success")
  }
}
