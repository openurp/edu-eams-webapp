package org.openurp.edu.eams.teach.election.service.rule.election



import org.beangle.commons.collection.CollectUtils
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.grade.CourseGrade



class CourseGradePrepare extends ElectRulePrepare {

  var entityDao: EntityDao = _

  def prepare(context: PrepareContext) {
    if (context.isPreparedData(PreparedDataName.RETAKE_COURSES)) return
    val courseGradePassedMap = CollectUtils.newHashMap()
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.select("distinct grade.course.id, grade.passed")
      .where("grade.std = :std", context.getStudent)
      .where("grade.status = :published", Grade.Status.PUBLISHED)
    val results = entityDao.search(query)
    for (objects <- results) {
      val courseId = objects(0).asInstanceOf[java.lang.Long]
      val passed = objects(1).asInstanceOf[java.lang.Boolean]
      if (courseGradePassedMap.get(courseId) == null) {
        courseGradePassedMap.put(courseId, passed)
      } else {
        if (passed) {
          courseGradePassedMap.put(courseId, passed)
        }
      }
    }
    context.getState.getHisCourses.putAll(courseGradePassedMap)
    context.addPreparedDataName(PreparedDataName.RETAKE_COURSES)
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
