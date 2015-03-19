package org.openurp.edu.eams.util

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.functor.Predicate
import org.slf4j.Logger
import org.slf4j.LoggerFactory



class DataAuthorityPredicate(stdTypeIdSeq: String, departIdSeq: String) extends Predicate[Any] {

  protected val logger = LoggerFactory.getLogger(this.getClass)

  protected var stdTypeDataRealm: String = stdTypeIdSeq

  protected var departDataRealm: String = departIdSeq

  protected var stdTypeAttrName: String = "studentType"

  protected var departAttrName: String = "department"

  def this() {
    this()
  }

  def this(stdTypeIdSeq: String, 
      departIdSeq: String, 
      studentTypeName: String, 
      departAttrName: String) {
    this()
    this.stdTypeDataRealm = stdTypeIdSeq
    this.departDataRealm = departIdSeq
    this.stdTypeAttrName = studentTypeName
    this.departAttrName = departAttrName
  }

  def apply(arg0: AnyRef): java.lang.Boolean = {
    try {
      if (null == arg0) return true
      if (Strings.isNotEmpty(stdTypeDataRealm)) {
        val stdTypeId = PropertyUtils.getProperty(arg0, stdTypeAttrName + ".id").asInstanceOf[java.lang.Long]
        if ((null != stdTypeId) && 
          !Strings.contains(stdTypeDataRealm, stdTypeId.toString)) return false
      }
      if (Strings.isNotEmpty(departDataRealm)) {
        val departId = PropertyUtils.getProperty(arg0, departAttrName + ".id").asInstanceOf[java.lang.Long]
        if ((null != departId) && 
          !Strings.contains(departDataRealm, departId.toString)) return false
      }
      true
    } catch {
      case e: Exception => {
        logger.info("exception occurred in judge dataAuthorty of " + arg0.getClass.getName, e)
        false
      }
    }
  }

  def getDepartAttrName(): String = departAttrName

  def setDepartAttrName(departAttrName: String) {
    this.departAttrName = departAttrName
  }

  def getDepartDataRealm(): String = departDataRealm

  def setDepartDataRealm(departDataRealm: String) {
    this.departDataRealm = departDataRealm
  }

  def getStdTypeAttrName(): String = stdTypeAttrName

  def setStdTypeAttrName(stdTypeAttrName: String) {
    this.stdTypeAttrName = stdTypeAttrName
  }

  def getStdTypeDataRealm(): String = stdTypeDataRealm

  def setStdTypeDataRealm(stdTypeDataRealm: String) {
    this.stdTypeDataRealm = stdTypeDataRealm
  }
}
