package org.openurp.edu.eams.teach.lesson.task.web.action



import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Predicate
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Project
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.task.service.LessonStatService
import org.openurp.edu.eams.teach.lesson.task.util.TaskOfCourseType
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class LessonStatisticAction extends SemesterSupportAction {

  var lessonStatService: LessonStatService = _

  var courseLimitService: CourseLimitService = _

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    forward()
  }

  def restrictLessons(): String = {
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val project = entityDao.get(classOf[Project], getInt("project.id"))
    val courseTypes = baseCodeService.getCodes(classOf[CourseType])
    CollectionUtils.filter(courseTypes, new Predicate() {

      def evaluate(`object`: AnyRef): Boolean = {
        if (`object`.asInstanceOf[CourseType].getName.indexOf("限") != 
          -1) {
          return true
        }
        return false
      }
    })
    val taskOfCourseTypes = lessonStatService.getTaskOfCourseTypes(project, semester, getDataRealm, courseTypes)
    var orderBy = get("orderBy")
    if (Strings.isEmpty(orderBy)) {
      orderBy = "courseType.name"
    }
    Collections.sort(taskOfCourseTypes, new PropertyComparator(orderBy))
    put("taskOfCourseTypes", taskOfCourseTypes)
    forward()
  }

  def multiAdminclassesLessons(): String = {
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val project = entityDao.get(classOf[Project], getInt("project.id"))
    val query = OqlBuilder.from(classOf[Lesson], "lesson").select("distinct lesson")
      .join("lesson.teachClass.limitGroups", "lgroup")
      .join("lgroup.items", "litem")
      .where("litem.meta.id = :metaId", CourseLimitMetaEnum.ADMINCLASS.getMetaId)
      .where("litem.content like ',%,'")
      .where("lesson.project = :project", project)
      .where("lesson.semester = :semester", semester)
    restrictionHelper.applyRestriction(query)
    val lessons = entityDao.search(query)
    val classMap = new HashMap[Long, List[Adminclass]]()
    for (lesson <- lessons) {
      classMap.put(lesson.id, courseLimitService.extractAdminclasses(lesson.getTeachClass))
    }
    put("classMap", classMap)
    put("lessons", lessons)
    forward()
  }

  def guapaiLessons(): String = {
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val project = entityDao.get(classOf[Project], getInt("project.id"))
    val query = OqlBuilder.from(classOf[Lesson], "lesson").where("exists (select tag.id from lesson.tags tag where tag.id=:guaPai)", 
      LessonTag.PredefinedTags.GUAPAI.id)
    query.where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
    restrictionHelper.applyRestriction(query)
    put("tasks", entityDao.search(query))
    forward()
  }

  def setLessonStatService(lessonStatService: LessonStatService) {
    this.lessonStatService = lessonStatService
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }
}
