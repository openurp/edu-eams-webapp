package org.openurp.edu.eams.web.view.component.semester

import org.openurp.base.Semester
import com.opensymphony.xwork2.util.ValueStack



class SemesterCalendar(stack: ValueStack, generateId: Boolean) extends AbstractSemesterCalendarUI(stack) {

  if (!generateId) {
    this.id = "skipGenerateId"
  }

  def this(stack: ValueStack) {
    this(stack, true)
  }
}
