package org.openurp.edu.eams.teach.grade.course.service.impl



import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.grade.course.service.MakeupStdStrategy
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson



class MakeupByExamStrategy extends BaseServiceImpl with MakeupStdStrategy {

  def getCourseTakes(lesson: Lesson): List[CourseTake] = {
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.lesson = :lesson", lesson).where(" exists (from " + classOf[ExamTake].getName + " et " + 
      " where et.std = take.std and et.lesson = take.lesson and et.examType.id in(:examTypeIds))", Collections.newBuffer[Any](ExamType.MAKEUP, 
      ExamType.DELAY))
    entityDao.search(query)
  }

  def getCourseTakeCounts(lessons: List[Lesson]): Map[Lesson, Number] = {
    if (lessons.isEmpty) return Collections.newMap[Any]

    val lessonMap = Collections.newMap[Any]
    for (lesson <- lessons) lessonMap.put(lesson.id, lesson)
    val query = OqlBuilder.from(classOf[CourseTake], "take")
    query.where("take.lesson in (:lessons)", lessons).where(" exists (from " + classOf[ExamTake].getName + " et " + 
      " where et.std = take.std and et.lesson = take.lesson and et.examType.id in(:examTypeIds))", Collections.newBuffer[Any](ExamType.MAKEUP, 
      ExamType.DELAY))
      .select("take.lesson.id,count(*)")
      .groupBy("take.lesson.id")
    val rs = entityDao.search(query)
    val counts = Collections.newMap[Any]
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
