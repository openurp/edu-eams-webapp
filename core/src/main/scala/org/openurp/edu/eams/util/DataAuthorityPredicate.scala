package org.openurp.edu.eams.util

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.functor.Predicate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DataAuthorityPredicate(var stdTypeDataRealm: String, var departDataRealm: String,
  var stdTypeAttrName: String = "studentType",
  var departAttrName: String = "department") extends Predicate[Any] {

  protected val logger = LoggerFactory.getLogger(this.getClass)

  override def apply(arg0: Any): Boolean = {
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
}
