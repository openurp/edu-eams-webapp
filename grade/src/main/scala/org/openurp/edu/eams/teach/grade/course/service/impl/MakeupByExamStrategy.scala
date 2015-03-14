package org.openurp.edu.eams.teach.grade.course.service.impl

import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.grade.course.service.MakeupStdStrategy
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

class MakeupByExamStrategy extends BaseServiceImpl with MakeupStdStrategy {

  def getCourseTakes(lesson: Lesson): List[CourseTake] = {
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.lesson = :lesson", lesson).where(" exists (from " + classOf[ExamTake].getName + " et " + 
      " where et.std = take.std and et.lesson = take.lesson and et.examType.id in(:examTypeIds))", CollectUtils.newArrayList(ExamType.MAKEUP, 
      ExamType.DELAY))
    entityDao.search(query)
  }

  def getCourseTakeCounts(lessons: List[Lesson]): Map[Lesson, Number] = {
    if (lessons.isEmpty) return CollectUtils.newHashMap()

    val lessonMap = CollectUtils.newHashMap()
    for (lesson <- lessons) lessonMap.put(lesson.getId, lesson)
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.lesson in (:lessons)", lessons).where(" exists (from " + classOf[ExamTake].getName + " et " + 
      " where et.std = take.std and et.lesson = take.lesson and et.examType.id in(:examTypeIds))", CollectUtils.newArrayList(ExamType.MAKEUP, 
      ExamType.DELAY))
      .select("take.lesson.id,count(*)")
      .groupBy("take.lesson.id")
    val rs = entityDao.search(query)
    val counts = CollectUtils.newHashMap()
    for (obj <- rs) {
      val count = obj.asInstanceOf[Array[Any]]
      counts.put(lessonMap.get(count(0)), count(1).asInstanceOf[Number])
    }
    counts
  }

  def getLessonCondition(gradeTypeId: java.lang.Integer): String = {
    var examTypeId: java.lang.Integer = null
    if (gradeTypeId == GradeTypeConstants.MAKEUP_ID) examTypeId = ExamType.MAKEUP else if (gradeTypeId == GradeTypeConstants.DELAY_ID) examTypeId = ExamType.DELAY
    if (null == examTypeId) "" else "and exists(from " + classOf[ExamTake].getName + " et where et.lesson=lesson and et.examType.id =" + 
      examTypeId + 
      ")"
  }
}
