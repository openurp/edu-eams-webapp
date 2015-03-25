package org.openurp.edu.eams.system.web.action

import java.sql.Dateimport java.util.Comparator

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.struts2.ServletActionContext
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.util.HierarchyEntityUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.CookieUtils
import org.beangle.security.auth.AuthenticationManager
import org.beangle.security.blueprint.Profile
import org.beangle.security.blueprint.Property
import org.beangle.security.blueprint.SecurityUtils
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.nav.Menu
import org.beangle.security.blueprint.nav.MenuProfile
import org.beangle.security.blueprint.nav.service.MenuService
import org.beangle.security.core.AuthenticationException
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.exception.EamsException
import org.openurp.edu.eams.system.doc.model.Document
import org.openurp.edu.eams.system.doc.model.ManagerDocument
import org.openurp.edu.eams.system.doc.model.StudentDocument
import org.openurp.edu.eams.system.doc.model.TeacherDocument
import org.openurp.edu.eams.system.firstlogin.FirstLoginCheckService
import org.openurp.edu.eams.system.msg.service.SystemMessageService
import org.openurp.edu.eams.system.notice.model.ManagerNotice
import org.openurp.edu.eams.system.notice.model.Notice
import org.openurp.edu.eams.system.notice.model.StudentNotice
import org.openurp.edu.eams.system.notice.model.TeacherNotice
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.web.action.BaseAction
import org.openurp.edu.eams.web.helper.RestrictionHelper



class HomeAction extends BaseAction {

  var systemMessageService: SystemMessageService = _

  var authenticationManager: AuthenticationManager = _

  var menuService: MenuService = _

  var restrictionHelper: RestrictionHelper = _

  var firstLoginCheckService: FirstLoginCheckService = _

  def index(): String = {
    val userId = getUserId
    if (null == userId) throw new AuthenticationException("without login")
    val user = entityDao.get(classOf[User], userId)
    if (null != firstLoginCheckService) {
      if (!firstLoginCheckService.check(user)) {
        put("checkerNames", firstLoginCheckService.getCheckerNames)
        return forward("loginCheck")
      }
    }
    put("user", SecurityUtils.getPrincipal)
    val menuProfile = getMenuProfile(user)
    put("menuProfile", menuProfile)
    val contextProjectId = changeProject()
    put("userCategoryId", getUserCategoryId)
    if (contextProjectId == null && menuProfile.getName.indexOf("管理") != -1) {
      return forward("noProject")
    }
    val dd = menuService.getMenus(menuProfile, user, getProfiles)
    val topMenus = CollectUtils.newArrayList()
    var maxdepath = 0
    for (m <- dd) {
      if (m.getDepth > maxdepath) maxdepath = m.getDepth
      if (null == m.getParent) topMenus.add(m)
    }
    if (maxdepath < 3) topMenus.clear()
    put("menus", topMenus)
    forward()
  }

  def changeProject(): java.lang.Integer = {
    val request = ServletActionContext.getRequest
    val response = ServletActionContext.getResponse
    CookieUtils.deleteCookieByName(request, response, "semester.id")
    var projectsOwnedByUser = CollectUtils.newArrayList()
    var contextProjectId = getInt("contextProjectId")
    if (contextProjectId == null) {
      contextProjectId = getRequest.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
    }
    try {
      if (EamsUserCategory.MANAGER_USER == getUserCategoryId) {
        projectsOwnedByUser = restrictionHelper.getProjects("/dataQuery").asInstanceOf[List[Project]]
      }
    } catch {
      case e: EamsException => logger.info(ExceptionUtils.getStackTrace(e))
    }
    Collections.sort(projectsOwnedByUser, new Comparator[Project]() {

      def compare(arg0: Project, arg1: Project): Int = return arg0.id.compareTo(arg1.id)
    })
    var contextProjectIdValid = false
    for (project <- projectsOwnedByUser if project.id == contextProjectId) {
      contextProjectIdValid = true
      //break
    }
    if (!contextProjectIdValid) {
      contextProjectId = null
      if (CollectUtils.isNotEmpty(projectsOwnedByUser)) {
        contextProjectId = projectsOwnedByUser.get(0).id
      }
    }
    if (null != contextProjectId && !projectsOwnedByUser.isEmpty) {
      getRequest.getSession.setAttribute("projectId", contextProjectId)
      put("contextProjectId", contextProjectId)
      put("projects", projectsOwnedByUser)
      var projectProfile: Profile = null
      securityHelper.setSessionProfile(null)
      for (profile <- getProfiles) {
        val p = profile.getProperty("projects")
        if (null != p) {
          if (p.getValue == Property.AllValue) {
            projectProfile = profile
            //break
          } else {
            if (CollectUtils.newHashSet(Strings.split(p.getValue, ","))
              .contains(String.valueOf(contextProjectId))) {
              projectProfile = profile
              //break
            }
          }
        }
      }
      securityHelper.setSessionProfile(projectProfile)
    }
    contextProjectId
  }

  def welcome(): String = {
    var docQuery: OqlBuilder[_ <: Document] = null
    val curProfileId = getUserCategoryId
    var notices: List[_ <: Notice] = null
    var kind = "manager"
    if (curProfileId == EamsUserCategory.STD_USER) {
      docQuery = OqlBuilder.from(classOf[StudentDocument], "doc")
      docQuery.cacheable(true)
      val stds = entityDao.get(classOf[Student], "code", getUsername)
      var std: Student = null
      if (!stds.isEmpty) {
        std = stds.get(0).asInstanceOf[Student]
        put("student", std)
        val query = OqlBuilder.from(classOf[StudentNotice], "stdNotice")
        query.join("stdNotice.stdTypes", "stdType")
        query.where("stdType.id=:stdTs", std.getType.id)
        query.join("stdNotice.departs", "depart")
        query.where("depart.id=:dept", std.department.id)
        query.orderBy("stdNotice.updatedAt")
        notices = entityDao.search(query)
        kind = "std"
      }
    } else if (curProfileId == EamsUserCategory.TEACHER_USER) {
      docQuery = OqlBuilder.from(classOf[TeacherDocument], "doc")
      val teachers = entityDao.get(classOf[Teacher], "code", getUsername)
      if (!teachers.isEmpty) {
        val teacher = teachers.get(0).asInstanceOf[Teacher]
        put("teacher", teacher)
        val query = OqlBuilder.from(classOf[TeacherNotice]).orderBy("updatedAt")
        notices = entityDao.search(query)
        kind = "teacher"
      }
    } else {
      docQuery = OqlBuilder.from(classOf[ManagerDocument], "doc")
      val query = OqlBuilder.from(classOf[ManagerNotice])
      query.orderBy("updatedAt")
      notices = entityDao.search(query)
    }
    docQuery.orderBy(Order.parse("doc.uploadOn desc"))
    docQuery.limit(new PageLimit(1, 7))
    put("downloadFileList", entityDao.search(docQuery))
    put("notices", notices)
    put("curProfileId", curProfileId)
    put("kind", kind)
    put("newMessageCount", systemMessageService.countNewly(getUsername))
    put("date", new Date(System.currentTimeMillis()))
    forward()
  }

  protected def getMenuProfile(user: User): MenuProfile = {
    var menuProfile: MenuProfile = null
    val menuProfiles = menuService.getProfiles(user)
    Collections.sort(menuProfiles, new Comparator[MenuProfile]() {

      def compare(arg0: MenuProfile, arg1: MenuProfile): Int = return arg0.id.compareTo(arg1.id)
    })
    put("menuProfiles", menuProfiles)
    var menuProfileId = getInt("menuProfileId")
    if (null == menuProfileId) {
      menuProfileId = getSession.get("menuProfileId").asInstanceOf[java.lang.Integer]
    }
    if (null == menuProfileId) {
      if (!menuProfiles.isEmpty) menuProfile = menuProfiles.get(0)
    } else {
      menuProfile = entityDao.get(classOf[MenuProfile], menuProfileId)
    }
    if (null != menuProfile) {
      menuProfileId = menuProfile.id
      getSession.put("menuProfileId", menuProfileId)
    }
    menuProfile
  }

  def submenus(): String = {
    val menuId = getInt("menu.id")
    val user = entityDao.get(classOf[User], getUserId)
    val modulesTree = menuService.getMenus(getMenuProfile(user), user, getProfiles)
    if (null != menuId) {
      val menu = entityDao.get(classOf[Menu], menuId)
      modulesTree.retainAll(HierarchyEntityUtils.getFamily(menu))
      modulesTree.remove(menu)
    }
    put("submenus", modulesTree)
    forward()
  }
}
