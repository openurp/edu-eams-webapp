package org.openurp.eams.action

import scala.collection.mutable.ListBuffer

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.Semester
import org.openurp.eams.BonusItem
import org.openurp.teach.code.StdLabel
import org.openurp.teach.core.Course

class BonusItemAction extends RestfulAction[BonusItem] {

  override def editSetting(entity: BonusItem) = {

    val stdLabels = findItems(classOf[StdLabel])
    put("stdLabels", stdLabels)

    val beginTimes = findItems(classOf[Semester])
    put("beginTimes", beginTimes)

    val endTimes = findItems(classOf[Semester])
    put("endTimes", endTimes)

    val courses = findItems(classOf[Course])
    put("courses", courses)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }

  protected override def saveAndRedirect(entity: BonusItem): View = {
    val bonusItem = entity.asInstanceOf[BonusItem]

    bonusItem.courses.clear()
    val courseIds = getAll("coursesId2nd", classOf[Integer])
    bonusItem.courses ++= entityDao.find(classOf[Course], courseIds)
    super.saveAndRedirect(entity)
  }
}