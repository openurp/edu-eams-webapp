package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Collection
import java.util.Collections
import java.util.Date
import java.util.List
import java.util.Map
import java.util.Set
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.Operation
import org.beangle.commons.dao.Operation.Builder
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Department
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonGroup
import org.openurp.edu.teach.lesson.LessonPlanRelation
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan
import org.openurp.edu.eams.teach.program.util.PlanUtils
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class LessonGroupAction extends SemesterSupportAction {

  protected override def getEntityName(): String = classOf[LessonGroup].getName

  protected def indexSetting() {
    putSemester(null)
  }

  def search(): String = {
    val semester = putSemester(null)
    val builder = OqlBuilder.from[Long](classOf[LessonGroup].getName)
      .alias("lessonGroup")
    builder.select("lessonGroup.id")
    builder.where("lessonGroup.semester=:semester", semester)
    builder.where("lessonGroup.teachDepart in (:departments)", getDeparts)
    populateConditions(builder)
    builder.limit(getPageLimit)
    val lessonConditions = QueryHelper.extractConditions(classOf[Lesson], "lesson", null)
    val teacherConditions = QueryHelper.extractConditions(classOf[Teacher], "teacher", null)
    if ((lessonConditions.size + teacherConditions.size) > 0) {
      builder.join("lessonGroup.lessons", "lesson")
      builder.where("lesson.project = :project", getProject)
      if (!lessonConditions.isEmpty) {
        builder.where(lessonConditions)
      }
      if (!teacherConditions.isEmpty) {
        builder.join("lesson.teachers", "teacher")
        builder.where(teacherConditions)
      }
    }
    builder.groupBy("lessonGroup.id")
    put("lessonGroups", entityDao.get(classOf[LessonGroup], entityDao.search(builder)))
    forward()
  }

  protected def editSetting(entity: Entity[_]) {
    val semester = putSemester(null)
    put("guaPai", Model.newInstance(classOf[LessonTag], LessonTag.PredefinedTags.GUAPAI.getId))
    val departs = getDeparts
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.project=:project1", getProject)
    if (entity.isTransient) {
      if (!departs.isEmpty) {
        Collections.sort(departs)
        builder.where("lesson.semester=:semester and lesson.teachDepart=:teachDepart", semester, departs.get(0))
      }
    } else {
      val lessonGroup = entity.asInstanceOf[LessonGroup]
      builder.where("lesson.semester=:semester and lesson.teachDepart=:teachDepart", semester, lessonGroup.getTeachDepart)
        .where("lesson.group!=:group)", lessonGroup)
    }
    builder.orderBy("lesson.course.name,lesson.no")
    put("lessons", entityDao.search(builder))
    put("departs", departs)
  }

  def editByPlan(): String = {
    val entityId = getLongId(getShortName)
    var entity: Entity[_] = null
    entity = if (null == entityId) populateEntity() else getModel(getEntityName, entityId)
    put(getShortName, entity)
    forward()
  }

  def getPlanTree(): String = {
    val semester = putSemester(null)
    val builder = OqlBuilder.from(classOf[LessonPlanRelation], "lpr")
    populateConditions(builder)
    builder.where("lpr.lesson.semester=:semester", semester)
    builder.orderBy("lpr.plan.id")
    val lprs = entityDao.search(builder)
    val planLessonsMap = CollectUtils.newHashMap()
    for (lpr <- lprs) {
      var lessons = planLessonsMap.get(lpr.getPlan)
      if (null == lessons) {
        lessons = CollectUtils.newArrayList()
        planLessonsMap.put(lpr.getPlan, lessons)
      }
      lessons.add(lpr.getLesson)
    }
    val cal = new TermCalculator(semesterService, semester)
    val groupLessonMap = CollectUtils.newHashMap()
    for (plan <- planLessonsMap.keySet) {
      val term = cal.getTerm(plan.getProgram.getEffectiveOn, true)
      for (courseGroup <- plan.getGroups if !courseGroup.isCompulsory) {
        for (planCourse <- courseGroup.getPlanCourses if PlanUtils.openOnThisTerm(planCourse.getTerms, 
          term)) {
          val lessons = CollectUtils.newArrayList()
          for (lesson <- planLessonsMap.get(plan) if planCourse.getCourse == lesson.getCourse) {
            lessons.add(lesson)
          }
          groupLessonMap.put(courseGroup, lessons)
        }
        var lessons = groupLessonMap.get(courseGroup)
        if (CollectUtils.isEmpty(lessons)) {
          for (lesson <- planLessonsMap.get(plan) if courseGroup.getCourseType == lesson.getCourseType) {
            if (null == lessons) lessons = CollectUtils.newArrayList()
            lessons.add(lesson)
          }
          groupLessonMap.put(courseGroup, lessons)
        }
      }
    }
    forward()
  }

  protected def saveAndForward(entity: Entity[_]): String = {
    val lessonGroup = entity.asInstanceOf[LessonGroup]
    val lessonIds = Strings.transformToLong(Strings.split(get("selectedLessonIds"), ","))
    if (ArrayUtils.isNotEmpty(lessonIds)) {
      lessonGroup.setProject(getProject)
      lessonGroup.setUpdatedAt(new Date())
      val entities = CollectUtils.newHashSet()
      val lessons = entityDao.get(classOf[Lesson], lessonIds)
      for (lesson <- lessons) {
        if (null != lesson.getGroup) {
          lesson.getGroup.removeLesson(lesson)
          entities.add(lesson.getGroup)
        }
        lesson.setGroup(lessonGroup)
        entities.add(lesson)
      }
      if (lessonGroup.isTransient) {
        lessonGroup.setCreatedAt(lessonGroup.getUpdatedAt)
      } else {
        val lessons2 = lessonGroup.getLessons
        lessonGroup.clearLesson()
        for (lesson <- lessons2 if !entities.contains(lesson)) {
          lesson.setGroup(null)
          entities.add(lesson)
        }
      }
      entities.add(lessonGroup)
      lessonGroup.setLessons(CollectUtils.newHashSet(lessons))
      try {
        saveOrUpdate(entities)
        return redirect("search", "info.save.success")
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
    redirect("search", "info.save.failure")
  }

  def ajaxQuery(): String = {
    val departId = getLong("departId")
    if (null != departId) {
      val semester = putSemester(null)
      put("lessons", entityDao.get(classOf[Lesson], Array("semester", "teachDepart.id"), Array(semester, departId)))
    } else {
      put("lessons", Collections.emptyList())
    }
    forward()
  }

  def remove(): String = {
    val entityId = getLongId(getShortName)
    var entities: Collection[LessonGroup] = null
    if (null == entityId) {
      val entityIds = getLongIds(getShortName)
      entities = if (ArrayUtils.isNotEmpty(entityIds)) entityDao.get(classOf[LessonGroup], entityIds) else Collections.emptyList()
    } else {
      val entity = entityDao.get(classOf[LessonGroup], entityId)
      entities = Collections.singletonList(entity)
    }
    val updateEntities = CollectUtils.newArrayList()
    for (lessonGroup <- entities) {
      val lessons = lessonGroup.getLessons
      for (lesson <- lessons) {
        lesson.setGroup(null)
        updateEntities.add(lesson)
      }
    }
    val builder = Operation.saveOrUpdate(updateEntities)
    if (!entities.isEmpty) {
      builder.remove(entities)
    }
    try {
      entityDao.execute(builder)
    } catch {
      case e: Exception => {
        logger.info("removeAndForwad failure", e)
        return redirect("search", "info.delete.failure")
      }
    }
    redirect("search", "info.delete.success")
  }

  def buildPlanQuery(): OqlBuilder[MajorPlan] = {
    val builder = OqlBuilder.from(classOf[MajorPlan], "plan")
    populateConditions(builder)
    val departId = getLong("lessonGroup.teachDepart.id")
    if (null != departId) {
      builder.where("plan.program.department.id=:departId", departId)
    }
    if (Strings.isEmpty(Params.get(Order.ORDER_STR))) {
      builder.orderBy(new Order("plan.program.grade desc"))
    } else {
      builder.orderBy(Params.get(Order.ORDER_STR))
    }
    builder.limit(getPageLimit)
    builder
  }
}
