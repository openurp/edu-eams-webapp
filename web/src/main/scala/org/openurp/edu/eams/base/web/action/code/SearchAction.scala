package org.openurp.edu.eams.base.web.action.code

import org.apache.commons.lang3.ClassUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.pojo.BaseCode
import org.beangle.commons.lang.Strings
import org.beangle.ems.dictionary.model.CodeCategory
import org.beangle.ems.dictionary.model.CodeMeta
import org.beangle.ems.dictionary.service.CodeGenerator
import org.openurp.edu.eams.web.action.BaseAction
import SearchAction._



object SearchAction {

  val EXT = "ext/"
}

class SearchAction extends BaseAction {

  protected var codeGenerator: CodeGenerator = _

  def index(): String = {
    put("coders", entityDao.search(OqlBuilder.hql("from org.beangle.ems.dictionary.model.CodeMeta code order by code.category.id,code.title")))
    put("categories", entityDao.getAll(classOf[CodeCategory]))
    forward()
  }

  def search(): String = {
    val classNames = get("className1")
    val className = Strings.split(classNames, ",")(0)
    try {
      val shortName = Strings.uncapitalize(ClassUtils.getShortClassName(className))
      put("shortName", shortName)
      put("baseCodes", entityDao.search(getQueryBuilder))
      getCodeMeta(className)
      val baseCodeObj = Class.forName(className).newInstance().asInstanceOf[BaseCode[_]]
      if (baseCodeObj.hasExtPros()) {
        return checkMethod(className, shortName)
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        logger.error(e.getMessage)
        val error = "没有找到指定的基础代码模型：" + className
        addError(error)
        put("error", error)
        return forward()
      }
    }
    forward()
  }

  protected def checkMethod(className: String, shortName: String): String = EXT + shortName + "List"

  protected def getQueryBuilder(): OqlBuilder[_] = {
    val classNames = get("className1")
    val className = Strings.split(classNames, ",")(0)
    val query = OqlBuilder.from(Class.forName(className), "baseCode")
    query.where("baseCode.effectiveAt <= current_time() and (baseCode.invalidAt >= current_time() or baseCode.invalidAt is null)")
    populateConditions(query)
    query.limit(getPageLimit)
    var orderByPras = get("orderBy")
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "code"
    }
    query.orderBy(Order.parse(orderByPras))
    query
  }

  protected def getCodeMeta(className: String) {
    val query = OqlBuilder.from(classOf[CodeMeta], "codeMeta")
    populateConditions(query)
    query.where("codeMeta.className = :className", className)
    put("codeMeta", entityDao.search(query).get(0))
  }

  def setCodeGenerator(codeGenerator: CodeGenerator) {
    this.codeGenerator = codeGenerator
  }
}
