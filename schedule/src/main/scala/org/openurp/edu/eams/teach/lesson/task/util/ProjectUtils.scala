package org.openurp.edu.eams.teach.lesson.task.util

import java.util.ArrayList
import java.util.List
import org.openurp.base.Department
import org.openurp.edu.base.Project

import scala.collection.JavaConversions._

object ProjectUtils {

  def getColleges(project: Project): List[Department] = {
    val res = new ArrayList[Department]()
    for (depart <- project.departments if depart.isCollege) {
      res.add(depart)
    }
    res
  }

  def getTeachDeparts(project: Project): List[Department] = {
    val res = new ArrayList[Department]()
    for (depart <- project.departments if depart.isTeaching) {
      res.add(depart)
    }
    res
  }
}
