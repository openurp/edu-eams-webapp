package org.openurp.edu.eams.teach.lesson.task.util



import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.beangle.commons.collection.Collections



object ProjectUtils {

  def getColleges(project: Project): Seq[Department] = {
    val res = Collections.newBuffer[Department]
    for (depart <- project.departments if depart.college) {
      res += depart
    }
    res
  }

  def getTeachDeparts(project: Project): Seq[Department] = {
    val res = Collections.newBuffer[Department]
    for (depart <- project.departments if depart.teaching) {
      res += depart
    }
    res
  }
}
