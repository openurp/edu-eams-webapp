package org.openurp.edu.eams.web.action.common



import org.beangle.commons.collection.CollectUtils
import org.openurp.base.Department
import org.openurp.edu.base.Project



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
