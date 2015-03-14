package org.openurp.edu.eams.web.action.common

import java.util.List
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.openurp.base.Department
import org.openurp.edu.base.Project

import scala.collection.JavaConversions._

abstract class ProjectSupportAction extends RestrictionSupportAction {

  protected def getCollegeOfDeparts(): List[Department] = {
    val departments = CollectUtils.newArrayList()
    val project = getProject
    val departs = CollectUtils.newHashSet(project.departments)
    for (department <- getDeparts if department.isCollege && departs.contains(department)) {
      departments.add(department)
    }
    departments
  }
}
