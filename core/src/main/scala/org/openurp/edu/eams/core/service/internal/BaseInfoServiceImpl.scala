package org.openurp.edu.eams.core.service.internal

import java.lang.reflect.Method
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.eams.base.BaseInfo
import org.openurp.edu.eams.core.service.BaseInfoService



class BaseInfoServiceImpl extends BaseServiceImpl with BaseInfoService {

  def getBaseInfos(clazz: Class[_]): List[_] = {
    if (classOf[BaseInfo].isAssignableFrom(clazz) || likeBaseInfo(clazz)) {
      val builder = OqlBuilder.from(clazz, "baseInfo").where("baseInfo.effectiveAt <= :now and (baseInfo.invalidAt is null or baseInfo.invalidAt >= :now)", 
        new java.util.Date())
        .orderBy("baseInfo.code")
      builder.cacheable(true)
      entityDao.search(builder)
    } else {
      throw new RuntimeException(clazz.name + " is not a baseInfo ")
    }
  }

  private def likeBaseInfo(clazz: Class[_]): Boolean = {
    val methods = clazz.declaredMethods
    var hasEffectiveAt = false
    var hasInvalidAt = false
    var hasCode = false
    for (method <- methods) {
      if ("getEffectiveAt" == method.name) {
        hasEffectiveAt = true
      } else if ("getInvalidAt" == method.name) {
        hasInvalidAt = true
      } else if ("getCode" == method.name) {
        hasCode = true
      }
    }
    hasEffectiveAt && hasCode && hasInvalidAt
  }

  def getBaseInfo(clazz: Class[_], id: java.lang.Integer): BaseInfo = {
    val query = OqlBuilder.from(clazz, "info")
    query.where("info.id=:infoId", id)
    val rs = entityDao.search(query)
    if (!rs.isEmpty) rs.get(0).asInstanceOf[BaseInfo] else null
  }
}
