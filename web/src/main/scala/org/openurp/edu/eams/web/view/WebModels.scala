package org.openurp.edu.eams.web.view

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.struts2.view.tag.AbstractModels
import org.beangle.struts2.view.tag.TagModel
import org.openurp.edu.eams.web.view.component.Menu
import org.openurp.edu.eams.web.view.component.NumRange
import org.openurp.edu.eams.web.view.component.ProjectUI
import org.openurp.edu.eams.web.view.component.SemesterBar
import org.openurp.edu.eams.web.view.component.semester.SemesterCalendar
import com.opensymphony.xwork2.util.ValueStack

import scala.collection.JavaConversions._

class WebModels(stack: ValueStack, req: HttpServletRequest, res: HttpServletResponse)
    extends AbstractModels(stack, req, res) {

  def getProjectUI(): TagModel = get(classOf[ProjectUI])

  def getSemesterBar(): TagModel = get(classOf[SemesterBar])

  def getNumRange(): TagModel = get(classOf[NumRange])

  def getSemesterCalendar(): TagModel = get(classOf[SemesterCalendar])

  def getMenu(): TagModel = get(classOf[Menu])
}
