package org.openurp.edu.eams.dataQuery.web.action

import java.io.IOException
import java.io.Writer


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.beangle.ems.dictionary.model.CodeMeta
import org.beangle.security.access.AccessDeniedException
import org.openurp.edu.eams.base.Building
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction
import org.openurp.edu.eams.web.view.component.semester.SemesterCalendar
import com.opensymphony.xwork2.util.ValueStack
import freemarker.core.Environment
import DataQueryAction._



object DataQueryAction {

  object DataType extends Enumeration {

    val PROJECT = new DataType()

    val DEPARTMENT = new DataType()

    val MAJOR = new DataType()

    val STDTYPE = new DataType()

    val EDUCATION = new DataType()

    val COLLEGE = new DataType()

    val SEMESTER = new DataType()

    val SEMESTERCALENDAR = new DataType()

    val BASECODE = new DataType()

    val PROJECTID = new DataType()

    val CURRPROJECT = new DataType()

    val MAJORCASCADE = new DataType()

    val DIRECTIONCASCADE = new DataType()

    val BUILDINGCASCADE = new DataType()

    class DataType extends Val {

      override def toString(): String = {
        if (this == STDTYPE) "stdType" else super.toString
      }
    }

    implicit def convertValue(v: Value): DataType = v.asInstanceOf[DataType]
  }
}

class DataQueryAction extends RestrictionSupportAction {

  def index(): String = {
    var view = get("dataType")
    if (view == null) {
      view = "project"
    }
    val `type` = getDataType(view)
    getDatas(`type`)
    forward(getView(view))
  }

  def changeProjectId() {
    var projectId = getInt("projectId")
    val projects = getProjects
    if (null == projectId) {
      projectId = getRequest.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
      if (null == projectId) {
        if (!projects.isEmpty) {
          projectId = projects.get(0).id
        }
      }
      getRequest.getSession.setAttribute("projectId", projectId)
    } else {
      for (project <- projects if project.id == projectId) {
        getRequest.getSession.setAttribute("projectId", projectId)
        //break
      }
    }
    val request = getRequest
    val response = getResponse
    var writer: Writer = null
    try {
      response.setContentType(request.getContentType)
      writer = response.getWriter
      writer.write("success")
      writer.flush()
    } catch {
      case e: IOException => 
    } finally {
      if (null != writer) {
        try {
          writer.close()
        } catch {
          case e: IOException => 
        }
      }
    }
  }

  protected def getDatas(`type`: DataType) {
    val request = getRequest
    put("entityId", getLong("entityId"))
    if (`type` == DataType.PROJECT) {
      var projects = Collections.newBuffer[Any](0)
      try {
        projects = getProjects
      } catch {
        case e: AccessDeniedException => {
          val std = getLoginStudent
          if (null != std) {
            projects.add(std.getProject)
          } else {
            val teacher = getLoginTeacher
            if (null != teacher) {
              val depart = teacher.department
              val builder = OqlBuilder.from(classOf[Project], "p")
              builder.where(":departmet in elements(p.departments)", depart.id)
              projects = entityDao.search(builder)
              if (projects.isEmpty) {
                throw e
              }
            } else {
              throw e
            }
          }
        }
      }
      put("datas", projects)
      put("entityId", request.getSession.getAttribute("projectId"))
    } else if (`type` == DataType.PROJECTID) {
      put("datas", request.getSession.getAttribute("projectId"))
    } else if (`type` == DataType.CURRPROJECT) {
      put("datas", entityDao.get(classOf[Project], request.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]))
    } else if (`type` == DataType.DEPARTMENT) {
      put("datas", getDeparts)
    } else if (`type` == DataType.COLLEGE) {
      put("datas", getColleges)
    } else if (`type` == DataType.MAJOR) {
      put("datas", getMajors)
    } else if (`type` == DataType.STDTYPE) {
      put("datas", getStdTypes)
    } else if (`type` == DataType.EDUCATION) {
      put("datas", getEducations)
    } else if (`type` == DataType.SEMESTER) {
      put("datas", getSemesters)
    } else if (`type` == DataType.MAJORCASCADE) {
      put("datas", getMajorsCasCade)
    } else if (`type` == DataType.DIRECTIONCASCADE) {
      put("datas", getDirectionCascade)
    } else if (`type` == DataType.BUILDINGCASCADE) {
      put("datas", getBuildingCascade)
    } else if (`type` == DataType.SEMESTERCALENDAR) {
      val semesterCalendar = getSemesterCalendar
      val semesterId = getInt("value")
      if (null != semesterId) {
        val semester = entityDao.get(classOf[Semester], semesterId)
        val semesters = semesterCalendar.get(semester.getSchoolYear)
        if (null != semesters) {
          for (s <- semesters if semester == s) {
            put("value", s)
            //break
          }
        }
      }
      put("semesterId", semesterId)
      put("datas", semesterCalendar)
      put("tagId", get("tagId"))
      put("uiType", StringUtils.lowerCase(get("uiType")))
    } else if (`type` == DataType.BASECODE) {
      put("datas", getBaseCodes)
    } else {
      put("datas", getDatas)
    }
  }

  protected def getSemesters(): List[Semester] = {
    val request = getRequest
    val projectId = request.getSession.getAttribute("projectId").asInstanceOf[java.lang.Integer]
    if (projectId == null) return Collections.emptyList()
    val p = entityDao.get(classOf[Project], projectId)
    val builder = OqlBuilder.from(classOf[Semester], "s").where("s.calendar=:calendar and s.archived=false", 
      p.getCalendar)
      .orderBy("s.beginOn")
      .cacheable()
    entityDao.search(builder)
  }

  protected def getSemesterCalendar(): Map[String, List[Semester]] = {
    val request = getRequest
    val calendar = new SemesterCalendar(request.getAttribute("struts.valueStack").asInstanceOf[ValueStack], 
      false)
    calendar.setEmpty(get("empty"))
    calendar.setFormat(get("format"))
    calendar.setItems(getSemesters)
    calendar.setMulti(get("multi"))
    calendar.setYearRules(get("yearRules"))
    calendar.setTermRules(get("termRules"))
    calendar.getSemesterTree
  }

  protected def getBaseCodes(): List[_] = {
    val simpleName = get("className")
    if (Strings.isNotBlank(simpleName)) {
      val it = entityDao.get(classOf[CodeMeta], "name", simpleName)
        .iterator()
      if (it.hasNext) {
        try {
          return entityDao.getAll(Class.forName(it.next().getClassName).asInstanceOf[Class[Entity[_]]])
        } catch {
          case e: ClassNotFoundException => return Collections.newBuffer[Any]
        }
      }
    }
    Collections.newBuffer[Any]
  }

  protected def getDatas(): AnyRef = {
    val simpleName = get("className")
    if (Strings.isNotBlank(simpleName)) {
      val it = entityDao.get(classOf[CodeMeta], "name", simpleName)
        .iterator()
      if (it.hasNext) {
        try {
          entityDao.getAll(Class.forName(it.next().getClassName).asInstanceOf[Class[Entity[_]]])
        } catch {
          case e: ClassNotFoundException => return Collections.newBuffer[Any]
        }
      }
    }
    Collections.newBuffer[Any]
  }

  private def getMajors(): List[Major] = {
    val departs = getDeparts
    if (Collections.isEmpty(departs)) {
      return Collections.newBuffer[Any]
    }
    val builder = OqlBuilder.from(classOf[Major]).where("exists(from major.journals md where md.depart in (:departs))", 
      departs)
      .where("major.effectiveAt <= :now and (major.invalidAt is null or major.invalidAt >= :now)", new java.util.Date())
    entityDao.search(builder)
  }

  private def getMajorsCasCade(): List[Major] = {
    val departId = getInt("departId")
    val educationId = getInt("educationId")
    val builder = OqlBuilder.from(classOf[Major], "major").where("major.effectiveAt <= :now and (major.invalidAt is null or major.invalidAt >= :now)", 
      new java.util.Date())
    if (null != educationId) {
      builder.where("major.education.id =:educationId", educationId)
    }
    if (null != departId) {
      builder.where("exists(from major.journals md where md.depart.id =:departId)", departId)
      entityDao.search(builder)
    } else {
      Collections.newBuffer[Any]
    }
  }

  private def getBuildingCascade(): List[Building] = {
    val builder = OqlBuilder.from(classOf[Building], "building").where("building.effectiveAt <= :now and (building.invalidAt is null or building.invalidAt >= :now)", 
      new java.util.Date())
    val campusId = getInt("campusId")
    if (campusId == null) {
      Collections.newBuffer[Any]
    } else {
      builder.where("building.campus.id =:campusId", campusId)
      entityDao.search(builder)
    }
  }

  private def getDirectionCascade(): List[Direction] = {
    val builder = OqlBuilder.from(classOf[Direction], "direction").where("direction.effectiveAt <= :now and (direction.invalidAt is null or direction.invalidAt >= :now)", 
      new java.util.Date())
    val majorId = getInt("majorId")
    if (majorId == null) {
      Collections.newBuffer[Any]
    } else {
      builder.where("direction.major.id =:majorId", majorId)
      entityDao.search(builder)
    }
  }

  protected def getDataType(): DataType = {
    val dataType = get("dataType")
    getDataType(dataType)
  }

  protected def getDataType(dataType: String): DataType = {
    if (null == dataType) {
      dataType = get("dataType")
    }
    if (Strings.isBlank(dataType)) {
      return DataType.PROJECT
    }
    DataType.valueOf(dataType.toUpperCase())
  }

  protected def getView(view: String): String = {
    val upperView = view.toUpperCase()
    if (DataType.PROJECT.toString == upperView) {
      "project"
    } else if (DataType.DEPARTMENT.toString == upperView) {
      "department"
    } else if (DataType.MAJOR.toString == upperView) {
      "major"
    } else if (DataType.STDTYPE.toString == upperView) {
      "stdtype"
    } else if (DataType.EDUCATION.toString == upperView) {
      "education"
    } else if (DataType.COLLEGE.toString == upperView) {
      "college"
    } else if (DataType.SEMESTER.toString == upperView) {
      "semester"
    } else if (DataType.SEMESTERCALENDAR.toString == upperView) {
      "semesterCalendar"
    } else if (DataType.BASECODE.toString == upperView) {
      "baseCode"
    } else if (DataType.PROJECTID.toString == upperView) {
      "projectId"
    } else if (DataType.CURRPROJECT.toString == upperView) {
      view
    } else if (DataType.MAJORCASCADE.toString == upperView) {
      "majorCascade"
    } else if (DataType.DIRECTIONCASCADE.toString == upperView) {
      "directionCascade"
    } else if (DataType.BUILDINGCASCADE.toString == upperView) {
      "buildingCascade"
    } else {
      view
    }
  }
}
