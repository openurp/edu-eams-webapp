package org.openurp.edu.eams.base.web.action.code

import java.io.IOException
import java.lang.reflect.Method

import java.util.Date

import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.commons.lang3.ClassUtils
import org.apache.struts2.ServletActionContext
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.pojo.BaseCode
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.excel.ExcelItemWriter
import org.beangle.commons.transfer.exporter.Context
import org.beangle.commons.transfer.exporter.Exporter
import org.beangle.commons.transfer.exporter.MultiEntityExporter
import org.beangle.commons.transfer.io.TransferFormat
import org.beangle.ems.dictionary.model.CodeMeta
import org.springframework.dao.DataIntegrityViolationException



abstract class AbstractManageAction extends SearchAction {

  protected def checkMethod(className: String, shortName: String): String = {
    val method = this.getClass.getDeclaredMethods
    val forwardMethod = "edit" + ClassUtils.getShortClassName(className)
    for (i <- 0 until method.length if Objects.==(method(i).getName, forwardMethod)) {
      return EXT + shortName + "List"
    }
    logger.info("没有找到指定的跳转页面（" + (EXT + shortName + "List") + ".ftl），将转跳默认页面")
    addError("没有找到指定的跳转页面，将转跳默认页面")
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val classNames = get("className1")
    val className = Strings.split(classNames, ",")(0)
    val query = OqlBuilder.from(Class.forName(className), "baseCode")
    populateConditions(query)
    query.limit(getPageLimit)
    var orderByPras = get("orderBy")
    if (Strings.isEmpty(orderByPras)) {
      orderByPras = "code"
    }
    query.orderBy(Order.parse(orderByPras))
    query
  }

  def edit(): String = {
    try {
      val classNames = get("className1")
      val className = Strings.split(classNames, ",")(0)
      val shortName = Strings.uncapitalize(ClassUtils.getShortClassName(className))
      put("shortName", shortName)
      getCodeMeta(className)
      val baseCodeObj = getEntity(Class.forName(className), "baseCode").asInstanceOf[BaseCode[_]]
      put("baseCode", baseCodeObj)
      if (baseCodeObj.hasExtPros()) {
        val method = this.getClass.getDeclaredMethods
        val forwardMethod = "edit" + ClassUtils.getShortClassName(className)
        for (i <- 0 until method.length if Objects.==(method(i).getName, forwardMethod)) {
          return method(i).invoke(this, null.asInstanceOf[Array[Any]]).asInstanceOf[String]
        }
        logger.error("org.openurp.edu.eams.basecode.web.action.BaseCodeAction.search() : 没有找到指定的跳转页面（" + 
          (EXT + shortName) + 
          "Form.ftl），将转跳默认页面")
        addError("没有找到指定的跳转页面，将转跳默认页面")
        forward()
      } else {
        forward()
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
        throw new RuntimeException(e)
      }
    }
  }

  def getExtEditForward(): String = {
    try {
      val classNames = get("className1")
      val className = Strings.split(classNames, ",")(0)
      val shortName = Strings.uncapitalize(ClassUtils.getShortClassName(className))
      EXT + shortName + "Form"
    } catch {
      case e: Exception => {
        logger.error(e.getMessage)
        throw new RuntimeException(e)
      }
    }
  }

  def save(): String = {
    try {
      val classNames = get("className1")
      val className = Strings.split(classNames, ",")(0)
      val shortName = Strings.uncapitalize(ClassUtils.getShortClassName(className))
      put("shortName", shortName)
      val baseCodeObj = populateEntity(Class.forName(className), shortName).asInstanceOf[BaseCode[_]]
      val query = OqlBuilder.from(Class.forName(className), shortName)
      query.where(shortName + ".code = :code", baseCodeObj.getCode)
      if (null != baseCodeObj.id) {
        query.where(shortName + " != :" + shortName, baseCodeObj)
      }
      if (Collections.isNotEmpty(entityDao.search(query))) {
        getCodeMeta(className)
        put("baseCode", baseCodeObj)
        addError(getText("error.code.existed"))
        search()
        return "edit"
      }
      if (null == baseCodeObj.id) {
        baseCodeObj.setCreatedAt(new Date())
        baseCodeObj.setUpdatedAt(baseCodeObj.getCreatedAt)
      } else {
        baseCodeObj.setUpdatedAt(new Date())
      }
      entityDao.saveOrUpdate(baseCodeObj)
      redirect("search", "info.action.success")
    } catch {
      case e: Exception => {
        logger.error(ExceptionUtils.getStackTrace(e))
        throw new RuntimeException(e)
      }
    }
  }

  def remove(): String = {
    var className: String = null
    try {
      val classNames = get("className1")
      className = Strings.split(classNames, ",")(0)
      entityDao.remove(entityDao.get(Class.forName(className).asInstanceOf[Class[BaseCode[Integer]]], 
        getIntIds("baseCode")))
    } catch {
      case e: ClassNotFoundException => {
        logger.error(e.getMessage)
        throw new RuntimeException(e)
      }
      case e: DataIntegrityViolationException => {
        logger.error(e.getMessage)
        addError(getText("error.remove.beenUsed"))
        search()
        return "search"
      }
    }
    redirect("search", "info.action.success")
  }

  protected override def buildExporter(format: TransferFormat, context: Context): Exporter = {
    val exporter = new MultiEntityExporter()
    exporter.setWriter(new ExcelItemWriter(ServletActionContext.getResponse.getOutputStream))
    exporter
  }

  protected def configExporter(exporter: Exporter, context: Context) {
    try {
      val classNames = get("className1")
      val classNameArray = Strings.split(classNames, ",")
      val keyArray = Strings.split(context.get(Context.KEYS).toString, ",")
      val titleArray = Strings.split(context.get(Context.TITLES).toString, ",")
      val metadatas = Collections.newBuffer[Any]
      context.put("metadatas", metadatas)
      val items = Collections.newBuffer[Any]
      for (i <- 0 until classNameArray.length) {
        val codeMeta = entityDao.get(classOf[CodeMeta], "className", classNameArray(i))
          .get(0)
        val query = OqlBuilder.from(Class.forName(codeMeta.getClassName), "baseCode")
        populateConditions(query)
        val baseCodeIds = Strings.splitToInt(get("baseCodeIds"))
        if (baseCodeIds != null && baseCodeIds.length > 0) {
          query.where("baseCode.id in (:baseCodeIds)", baseCodeIds)
        }
        query.orderBy(Order.parse("baseCode.code"))
        items.add(entityDao.search(query))
        metadatas.add(new MultiEntityExporter.Metadata(codeMeta.getTitle, keyArray, titleArray))
      }
      context.put("items", items)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        throw new RuntimeException(e)
      }
    }
  }
}
