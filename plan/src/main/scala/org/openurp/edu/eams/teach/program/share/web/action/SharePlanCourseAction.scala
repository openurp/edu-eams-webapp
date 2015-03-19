package org.openurp.edu.eams.teach.program.share.web.action






import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.teach.Course
import org.openurp.edu.eams.teach.program.share.SharePlanCourse
import org.openurp.edu.eams.teach.program.share.SharePlanCourseGroup
import org.openurp.edu.eams.teach.program.share.model.SharePlanCourseBean
import com.ekingstar.eams.web.action.BaseAction
//remove if not needed


class SharePlanCourseAction extends BaseAction {

  protected override def getEntityName(): String = classOf[SharePlanCourse].getName

  def search(): String = {
    var groupId: java.lang.Long = null
    groupId = if (null != getLong("sharePlanCourse.courseGroup.id")) getLong("sharePlanCourse.courseGroup.id") else getLong("courseGroupId")
    val group = entityDao.get(classOf[SharePlanCourseGroup], groupId).asInstanceOf[SharePlanCourseGroup]
    put("group", group)
    put("plan", group.getPlan)
    val query = OqlBuilder.from(classOf[SharePlanCourse], "sharePlanCourse")
      .where(new Condition("sharePlanCourse.courseGroup.id=:groupId", groupId))
      .orderBy(get(Order.ORDER_STR))
      .limit(getPageLimit)
    put("sharePlanCourses", entityDao.search(query))
    forward()
  }

  protected def editSetting(teacher: Entity[_]) {
    put("sharePlanCourse.courseGroup.id", getLong("sharePlanCourse.courseGroup.id"))
  }

  protected def saveAndForward(entity: Entity[_]): String = {
    val planCourse = entity.asInstanceOf[SharePlanCourse]
    try {
      if (planCourse.isTransient) {
        val courseCode = get("sharePlanCourse.course.code")
        if (Strings.isNotEmpty(courseCode)) {
          val iterator = entityDao.get(classOf[Course], "code", courseCode).iterator()
          if (iterator.hasNext) {
            planCourse.setDepartment(iterator.next().getDepartment)
            entityDao.saveOrUpdate(planCourse)
          }
        }
      } else {
        entityDao.saveOrUpdate(planCourse)
      }
      redirect("search", "info.save.success", "&sharePlanCourse.courseGroup.id=" + planCourse.getCourseGroup.id)
    } catch {
      case e: Exception => {
        logger.info("saveAndForward failure for:" + e.getMessage)
        redirect("search", "info.save.failure", "&sharePlanCourse.courseGroup.id=" + planCourse.getCourseGroup.id)
      }
    }
  }

  def batchAdd(): String = {
    val queryDepartment = OqlBuilder.from(classOf[Department], "department").where(new Condition("department.parent is null"))
      .where(new Condition("department.college=true"))
    put("departments", entityDao.search(queryDepartment))
    val query = OqlBuilder.from(classOf[Course], "course").where("course.enabled=true")
      .orderBy(get(Order.ORDER_STR))
      .limit(getPageLimit)
    populateConditions(query)
    query.where("not exists(from " + classOf[SharePlanCourse].getName + 
      " planCourse where planCourse.course=course and planCourse.courseGroup.id=:courseGroupId)", getLong("sharePlanCourse.courseGroup.id"))
    put("courses", entityDao.search(query))
    forward()
  }

  def batchSave(): String = {
    val selectCourseIds = get("selectCourseIds")
    val courseGroupId = getLong("sharePlanCourse.courseGroup.id")
    val group = entityDao.get(classOf[SharePlanCourseGroup], courseGroupId)
    val selectCourses = entityDao.get(classOf[Course], Strings.splitToLong(selectCourseIds))
    val planCourses = new ArrayList[SharePlanCourse]()
    for (i <- 0 until selectCourses.size) {
      val course = selectCourses.get(i).asInstanceOf[Course]
      val planCourse = new org.openurp.edu.eams.teach.program.share.model.SharePlanCourseBean()
      planCourse.setCourse(course)
      if (null == get("courseTerms" + course.id) || "" == get("courseTerms" + course.id)) {
        planCourse.setTerms("春")
      } else planCourse.setTerms(get("courseTerms" + course.id))
      planCourse.setDepartment(course.getDepartment)
      planCourse.setCourseGroup(group)
      val params = new HashMap[String, Any]()
      params.put("courseGroup", group)
      params.put("course", course)
      if (null != course.getDepartment && 
        entityDao.duplicate(classOf[SharePlanCourseBean].getName, planCourse.id, params)) {
        planCourses.add(planCourse)
      }
    }
    entityDao.saveOrUpdate(planCourses)
    redirect("search", "info.save.success")
  }

  def batchEdit(): String = {
    val selectPlanCourseIds = get("selectPlanCourseIds")
    val selectPlanCourses = entityDao.get(classOf[SharePlanCourse], Strings.splitToLong(selectPlanCourseIds))
    put("sharePlanCourses", selectPlanCourses)
    forward()
  }

  def batchEditSave(): String = {
    val size = get("sharePlanCourseSize")
    val sharePlanCourseSize = new java.lang.Integer(size).intValue()
    val sharePlanCourses = new ArrayList[SharePlanCourse]()
    for (i <- 0 until sharePlanCourseSize) {
      val sharePlanCourse = populateEntity(classOf[SharePlanCourse], "sharePlanCourse" + i).asInstanceOf[SharePlanCourse]
      if (Strings.isBlank(sharePlanCourse.getTerms)) {
        sharePlanCourse.setTerms("")
      }
      sharePlanCourses.add(sharePlanCourse)
    }
    entityDao.saveOrUpdate(sharePlanCourses)
    redirect("search", "info.save.success")
  }
}
