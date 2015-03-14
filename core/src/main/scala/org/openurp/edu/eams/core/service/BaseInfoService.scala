package org.openurp.edu.eams.core.service

import java.util.List
import org.openurp.edu.eams.base.BaseInfo

import scala.collection.JavaConversions._

trait BaseInfoService {

  def getBaseInfos(infoClass: Class[_]): List[_]

  def getBaseInfo(clazz: Class[_], id: java.lang.Integer): BaseInfo
}
