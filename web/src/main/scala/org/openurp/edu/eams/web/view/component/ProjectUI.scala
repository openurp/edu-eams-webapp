package org.openurp.edu.eams.web.view.component

import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.Select
import com.opensymphony.xwork2.util.ValueStack




class ProjectUI(stack: ValueStack) extends Select(stack) {

  
  var semesterName: String = _

  
  var onChange: String = _

  
  var initCallback: String = _

  
  var onSemesterChange: String = _

  
  var semesterEmpty: AnyRef = _

  
  var semesterValue: AnyRef = _

  if (Strings.isBlank(onChange)) {
    onChange = null
  }

  if (Strings.isBlank(onSemesterChange)) {
    onSemesterChange = null
  }

  if (Strings.isBlank(initCallback)) {
    initCallback = null
  }

  protected override def evaluateParams() {
    super.evaluateParams()
    if (null == semesterName) {
      semesterName = "semester"
    }
  }
}
