package org.openurp.edu.eams.web.view

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.struts2.view.tag.AbstractTagLibrary
import com.opensymphony.xwork2.util.ValueStack

import scala.collection.JavaConversions._

class WebTagLibrary extends AbstractTagLibrary {

  def getFreemarkerModels(stack: ValueStack, req: HttpServletRequest, res: HttpServletResponse): AnyRef = {
    new WebModels(stack, req, res)
  }
}
