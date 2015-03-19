package org.openurp.edu.eams.core.service


import org.openurp.edu.eams.base.BaseInfo



trait BaseInfoService {

  def getBaseInfos(infoClass: Class[_]): List[_]

  def getBaseInfo(clazz: Class[_], id: java.lang.Integer): BaseInfo
}
