package org.openurp.edu.eams.base.web.action


import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper



abstract class BaseInfoAction extends BaseInfoSearchAction {

  def getEntityName(): String

  var baseInfoSearchHelper: BaseInfoSearchHelper = _

  var codeGenerator: CodeGenerator = _

  def info(): String = {
    val entity = getOpEntity
    if (null == entity) {
      return forwardError(Array("entity.course", "error.model.id.needed"))
    }
    put(getShortName, entity)
    forward()
  }

  protected def getOpEntity(): Entity = {
    val id = getLong("id")
    if (null == id) {
      return null
    }
    val className = getEntityName
    entityDao.get(Class.forName(className).asInstanceOf[Class[Entity[Long]]], id)
  }

  def remove(): String = {
    val entity = getOpEntity
    try {
      entityDao.remove(Collections.singleton(entity))
    } catch {
      case e: Exception => return redirect("search", "info.delete.failure")
    }
    redirect("search", "info.delete.success")
  }

  def saveOrUpdate(baseInfo: BaseInfo): String = {
    if (!codeGenerator.isValidCode(baseInfo.getCode)) {
      val code = codeGenerator.gen(new CodeFixture(baseInfo))
      if (codeGenerator.isValidCode(code)) {
        baseInfo.setCode(code)
      } else {
        addMessage(getText("system.codeGen.failure"))
        return forward(new Action(this.getClass, "edit"))
      }
    }
    baseInfo.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()))
    if (!baseInfo.isPersisted) {
      baseInfo.setCreatedAt(new java.sql.Date(System.currentTimeMillis()))
    }
    onSave(baseInfo)
    null
  }

  def batchUpdateState(): String = {
    val ids = Strings.transformToLong(Strings.split(get("ids")))
    val status = getBoolean("status")
    val infos = entityDao.get(getEntityClazz, "id", ids).asInstanceOf[List[_]]
    var it = infos.iterator()
    while (it.hasNext) {
      val info = it.next().asInstanceOf[BaseInfo]
    }
    entityDao.saveOrUpdate(infos)
    redirect("search", "info.action.success")
  }

  protected def getEntityClazz(): Class[_] = classOf[BaseInfo]

  protected def onSave(entity: Entity) {
    entityDao.saveOrUpdate(entity)
  }
}
