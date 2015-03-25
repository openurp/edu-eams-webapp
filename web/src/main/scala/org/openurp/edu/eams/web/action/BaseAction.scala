package org.openurp.edu.eams.web.action




import javax.servlet.http.HttpServletResponse
import org.apache.struts2.ServletActionContext
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.pojo.BaseCode
import org.beangle.commons.event.Event
import org.beangle.commons.event.EventMulticaster
import org.beangle.commons.lang.Assert
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.ems.dictionary.service.BaseCodeService
import org.beangle.ems.web.action.SecurityActionSupport
import org.beangle.security.blueprint.nav.MenuProfile
import org.beangle.struts2.convention.route.Action
import org.openurp.edu.eams.base.BaseInfo
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.service.BaseInfoService
import org.openurp.edu.eams.core.service.DepartmentService
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.system.security.EamsUserCategory
import org.openurp.edu.eams.web.helper.LogHelper
import org.openurp.edu.eams.web.helper.SemesterHelper
import org.openurp.edu.eams.web.util.OutputProcessObserver
import org.openurp.edu.eams.web.util.OutputWebObserver
import com.opensymphony.xwork2.ActionContext



abstract class BaseAction extends SecurityActionSupport {

  var baseCodeService: BaseCodeService = _

  var baseInfoService: BaseInfoService = _

  var eventMulticaster: EventMulticaster = _

  var semesterService: SemesterService = _

  var departmentService: DepartmentService = _

  var logHelper: LogHelper = _

  var semesterHelper: SemesterHelper = _

  protected def getUserCategoryId(): java.lang.Integer = {
    var userCategoryId = ActionContext.getContext.getSession.get("security.userCategoryId").asInstanceOf[java.lang.Integer]
    val loginName = getUsername
    if (null != loginName) {
      var query = OqlBuilder.from(classOf[Student], "std").select("std.id")
        .where("std.code=:code", loginName)
      var ids = entityDao.search(query).asInstanceOf[List[Long]]
      if (!ids.isEmpty) {
        userCategoryId = EamsUserCategory.STD_USER
      } else {
        query = OqlBuilder.from(classOf[Teacher], "t").select("t.id")
          .where("t.code=:code", loginName)
        ids = entityDao.search(query).asInstanceOf[List[Long]]
        if (!ids.isEmpty) {
          userCategoryId = EamsUserCategory.TEACHER_USER
          val menuProfileId = ServletActionContext.getRequest.getSession.getAttribute("menuProfileId").asInstanceOf[java.lang.Integer]
          val menuProfile = entityDao.get(classOf[MenuProfile], menuProfileId)
          if (menuProfile.getName.indexOf("管理") != -1) {
            userCategoryId = EamsUserCategory.MANAGER_USER
          }
        } else {
          userCategoryId = EamsUserCategory.MANAGER_USER
        }
      }
    }
    ActionContext.getContext.getSession.put("security.userCategoryId", userCategoryId)
    userCategoryId
  }

  protected def getLoginStudent(): Student = {
    val loginName = getUsername
    if (null != loginName) {
      val projectId = getSession.get("projectId").asInstanceOf[java.lang.Integer]
      val students = entityDao.get(classOf[Student], "code", loginName)
      var std: Student = null
      if (!students.isEmpty) {
        std = students.get(0)
      }
      if (std == null) {
        return null
      }
      if (null != projectId) {
        val query = OqlBuilder.from(classOf[Student], "std")
        query.where("std.project.id=:projectId", projectId)
        if (std.getPerson != null) {
          query.where("std.person = :person", std.getPerson)
        }
        val it = entityDao.search(query).iterator()
        if (it.hasNext) it.next() else null
      } else {
        getSession.put("projectId", std.getProject.id)
        std
      }
    } else {
      null
    }
  }

  protected def getLoginTeacher(): Teacher = {
    val loginName = getUsername
    if (null == loginName) null else {
      val it = entityDao.get(classOf[Teacher], "code", loginName).iterator()
      if (it.hasNext) it.next() else null
    }
  }

  protected def forwardError(message: String): String = {
    addError(message)
    "error"
  }

  protected def forwardError(messages: Array[String]): String = {
    var i = 0
    while (i < messages.length) {
      addMessage(messages(i += 1))
    }
    "error"
  }

  def importForm(): String = forward("/components/importData/form")

  def putSemester(project: Project): Semester = semesterHelper.putSemester(project)

  def setSemesterDataRealm(realmScope: Int) {
    putSemester(null)
  }

  protected def addBaseCode(key: String, clazz: Class[_ <: BaseCode[Integer]]) {
    key
    put(key, baseCodeService.getCodes(clazz))
  }

  protected def addBaseInfo(key: String, clazz: Class[_ <: BaseInfo]) {
    put(key, baseInfoService.getBaseInfos(clazz))
  }

  protected def getOutputProcessObserver(forwardName: String, observerClass: Class[_ <: OutputWebObserver]): OutputWebObserver = {
    val response = ServletActionContext.getResponse
    response.setContentType("text/html; charset=utf-8")
    var observer: OutputWebObserver = null
    observer = observerClass.newInstance()
    observer.setTextResource(getTextResource)
    observer.setWriter(response.getWriter)
    observer.setPath(forwardName)
    observer.outputTemplate()
    observer
  }

  protected def redirect(action: Action, message: String, prefixes: Array[String]): String = {
    val params = ActionContext.getContext.getParameters
    if (null != prefixes && prefixes.length > 0) {
      for (key <- params.keySet if Objects.!=("params", key); i <- 0 until prefixes.length if key.startsWith(prefixes(i))) {
        val value = get(key)
        if (Strings.isNotEmpty(value)) {
          action.getParams.put(key, value)
        }
        //break
      }
    }
    redirect(action, message)
  }

  protected def getOutputProcessObserver(): OutputProcessObserver = {
    getOutputProcessObserver("processDisplay.ftl", classOf[OutputWebObserver])
  }

  protected def getOutputProcessObserver(observerClass: Class[_ <: OutputWebObserver]): OutputProcessObserver = {
    getOutputProcessObserver("processDisplay.ftl", observerClass)
  }

  protected def publish(event: Event) {
    eventMulticaster.multicast(event)
  }

  protected def debug(debubObj: AnyRef) {
    logger.debug(String.valueOf(debubObj))
  }

  protected def debug(debubObj: AnyRef, e: Exception) {
    logger.debug(String.valueOf(debubObj), e)
  }

  protected def info(infoObj: AnyRef) {
    logger.info(String.valueOf(infoObj))
  }

  protected def info(infoObj: AnyRef, e: Exception) {
    logger.info(String.valueOf(infoObj), e)
  }

  protected def error(errorObj: AnyRef) {
    logger.error(String.valueOf(errorObj))
  }

  protected def error(errorObj: AnyRef, e: Exception) {
    logger.error(String.valueOf(errorObj), e)
  }
}
