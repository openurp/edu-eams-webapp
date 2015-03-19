package org.openurp.edu.eams.web.view.component

import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.ClosingUIBean
import com.opensymphony.xwork2.util.ValueStack




class SemesterBar(stack: ValueStack) extends ClosingUIBean(stack) {

  
  var formName: String = _

  
  var action: String = _

  
  var target: String = _

  
  var semesterName: String = _

  
  var name: String = _

  
  var onChange: String = _

  
  var onSemesterChange: String = _

  
  var initCallback: String = _

  
  var label: String = _

  
  var submitValue: String = _

  
  var value: String = _

  
  var divId: String = _

  
  var submit: AnyRef = _

  
  var semesterEmpty: AnyRef = _

  
  var empty: AnyRef = _

  
  var semesterValue: AnyRef = _

  protected override def evaluateParams() {
    if (Strings.isEmpty(this.id)) {
      generateIdIfEmpty()
    }
  }
}
