package org.openurp.edu.eams.teach.grade.search.web.action

import java.util.Date




import javax.servlet.http.HttpServletRequest
import org.beangle.commons.lang.Strings
import org.apache.struts2.ServletActionContext
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.eams.teach.grade.model.GradeRateItem
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.model.StdSemesterGpa
import org.openurp.edu.eams.teach.grade.service.GpaStatService
import org.openurp.edu.eams.teach.grade.service.impl.MultiStdGpa
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.web.helper.StdSearchHelper
import com.opensymphony.xwork2.ActionContext



class GpaAction extends GpaStatAction {

  var gpaStatService: GpaStatService = _

  var stdSearchHelper: StdSearchHelper = _

  var lessonGradeService: LessonGradeService = _

  protected override def getEntityName(): String = classOf[StdGpa].getName

  protected def getQueryBuilder(): OqlBuilder[StdGpa] = {
    val query = OqlBuilder.from(classOf[StdGpa], "stdGpa")
    val stdGpaIdStr = get("stdGpaIds")
    if (Strings.isNotEmpty(stdGpaIdStr)) {
      query.where("stdGpa.id in (:stdGpaIds)", Strings.splitToLong(stdGpaIdStr))
    }
    populateConditions(query)
    if (getProject != null) query.where("stdGpa.std.project =:project", getProject)
    if (CollectUtils.isNotEmpty(getDeparts)) query.where("stdGpa.std.department in (:departments)", getDeparts)
    query.limit(getPageLimit)
    query.orderBy(Order.parse(if (Strings.isBlank(get("orderBy"))) "stdGpa.std.code" else get("orderBy")))
    query
  }

  def search(): String = {
    val builder = getQueryBuilder.asInstanceOf[OqlBuilder]
    val requset = ServletActionContext.getRequest
    requset.getSession.setAttribute("gpasBuilder", builder)
    put("stdGpas", entityDao.search(builder))
    forward()
  }

  def stdGpaReport(): String = {
    var stdIds = get("stdIds")
    if (Strings.isBlank(stdIds)) stdIds = get("std.ids")
    if (Strings.isEmpty(stdIds)) {
      return forwardError("error.parameters.needed")
    }
    val stds = entityDao.get(classOf[Student], Strings.splitToLong(stdIds))
    var isMinor = getBoolean("stdGpa.minor")
    if (null == isMinor) isMinor = false
    put("isMinor", isMinor)
    val stdGpas = CollectUtils.newArrayList()
    for (std <- stds) {
      stdGpas.add(gpaStatService.statGpa(std))
    }
    entityDao.saveOrUpdate(stdGpas)
    if (stds.size <= 20) {
      put("stdGpas", stdGpas)
      forward()
    } else {
      redirect("search", "info.action.success")
    }
  }

  protected override def getExportDatas(): Iterable[_] = {
    val stdGpaIds = Strings.transformToLong(Strings.split(get("stdGpaIds")))
    val requset = ServletActionContext.getRequest
    var builder = requset.getSession.getAttribute("gpasBuilder").asInstanceOf[OqlBuilder[StdGpa]]
    if (stdGpaIds.length > 0) {
      if (null == builder) {
        builder = OqlBuilder.from(classOf[StdGpa], "stdGpa")
      }
      builder.where("stdGpa.id in (:stdGpaIds)", stdGpaIds)
      builder.limit(null)
      entityDao.search(builder)
    } else {
      entityDao.search(builder.limit(null))
    }
  }

  def reminderStds(): String = {
    val query = stdSearchHelper.buildStdQuery().asInstanceOf[OqlBuilder[Student]]
    query.where("not exists(from " + classOf[StdGpa].getName + " gpa where gpa.std=std)")
    put("students", entityDao.search(query))
    forward()
  }

  def statStdGpa(): String = {
    val query = stdSearchHelper.buildStdQuery().asInstanceOf[OqlBuilder[Student]]
    query.where("not exists(from " + classOf[StdGpa].getName + " gpa where gpa.std=std)")
    val stds = entityDao.search(query)
    if (CollectUtils.isEmpty(stds)) return redirect("search", "没有未统计的学生")
    val msg = gpaStatService.statGpas(stds)
    try {
      entityDao.saveOrUpdate(msg.getStdGpas)
      redirect("search", "统计完成")
    } catch {
      case e: Exception => redirect("search", "统计失败")
    }
  }

  def reStatGpSetting(): String = {
    val semesters = semesterService.getCalendar(Model.newInstance(classOf[Project], getInt("std.project.id")))
      .getSemesters
    var iter = semesters.iterator()
    while (iter.hasNext) {
      val semester = iter.next()
      if (semester.beginOn.after(new Date())) {
        iter.remove()
      }
    }
    Collections.sort(semesters, new PropertyComparator("beginOn", false))
    put("semesters", semesters)
    forward()
  }

  def reStatGp(): String = {
    val stdGpaIdStr = get("stdGpaIds")
    var stdGpas = CollectUtils.newArrayList()
    stdGpas = if (Strings.isNotEmpty(stdGpaIdStr)) entityDao.get(classOf[StdGpa], Strings.splitToLong(stdGpaIdStr)) else entityDao.search(getQueryBuilder)
    for (stdGpa <- stdGpas) {
      val newGpa = gpaStatService.statGpa(stdGpa.getStd)
      stdGpa.setUpdatedAt(newGpa.getUpdatedAt)
      stdGpa.setGa(newGpa.getGa)
      stdGpa.setGpa(newGpa.getGpa)
      stdGpa.setCredits(newGpa.getCredits)
      stdGpa.setCount(newGpa.getCount)
      val semesterGpaCache = CollectUtils.newHashMap()
      for (gpterm <- stdGpa.getSemesterGpas) {
        semesterGpaCache.put(gpterm.getSemester, gpterm)
      }
      for (newStdSemesterGpa <- newGpa.getSemesterGpas) {
        val stdSemesterGpa = semesterGpaCache.get(newStdSemesterGpa.getSemester)
        if (null == stdSemesterGpa) {
          stdGpa.add(newStdSemesterGpa)
        } else {
          stdSemesterGpa.setCount(newStdSemesterGpa.getCount)
          stdSemesterGpa.setCredits(newStdSemesterGpa.getCredits)
          stdSemesterGpa.setGa(newStdSemesterGpa.getGa)
          stdSemesterGpa.setGpa(newStdSemesterGpa.getGpa)
        }
      }
      val it = stdGpa.getSemesterGpas.iterator()
      while (it.hasNext) {
        val newStdSemesterGpa = newGpa.getStdTermGpa(it.next().getSemester)
        if (null == newStdSemesterGpa) {
          it.remove()
        }
      }
      entityDao.saveOrUpdate(stdGpa)
    }
    redirect("search", "info.update.success", sendParams(null, null, null))
  }

  def reStatGp(config: GradeRateConfig, semesterIds: String) {
    if (null != config) {
      for (gradeRateItem <- config.getItems) {
        val hql = "update " + classOf[CourseGrade].getName + " set gp=" + 
          gradeRateItem.getGpExp + 
          " where (score between " + 
          gradeRateItem.getMinScore + 
          " and " + 
          gradeRateItem.getMaxScore + 
          ") and instr('," + 
          semesterIds + 
          ",',','||semester.id||',')>0 and markStyle.id=" + 
          config.getScoreMarkStyle.id
        entityDao.executeUpdate(hql, null.asInstanceOf[Array[Any]])
      }
    }
  }

  protected def sendParams(include: String, string: String, exclude: String): String = {
    val exceptString = "method"
    var sendParams: StringBuilder = null
    sendParams = if (null != include) new StringBuilder(include) else new StringBuilder()
    val params = ActionContext.getContext.getParameters
    val keys = params.keySet
    for (key <- keys) {
      if (exceptString.indexOf(key) != -1) {
        //continue
      }
      if (exclude != null && exclude.indexOf(key) != -1) {
        //continue
      }
      if (null != string) {
        if (string.indexOf(key) != -1) {
          buildParams(sendParams, params, key)
        }
      } else {
        buildParams(sendParams, params, key)
      }
    }
    sendParams.toString
  }

  private def buildParams(sendParams: StringBuilder, params: Map[String, Any], key: String) {
    val paramsArr = params.get(key).asInstanceOf[Array[String]]
    if (paramsArr.length > 0 && paramsArr(0) != null && paramsArr(0) != "") {
      if (sendParams.length != 0) {
        sendParams.append("&")
      }
      sendParams.append(key).append("=").append(paramsArr(0))
    }
  }
}
