package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.List
import java.util.Map
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Room
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.schedule.model.CourseArrangeAlteration
import org.openurp.edu.eams.teach.schedule.model.CurriculumChangeApplication
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class CurriculumChangeApplyAction extends SemesterSupportAction {

  private var lessonService: LessonService = _

  override def getEntityName(): String = {
    classOf[CurriculumChangeApplication].getName
  }

  override def edit(): String = {
    val entityId = getLongId("change")
    val semesterId = getInt("semesterId")
    var curriculumChangeApplication: CurriculumChangeApplication = null
    if (null == entityId) {
      curriculumChangeApplication = populateEntity(classOf[CurriculumChangeApplication], "change").asInstanceOf[CurriculumChangeApplication]
    } else {
      curriculumChangeApplication = entityDao.get(classOf[CurriculumChangeApplication], entityId)
      if (true == curriculumChangeApplication.getPassed) {
        return redirect("search", "审核已通过,不可修改", "passed = 1")
      }
    }
    put("change", curriculumChangeApplication)
    var semester: Semester = null
    semester = if (null == semesterId) semesterService.getCurSemester(getProject) else entityDao.get(classOf[Semester], 
      semesterId)
    val builder = OqlBuilder.from(classOf[Lesson], "lesson").join("lesson.teachers", "teacher")
      .where("teacher = :teacher", getLoginTeacher)
      .where("lesson.semester = :semester", semester)
    builder.where("lesson.project=:project1", getProject)
    val lessons = entityDao.search(builder)
    val lessonMap = CollectUtils.newHashMap()
    for (lesson <- lessons) {
      lessonMap.put(lesson.getId, lesson.getCourse.getName + "[" + lesson.getNo + "]")
    }
    put("lessons", lessonMap)
    forward()
  }

  override def save(): String = {
    val application = populateEntity(classOf[CurriculumChangeApplication], "change")
    try {
      if (application.isTransient) {
        application.setTeacher(getLoginTeacher)
      }
      application.setTime(new Date())
      application.setPassed(null)
      saveOrUpdate(Collections.singletonList(application))
      redirect("search", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("search", "info.save.failure")
      }
    }
  }

  def search(): String = {
    val teacher = getLoginTeacher
    if (null == teacher) {
      return forwardError("没有权限")
    }
    val builder = OqlBuilder.from(classOf[CurriculumChangeApplication], "change")
      .where("change.teacher = :teacher", teacher)
      .limit(getPageLimit)
      .orderBy(Order.parse(get("orderBy")))
    populateConditions(builder)
    putSemester(null)
    val passed = getBoolean("passed")
    if (null == passed) {
      builder.where("change.passed is null")
    } else {
      if (true == passed) {
        builder.where("change.passed is true")
      } else {
        builder.where("change.passed is false")
      }
    }
    if (passed != null) {
      put("passed", passed)
    }
    put("changes", entityDao.search(builder))
    forward()
  }

  def courseArrangeAlterations(): String = {
    val lessonId = getLongId("lesson")
    if (null == lessonId) {
      put("arrangeAlterInfo", CollectionUtils.EMPTY_COLLECTION)
      return forward()
    }
    val builder = OqlBuilder.from(classOf[CourseArrangeAlteration], "alter")
      .where("alter.lessonId = :lessonId", lessonId)
      .orderBy("alter.alterationAt desc")
    val alterations = entityDao.search(builder)
    val builder1 = OqlBuilder.from(classOf[CourseActivity], "activity")
      .where("activity.lesson.id = :lessonId", lessonId)
      .join("activity.rooms", "room")
      .select("room")
      .limit(1, 3)
    val classrooms = entityDao.search(builder1).asInstanceOf[List[Classroom]]
    var teacherExists = false
    var roomExists = false
    val alterationMap = new HashMap[CourseArrangeAlteration, String]()
    for (courseArrangeAlteration <- alterations) {
      var show = ""
      val alterationAfter = courseArrangeAlteration.getAlterationAfter
      val teachers = entityDao.get(classOf[Lesson], courseArrangeAlteration.getLessonId)
        .getTeachers
      for (teacher <- teachers if Strings.contains(alterationAfter, teacher.getName)) {
        teacherExists = true
      }
      for (room <- classrooms if Strings.contains(alterationAfter, room.getName)) {
        roomExists = true
      }
      if (roomExists) {
        show += "教室 "
      }
      if (teacherExists) {
        show += "教师 "
      }
      show += "时间"
      alterationMap.put(courseArrangeAlteration, show)
    }
    put("arrangeAlterInfo", alterationMap)
    forward()
  }

  override def info(): String = {
    val changeId = getLongId("change")
    if (null == changeId) {
      return forwardError("调课记录没有找到")
    }
    put("alteration", entityDao.get(classOf[CurriculumChangeApplication], changeId))
    forward()
  }

  override def remove(): String = {
    val changeIds = getLongIds("change")
    if (ArrayUtils.isEmpty(changeIds)) {
      return forwardError("调课记录没有找到")
    }
    val changeApply = entityDao.get(classOf[CurriculumChangeApplication], changeIds)
    val toRemoveList = CollectUtils.newArrayList()
    for (curriculumChangeApplication <- changeApply if curriculumChangeApplication.getPassed == null || false == curriculumChangeApplication.getPassed) {
      toRemoveList.add(curriculumChangeApplication)
    }
    try {
      entityDao.remove(toRemoveList)
      redirect("search", "info.delete.success")
    } catch {
      case e: Exception => redirect("search", "info.delete.failure")
    }
  }

  protected override def indexSetting() {
    put("teachDepartList", lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(getProject), 
      getDeparts, getAttribute("semester").asInstanceOf[Semester]))
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }
}
