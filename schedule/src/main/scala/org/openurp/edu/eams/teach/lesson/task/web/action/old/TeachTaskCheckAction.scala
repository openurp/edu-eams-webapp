package org.openurp.edu.eams.teach.lesson.task.web.action.old

import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.model.PlanTask
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.web.action.common.SemesterSupportAction
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper

import scala.collection.JavaConversions._

class TeachTaskCheckAction extends SemesterSupportAction {

  protected var baseInfoSearchHelper: BaseInfoSearchHelper = _

  protected var majorPlanService: MajorPlanService = _

  def index(): String = {
    val stdTypes = baseCodeService.getCodes(classOf[StdType])
    val user = getUser
    val stdTypeId = getLong("semester.studentType.id")
    put("stdTypeList", stdTypes)
    putSemester(null)
    put("departmentList", getTeachDeparts)
    forward()
  }

  def search(): String = {
    put("adminClasses", entityDao.search(baseInfoSearchHelper.buildAdminclassQuery()))
    forward()
  }

  def info(): String = {
    val adminClassIds = Strings.splitToInt(get("adminClassIds"))
    val semester = semesterService.getSemester(getInt("task.semester.id"))
    val adminClasses = entityDao.get(classOf[Adminclass], "id", adminClassIds)
    put("adminClasses", adminClasses)
    val adminClassMap = new HashMap()
    for (adminClass <- adminClasses) {
      val planCourses = new HashSet()
      val termCalc = new TermCalculator(semesterService, semester)
      val onCampusTimeNotFound = false
      val term = termCalc.getTerm(adminClass.getEffectiveAt, false)
      val plan = majorPlanService.majorPlanByAdminClass(adminClass)
      if (null != plan) {
        planCourses.addAll(PlanUtils.getPlanCourses(plan, term))
      }
      val tasksQuery = OqlBuilder.from(classOf[Lesson], "task")
      tasksQuery.where("task.semester.id=:semeterId", getLong("task.semester.id"))
      tasksQuery.join("task.teachClass.adminClasses", "adminClass")
      tasksQuery.where("adminClass.id in (:adminClassIds)", Array(adminClass.getId))
      val tasks = entityDao.search(tasksQuery)
      val taskCourses = new HashSet()
      for (z <- 0 until tasks.size) {
        val task = tasks.get(z).asInstanceOf[Lesson]
        taskCourses.add(task.getCourse)
      }
      val planTaskQry = OqlBuilder.from(classOf[PlanTask], "planTask")
      planTaskQry.where("planTask.teachPlan=:teachPlan", plan)
      planTaskQry.where("planTask.flag=:flag", PlanTask.REQ_CLOSE)
      planTaskQry.select("planTask.course")
      val planTasks = entityDao.search(planTaskQry)
      planCourses.removeAll(planTasks)
      taskCourses.removeAll(planTasks)
      val allCourses = new HashSet()
      allCourses.addAll(planCourses)
      allCourses.addAll(taskCourses)
      adminClassMap.put(adminClass.getId.toString, Array(planCourses, taskCourses, allCourses, onCampusTimeNotFound))
    }
    put("adminClassMap", adminClassMap)
    forward()
  }
}
