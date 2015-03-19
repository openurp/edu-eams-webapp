package org.openurp.edu.eams.system.message.web.action

import java.util.Date
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.security.blueprint.Role
import org.openurp.edu.eams.system.msg.SystemMessageConfig
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class SystemMessageConfigAction extends ProjectSupportAction {

  protected override def getEntityName(): String = classOf[SystemMessageConfig].getName

  protected def indexSetting() {
    put("categories", entityDao.getAll(classOf[Role]))
    put("project", getProject)
  }

  protected def getQueryBuilder(): OqlBuilder[_] = {
    val builder = super.getQueryBuilder.asInstanceOf[OqlBuilder[_]]
    val categoryId = getInt("systemMessageConfigCategoryId")
    val beginAt = getDateTime("beginAt")
    val endAt = getDateTime("endAt")
    if (beginAt != null) {
      builder.where(getShortName + ".beginAt <= :beginAt", beginAt)
    }
    if (endAt != null) {
      builder.where(getShortName + ".endAt >= :endAt", endAt)
    }
    if (null != categoryId) {
      builder.join(getShortName + ".categories", "userCategory")
      builder.where("userCategory.id=:categoryId", categoryId)
    }
    builder.where(getShortName + ".project=:project", getProject)
    builder
  }

  def edit(): String = {
    val entityId = getLongId(getShortName)
    var entity: Entity[_] = null
    entity = if (null == entityId) populateEntity() else getModel(getEntityName, entityId)
    if (null == entity) {
      return forwardError("系统消息设置不存在或已被删除")
    }
    put(getShortName, entity)
    put("categories", entityDao.getAll(classOf[Role]))
    put("now", new Date())
    editSetting(entity)
    forward()
  }

  protected def saveAndForward(entity: Entity[_]): String = {
    val config = entity.asInstanceOf[SystemMessageConfig]
    if (null == config) {
      return forwardError("系统消息设置不存在或已被删除")
    }
    config.getCategories.clear()
    config.getCategories.addAll(entityDao.get(classOf[Role], getAll("categoryId", classOf[Integer])))
    config.setProject(getProject)
    config.setUpdatedAt(new Date())
    if (config.isTransient) {
      config.setCreatedAt(config.getUpdatedAt)
    }
    try {
      entityDao.saveOrUpdate(config)
      return redirect("search", "info.save.success")
    } catch {
      case e: Exception => 
    }
    redirect("search", "info.save.failure")
  }
}
