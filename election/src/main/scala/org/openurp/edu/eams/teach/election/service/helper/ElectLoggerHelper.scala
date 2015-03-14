package org.openurp.edu.eams.teach.election.service.helper

import org.beangle.security.blueprint.SecurityUtils
import org.beangle.security.core.Authentication
import org.beangle.security.core.context.SecurityContextHolder
import org.beangle.security.web.auth.WebAuthenticationDetails
import org.openurp.edu.eams.teach.election.ElectLogger

import scala.collection.JavaConversions._

object ElectLoggerHelper {

  def getRemoteIp(): String = {
    val auth = SecurityContextHolder.getContext.getAuthentication
    val details = auth.getDetails
    if ((details.isInstanceOf[WebAuthenticationDetails])) {
      val webDetails = details.asInstanceOf[WebAuthenticationDetails]
      return webDetails.getAgent.getIp
    }
    null
  }

  def getOperatorCode(): String = SecurityUtils.getUsername

  def getOperatorName(): String = SecurityUtils.getFullname

  def getOperatorId(): java.lang.Long = SecurityUtils.getUserId

  def setLoggerData(logger: ElectLogger) {
    logger.setOperatorCode(getOperatorCode)
    logger.setOperatorName(getOperatorName)
    logger.setIpAddress(getRemoteIp)
  }
}
