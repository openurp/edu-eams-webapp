package org.openurp.edu.eams.teach.schedule.web.action

import java.text.SimpleDateFormat

import java.util.Date

import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.schedule.model.CurriculumChangeApplication
import org.openurp.edu.eams.teach.schedule.service.propertyExtractor.CurriculumChangePropertyExtractor
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class CurriculumChangeManageAction extends SemesterSupportAction {

  def search(): String = {
    put("changes", entityDao.search(buildChangeQuery()))
    forward()
  }

  def buildChangeQuery(): OqlBuilder[CurriculumChangeApplication] = {
    val entityQuery = OqlBuilder.from(classOf[CurriculumChangeApplication], "change")
    populateConditions(entityQuery)
    if (getLong("change.lesson.teachDepart.id") == null) {
      entityQuery.where("change.teacher.department in (:depts)", getColleges)
    }
    val passed = getBoolean("passed")
    if (null == passed) {
      entityQuery.where("change.passed is null")
    } else {
      if (true == passed) {
        entityQuery.where("change.passed is true")
      } else {
        entityQuery.where("change.passed is false")
      }
    }
    entityQuery.limit(getPageLimit)
    entityQuery.orderBy(Order.parse(get("orderBy")))
    entityQuery
  }

  protected def getExportDatas(): Iterable[_] = entityDao.search(buildChangeQuery())

  def updateState(): String = {
    val passed = getBool("status")
    val all = getBool("all")
    val changeIds = getLongIds("change")
    var applications = Collections.newBuffer[Any]
    applications = if (all) entityDao.search(buildChangeQuery()) else entityDao.get(classOf[CurriculumChangeApplication], 
      changeIds)
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val user = entityDao.get(classOf[User], getUserId)
    val date = new Date()
    for (alterRequest <- applications) {
      var remark = alterRequest.getRemark
      if (Strings.isNotEmpty(remark)) {
        remark += "<br/>" + user.getName + "于" + sdf.format(date) + (if (passed) "审核通过" else "审核不通过")
      } else {
        remark = user.getName + "于" + sdf.format(date) + (if (passed) "审核通过" else "审核不通过")
      }
      alterRequest.setPassed(passed)
      alterRequest.setRemark(remark)
    }
    try {
      entityDao.saveOrUpdate(applications)
    } catch {
      case e: Exception => return redirect("search", "info.action.failure")
    }
    redirect("search", "操作成功," + applications.size + "条被更新", get("params"))
  }

  override def info(): String = {
    val changeId = getLongId("change")
    if (null == changeId) {
      return forwardError("调课记录没有找到")
    }
    put("alteration", entityDao.get(classOf[CurriculumChangeApplication], changeId))
    forward()
  }

  protected def getPropertyExtractor(): PropertyExtractor = {
    new CurriculumChangePropertyExtractor(getTextResource)
  }
}
