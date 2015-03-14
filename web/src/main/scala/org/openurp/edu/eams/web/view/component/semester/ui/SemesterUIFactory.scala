package org.openurp.edu.eams.web.view.component.semester.ui

import java.util.HashMap
import java.util.Map

import scala.collection.JavaConversions._

object SemesterUIFactory {

  private val uiList = new HashMap[String, SemesterCalendarUI]()

  def register(name: String, instance: SemesterCalendarUI) {
    if (null != name) {
      name = name.toUpperCase()
    }
    uiList.put(name, instance)
  }

  def get(name: String): SemesterCalendarUI = {
    if (null != name) {
      name = name.toUpperCase()
    }
    val result = uiList.get(name)
    if (null == result) {
      throw new NullPointerException("SemesterCalendar UI Type = '" + name + "' is undefined ")
    }
    result
  }
}
