package org.openurp.edu.eams.system.web.action

import java.io.Serializable
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.util.ValidEntityKeyPredicate
import org.openurp.edu.base.Student
import org.openurp.edu.eams.system.notice.model.ManagerNotice
import org.openurp.edu.eams.system.notice.model.Notice
import org.openurp.edu.eams.system.notice.model.StudentNotice
import org.openurp.edu.eams.system.notice.model.TeacherNotice
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class NoticeSearchAction extends SemesterSupportAction {

  protected override def getEntityName(): String = classOf[Notice].getName

  protected override def indexSetting() {
    val menuProfileId = getUserCategoryId
    if (menuProfileId == EamsUserCategory.MANAGER_USER) {
      put("kind", "manager")
    } else if (menuProfileId == EamsUserCategory.STD_USER) {
      put("kind", "std")
    } else if (menuProfileId == EamsUserCategory.TEACHER_USER) {
      put("kind", "teacher")
    } else {
      throw new RuntimeException("unspported category")
    }
  }

  override def search(): String = {
    var kind = get("kind")
    if (null == kind) {
      kind = "manager"
    }
    var query: OqlBuilder[_ <: Notice] = null
    if ("manager" == kind) {
      query = OqlBuilder.from(classOf[ManagerNotice], "notice")
    } else if ("teacher" == kind) {
      query = OqlBuilder.from(classOf[TeacherNotice], "notice")
    } else if ("std" == kind) {
      val std = getLoginStudent
      query = OqlBuilder.from(classOf[StudentNotice], "notice")
      query.join("notice.stdTypes", "stdType")
      query.join("notice.departs", "department")
      query.where("stdType in (:stdType)", std.getType)
      query.where("department in (:department)", std.department)
    } else {
      throw new RuntimeException("unspported notice kind")
    }
    query.limit(getPageLimit).orderBy(get(Order.ORDER_STR))
    put("kind", kind)
    put("notices", entityDao.search(query))
    forward()
  }

  override def info(): String = {
    val noticeId = getLongId("notice")
    if (!ValidEntityKeyPredicate.Instance.apply(noticeId)) {
      return forward("errors", "error.parameters.illegal")
    } else {
      val menuProfileId = getUserCategoryId
      var resource: Class[_ <: Entity[Long]] = null
      if (EamsUserCategory.STD_USER == menuProfileId) {
        resource = classOf[StudentNotice]
      } else if (EamsUserCategory.TEACHER_USER == menuProfileId) {
        resource = classOf[TeacherNotice]
      } else if (EamsUserCategory.MANAGER_USER == menuProfileId) {
        val kind = get("kind")
        resource = if ("std" == kind) classOf[StudentNotice] else if ("teacher" == kind) classOf[TeacherNotice] else classOf[ManagerNotice]
      } else {
        throw new RuntimeException("error.parameters.illegal")
      }
      put("notice", entityDao.get(resource, noticeId))
    }
    forward()
  }
}
