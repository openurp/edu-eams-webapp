package org.openurp.edu.eams.web.helper


NotFoundException
import javax.servlet.http.HttpServletRequest
import org.apache.struts2.ServletActionContext
import org.beangle.data.model.dao.EntityDao
import org.beangle.commons.lang.Strings
import org.beangle.commons.web.util.CookieUtils
import org.beangle.security.blueprint.data.service.DataPermissionService
import org.beangle.security.blueprint.function.service.FuncPermissionService
import org.beangle.security.blueprint.nav.MenuProfile
import org.beangle.struts2.helper.ContextHelper
import org.beangle.struts2.helper.Params
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.system.security.EamsUserCategory
import com.opensymphony.xwork2.ActionContext



class SemesterHelper extends RestrictionHelperImpl() {

  var semesterService: SemesterService = _

  def this(semesterService: SemesterService, entityDao: EntityDao) {
    this()
    setSemesterService(semesterService)
    setEntityDao(entityDao)
  }

  def this(semesterService: SemesterService, 
      entityDao: EntityDao, 
      funcPermissionService: FuncPermissionService, 
      dataPermissionService: DataPermissionService) {
    this()
    setSemesterService(semesterService)
    setEntityDao(entityDao)
    setFuncPermissionService(funcPermissionService)
    setDataPermissionService(dataPermissionService)
  }

  def putSemester(project: Project): Semester = {
    val request = ServletActionContext.getRequest
    val projects = getProjects.asInstanceOf[List[Project]]
    var projectId = Params.getInt("project.id")
    if (projectId == null) {
      projectId = ServletActionContext.getRequest.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
    }
    if (projectId != null) {
      if (EamsUserCategory.MANAGER_USER == ServletActionContext.getRequest.getSession.getAttribute("security.userCategoryId")) {
        val givenProject = entityDao.get(classOf[Project], projectId)
        if (projects.contains(givenProject)) {
          project = givenProject
        }
      } else {
        project = entityDao.get(classOf[Project], projectId)
      }
    }
    if (null == project && !projects.isEmpty) {
      project = projects.get(0).asInstanceOf[Project]
    }
    if (null == project) return null
    var semester: Semester = null
    var semesterId = Params.get("semester.id")
    if (Strings.isEmpty(semesterId)) {
      semesterId = CookieUtils.getCookieValue(request, "semester.id")
    }
    if (Strings.isNotEmpty(semesterId)) {
      semester = entityDao.get(classOf[Semester], java.lang.Integer.valueOf(semesterId))
      if (null != semester) {
        val cookieAge = 60 * 60 * 24 * 30 * 6
        CookieUtils.addCookie(request, ServletActionContext.getResponse, "semester.id", semesterId, cookieAge)
      }
    }
    if (null == semester) {
      semester = semesterService.getCurSemester(project)
    }
    if (null == semester) {
      throw new EntityNotFoundException("没有找到学期数据")
    }
    ContextHelper.put("semester", semester)
    semester
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }
}
