package org.openurp.edu.eams.teach.grade.course.web.action

import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.grade.course.model.ScoreSection
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class StatAction extends SemesterSupportAction {

  def search(): String = {
    val departments = getDeparts
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    populateConditions(builder)
    builder.where("lesson.project =:project", getProject)
    if (CollectUtils.isEmpty(departments)) {
      builder.where("lesson is null")
    } else {
      builder.where("lesson.teachDepart in (:departments)", departments)
      if (null != getProject) {
        builder.where("lesson.project = :project", getProject)
      }
    }
    builder.limit(getPageLimit)
    builder.select("select distinct lesson.course")
    put("courses", entityDao.search(builder))
    forward()
  }

  def scoreSectionIndex(): String = {
    put("sections", scoreSectionSearchList())
    forward()
  }

  private def scoreSectionSearchList(): List[ScoreSection] = {
    val query = OqlBuilder.from(classOf[ScoreSection], "section")
    query.orderBy("section.fromScore desc")
    val sections = entityDao.search(query)
    sections
  }

  def scoreSectionSetting(): String = {
    val sections = entityDao.getAll(classOf[ScoreSection])
    val newSections = CollectUtils.newArrayList()
    var i = 1
    var j = i
    while (j <= getInt("count").intValue()) {
      val section = populateEntity(classOf[ScoreSection], "section" + i)
      if (null != section.getId && sections.contains(section)) {
        sections.remove(section)
      }
      newSections.add(section)
      i += 1
      j += 1
    }
    if (CollectUtils.isNotEmpty(newSections)) {
      entityDao.saveOrUpdate(newSections)
    }
    if (CollectUtils.isNotEmpty(sections)) {
      entityDao.remove(sections)
    }
    redirect("scoreSectionIndex", "info.action.success")
  }

  def statSetting(): String = {
    val departments = getDeparts
    val courseId = getLong("courseIds")
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    query.where("lesson.course.id =:courseId", courseId)
    if (null == getProject || CollectUtils.isEmpty(departments)) {
      query.where("lesson is null")
    } else {
      query.where("lesson.project =:project", getProject)
      query.where("lesson.teachDepart in (:departments)", departments)
    }
    query.select("distinct lesson.semester")
    put("semesters", entityDao.search(query))
    put("courseId", courseId)
    forward()
  }

  def stat(): String = {
    val courseId = getLong("courseId")
    val semesterIds = Strings.splitToInt(get("semesterIds"))
    val sections = scoreSectionSearchList()
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.course.id in (:courseId)", courseId)
    var hql = new StringBuilder()
    hql.append("courseGrade.lesson.semester.id, ")
    hql.append("count(*) ")
    for (i <- 0 until sections.size) {
      hql.append(", ")
      val section = sections.get(i)
      hql.append("sum(")
      hql.append("case when nvl(courseGrade.score, 0) between ")
        .append(section.getToScore)
      hql.append(" and ").append(section.getFromScore)
      if (i > 0) {
        hql.append(" and nvl(courseGrade.score, 0) != ").append(section.getFromScore)
      }
      hql.append(" then 1 else 0 end) ")
    }
    builder.where("courseGrade.lesson.semester.id in (:semesterIds)", semesterIds)
    builder.groupBy("courseGrade.lesson.semester.id")
    builder.orderBy("courseGrade.lesson.semester.id")
    builder.select(hql.toString)
    put("results1", entityDao.search(builder))
    val builderGrade = OqlBuilder.from(classOf[CourseGrade], "grade")
    builderGrade.where("grade.lesson.course.id in (:courseId)", courseId)
    hql = new StringBuilder()
    hql.append("count(*), ")
    hql.append("avg(grade.score), ")
    hql.append("max(grade.score), ")
    hql.append("min(grade.score) ")
    builderGrade.where("grade.lesson.semester.id in (:semesterIds)", semesterIds)
    builderGrade.select(hql.toString)
    put("results2", entityDao.search(builderGrade))
    put("sections", sections)
    put("course", entityDao.get(classOf[Course], courseId))
    put("semesters", entityDao.get(classOf[Semester], semesterIds))
    forward()
  }
}
