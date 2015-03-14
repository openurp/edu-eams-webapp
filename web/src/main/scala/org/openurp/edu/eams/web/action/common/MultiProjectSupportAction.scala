package org.openurp.edu.eams.web.action.common

import java.util.List
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.base.Project
import org.openurp.edu.eams.web.action.BaseAction

import scala.collection.JavaConversions._

abstract class MultiProjectSupportAction extends BaseAction {

  def index(): String = {
    val projects = getProjects
    if (projects.size == 1) {
      put("project", projects.get(0))
      getRequest.getSession.setAttribute("projectId", projects.get(0).getId)
      return forward(new Action(getClass, "innerIndex", "&projectId=" + projects.get(0).getId))
    }
    var defaultProjectId: java.lang.Integer = null
    if (projects.size > 0) {
      defaultProjectId = projects.get(0).getId
      var givenProjectId = getInt("projectId")
      if (null == givenProjectId) {
        givenProjectId = getRequest.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
      }
      if (null != givenProjectId) {
        for (p <- projects if p.getId == givenProjectId) {
          defaultProjectId = givenProjectId
          //break
        }
      }
    }
    put("defaultProjectId", defaultProjectId)
    getRequest.getSession.setAttribute("projectId", defaultProjectId)
    put("projects", projects)
    forward()
  }

  def innerIndex(): String

  protected def getProjects(): List[Project]

  protected def getProject(): Project = {
    var project = getAttribute("project").asInstanceOf[Project]
    if (null != project) return project
    var projectId = getInt("projectId")
    if (null == projectId) projectId = getRequest.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
    if (null == projectId) {
      val projects = getProjects
      if (projects.isEmpty) project = projects.get(0)
    } else {
      project = entityDao.get(classOf[Project], projectId)
    }
    if (null != project) {
      put("project", project)
      getRequest.getSession.setAttribute("projectId", project.getId)
    }
    project
  }
}
