package org.openurp.eams.home

import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.commons.web.util.CookieUtils
import org.springframework.util.StringUtils

object EnvUtils {

  val SEMESTER_ID = "semesterId"
  val PROJECT_ID = "projectId"

  def projectId = get(PROJECT_ID)

  def semesterId = get(SEMESTER_ID)

  private def get(name: String): Long = {
    val request = ContextHolder.context.request
    val value = CookieUtils.getCookieValue(request, PROJECT_ID)
    if (!StringUtils.isEmpty(value)) value.toLong else 0
  }
}