package org.openurp.edu.eams.web.action.common

import java.util.List
import java.util.Set
import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.eams.base.School
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.system.security.DataRealm
import org.openurp.edu.eams.util.DataRealmLimit
import org.openurp.edu.eams.web.action.BaseAction
import org.openurp.edu.eams.web.helper.RestrictionHelper
import RestrictionSupportAction._

import scala.collection.JavaConversions._

object RestrictionSupportAction {

  var hasStdType: Int = 1

  var hasDepart: Int = 2

  var hasCollege: Int = 4

  var hasAdminDepart: Int = 8

  var hasStdTypeDepart: Int = 3

  var hasStdTypeCollege: Int = 5

  var hasStdTypeAdminDepart: Int = 9

  var hasStdTypeTeachDepart: Int = 11
}

abstract class RestrictionSupportAction extends BaseAction {

  protected var restrictionHelper: RestrictionHelper = _

  protected def getProject(): Project = {
    var project: Project = null
    val projectId = getRequest.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
    if (null == projectId) {
      throw new RuntimeException("project not selected")
    } else {
      project = entityDao.get(classOf[Project], projectId)
    }
    project
  }

  protected def getSchool(): School = getProject.getSchool

  def index(): String = {
    restrictionHelper.setDataRealm(hasStdTypeCollege)
    indexSetting()
    forward()
  }

  def setDataRealm(realmScope: Int) {
    restrictionHelper.setDataRealm(realmScope)
  }

  protected def getEducations(): List[Education] = {
    val project = getProject
    if (null != project) {
      val data = CollectUtils.newHashSet(Strings.split(restrictionHelper.educationIdSeq))
      if (data.contains("*")) return project.educations
      val rs = CollectUtils.newArrayList()
      for (d <- project.educations if data.contains(d.getId.toString)) rs.add(d)
      rs
    } else {
      restrictionHelper.educations.asInstanceOf[List[Education]]
    }
  }

  protected def getStdTypes(): List[StdType] = {
    val project = getProject
    if (null != getProject) {
      val data = CollectUtils.newHashSet(Strings.split(restrictionHelper.stdTypeIdSeq))
      if (data.contains("*")) return project.getTypes
      val rs = CollectUtils.newArrayList()
      for (d <- project.getTypes if data.contains(d.getId.toString)) rs.add(d)
      rs
    } else {
      restrictionHelper.stdTypes.asInstanceOf[List[StdType]]
    }
  }

  protected def getDeparts(): List[Department] = {
    val project = getProject
    if (null != getProject) {
      val data = CollectUtils.newHashSet(Strings.split(restrictionHelper.departmentIdSeq))
      if (data.contains("*")) return project.departments
      val rs = CollectUtils.newArrayList()
      for (d <- project.departments if data.contains(d.getId.toString)) rs.add(d)
      rs
    } else {
      restrictionHelper.getDeparts.asInstanceOf[List[Department]]
    }
  }

  protected def getCollegeOfDeparts(): List[Department] = {
    val departments = CollectUtils.newArrayList()
    for (department <- getDeparts if department.isCollege) {
      departments.add(department)
    }
    departments
  }

  protected def getProjects(): List[Project] = {
    restrictionHelper.getProjects.asInstanceOf[List[Project]]
  }

  @Deprecated
  protected def getColleges(): List[Department] = {
    val project = getProject
    if (null != project) {
      CollectionUtils.intersection(project.departments, restrictionHelper.getColleges).asInstanceOf[List[Department]]
    } else {
      restrictionHelper.getColleges.asInstanceOf[List[Department]]
    }
  }

  protected def getTeachDeparts(): List[Department] = {
    val project = getProject
    if (null != project) {
      CollectionUtils.intersection(project.departments, restrictionHelper.getTeachDeparts).asInstanceOf[List[Department]]
    } else {
      restrictionHelper.getTeachDeparts.asInstanceOf[List[Department]]
    }
  }

  protected def getDepartmentIdSeq(): String = restrictionHelper.departmentIdSeq

  protected def getStdTypeIdSeq(): String = restrictionHelper.stdTypeIdSeq

  @Deprecated
  protected def getDataRealmLimit(): DataRealmLimit = {
    val limit = new DataRealmLimit()
    limit.getDataRealm.setStudentTypeIdSeq(getStdTypeIdSeq)
    limit.getDataRealm.setDepartmentIdSeq(getDepartmentIdSeq)
    limit.setPageLimit(getPageLimit)
    limit
  }

  protected def getDataRealm(): DataRealm = {
    DataRealm.mergeAll(restrictionHelper.getDataRealms)
  }

  protected def getDataRealms(): List[_] = restrictionHelper.getDataRealms

  protected def getDataRealmsWith(stdTypeId: java.lang.Long): List[DataRealm] = {
    restrictionHelper.getDataRealmsWith(stdTypeId)
  }

  def setRestrictionHelper(dataRealmHelper: RestrictionHelper) {
    this.restrictionHelper = dataRealmHelper
  }
}
