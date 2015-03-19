package org.openurp.edu.eams.system.web.action

import java.sql.Date

import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.util.ValidEntityKeyPredicate
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.openurp.base.Department
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.system.notice.NoticeContent
import org.openurp.edu.eams.system.notice.model.ManagerNotice
import org.openurp.edu.eams.system.notice.model.Notice
import org.openurp.edu.eams.system.notice.model.StudentNotice
import org.openurp.edu.eams.system.notice.model.TeacherNotice



class NoticeAction extends NoticeSearchAction {

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
      query = OqlBuilder.from(classOf[StudentNotice], "notice")
    } else {
      throw new RuntimeException("unspported notice kind")
    }
    query.limit(getPageLimit).orderBy(get(Order.ORDER_STR))
    put("kind", kind)
    put("notices", entityDao.search(query))
    forward()
  }

  override def edit(): String = {
    val noticeId = getLongId("notice")
    var kind = get("kind")
    if (null == kind) {
      kind = "manager"
    }
    var notice: Notice = null
    if (!ValidEntityKeyPredicate.Instance.apply(noticeId)) {
      if ("std" == kind) {
        notice = new StudentNotice()
        put("departments", getDeparts)
        put("stdTypes", getStdTypes)
      } else {
        notice = new Notice()
      }
    } else {
      if (kind == "std") {
        notice = entityDao.get(classOf[StudentNotice], noticeId)
        put("departments1", notice.asInstanceOf[StudentNotice].getDeparts)
        put("stdTypes1", notice.asInstanceOf[StudentNotice].stdTypes)
      } else if (kind == "teacher") {
        notice = entityDao.get(classOf[TeacherNotice], noticeId)
      } else if (kind == "manager") {
        notice = entityDao.get(classOf[ManagerNotice], noticeId)
      } else {
        throw new RuntimeException("unspported notice kind")
      }
      if ("std" == kind) {
        put("departments", CollectionUtils.subtract(getDeparts, notice.asInstanceOf[StudentNotice].getDeparts))
        put("stdTypes", CollectionUtils.subtract(getStdTypes, notice.asInstanceOf[StudentNotice].stdTypes))
      }
    }
    put("kind", kind)
    put("notice", notice)
    forward()
  }

  override def save(): String = {
    var kind = get("kind")
    val noticeId = getLongId("notice")
    if (null == kind) {
      kind = "manager"
    }
    var notice: Notice = null
    try {
      if (noticeId != null) {
        if (kind == "std") {
          notice = entityDao.get(classOf[StudentNotice], noticeId)
        } else if (kind == "teacher") {
          notice = entityDao.get(classOf[TeacherNotice], noticeId)
        } else if (kind == "manager") {
          notice = entityDao.get(classOf[ManagerNotice], noticeId)
        } else {
          throw new RuntimeException("unspported notice kind")
        }
        populate(notice, getEntityName, Params.sub(getShortName))
      } else {
        if (kind == "std") {
          notice = new StudentNotice()
        } else if (kind == "teacher") {
          notice = new TeacherNotice()
        } else if (kind == "manager") {
          notice = new ManagerNotice()
        } else {
          throw new RuntimeException("unspported notice kind")
        }
        populate(notice, getEntityName, Params.sub(getShortName))
      }
      notice.setUpdatedAt(new Date(System.currentTimeMillis()))
      notice.setPublisher(getUsername)
      entityDao.saveOrUpdate(notice.getContent)
      entityDao.saveOrUpdate(notice)
      if ("std" == kind) {
        val stdNotice = notice.asInstanceOf[StudentNotice]
        val stdTypeIds = Strings.splitToInt(get("stdTypeIds"))
        val departIds = Strings.splitToInt(get("departIds"))
        val stdTypes = entityDao.get(classOf[StdType], stdTypeIds)
        val departs = entityDao.get(classOf[Department], departIds)
        stdNotice.getDeparts.clear()
        stdNotice.getDeparts.addAll(departs)
        stdNotice.stdTypes.clear()
        stdNotice.stdTypes.addAll(stdTypes)
        entityDao.saveOrUpdate(stdNotice)
      }
      redirect("search", "info.save.success", "&kind=" + kind)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        redirect("search", "info.save.failure", "&kind=" + kind)
      }
    }
  }

  override def remove(): String = {
    val noticeIds = getLongIds("notice")
    val notices = entityDao.get(classOf[Notice], noticeIds)
    try {
      for (notice <- notices) {
        val content = notice.getContent
        entityDao.remove(notice)
        entityDao.remove(content)
      }
      redirect("search", "info.delete.success", "&kind=" + get("kind"))
    } catch {
      case e: Exception => {
        logger.info("remove failure", e)
        redirect("search", "info.delete.failure", "&kind=" + get("kind"))
      }
    }
  }
}
