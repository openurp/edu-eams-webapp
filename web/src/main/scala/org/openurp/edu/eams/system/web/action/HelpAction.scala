package org.openurp.edu.eams.system.web.action

import java.util.Locale
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.web.action.BaseAction
import HelpAction._



object HelpAction {

  protected val helpPath = "/WEB-INF/help/"

  protected val urlPostfix = ".html"

  protected val cn = "zh_CN"

  protected val en = "en_US"
}

class HelpAction extends BaseAction {

  def help(): String = {
    val module = get("helpId")
    if (Strings.isEmpty(module)) {
      return forward("adminHome")
    }
    val buf = new StringBuffer()
    buf.append(helpPath)
    val locale = getLocale.asInstanceOf[Locale]
    var localName = locale.getLanguage
    if (null == localName) {
      localName = "zh_CN"
    }
    if (localName.indexOf("zh") != -1) {
      buf.append(cn)
    } else {
      buf.append(en)
    }
    buf.append("/")
    if (module.indexOf("/") == -1) {
      buf.append(module.replace('.', '/'))
      buf.append("/index")
    } else {
      val controller = module.substring(0, module.indexOf("/"))
      buf.append(controller.replace('.', '/'))
      buf.append(module.substring(module.indexOf("/")))
    }
    buf.append(urlPostfix)
    logger.debug("HELP system: " + buf.toString)
    forward("adminHome")
  }

  def stdHome(): String = forward()

  def teacherHome(): String = forward()
}
