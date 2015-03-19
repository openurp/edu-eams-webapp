package org.openurp.edu.eams.teach.grade.lesson.service.impl




import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.lesson.service.CourseSegStat
import org.openurp.edu.eams.teach.grade.lesson.service.GradeStatService
import org.openurp.edu.eams.teach.grade.lesson.service.LessonSegStat
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.util.stat.FloatSegment



class GradeStatServiceImpl extends BaseServiceImpl with GradeStatService {

  def statTask(lessonIdSeq: String, 
      scoreSegments: List[FloatSegment], 
      gradeTypes: List[GradeType], 
      teacher: Teacher): List[LessonSegStat] = {
    val tasks = entityDao.get(classOf[Lesson], Strings.splitToLong(lessonIdSeq))
    val stats = CollectUtils.newArrayList()
    var iter = tasks.iterator()
    while (iter.hasNext) {
      val lesson = iter.next()
      if (null == teacher || lesson.getTeachers.contains(teacher)) {
        val grades = getGrades(lesson)
        val stat = new LessonSegStat(lesson, teacher, grades)
        stat.setScoreSegments(scoreSegments)
        stat.stat(gradeTypes)
        stats.add(stat)
      }
    }
    stats
  }

  private def getGrades(lesson: Lesson): List[CourseGrade] = {
    val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    query.where("courseGrade.lesson = :lesson", lesson)
    entityDao.search(query)
  }

  def statCourse(courses: List[Course], 
      scoreSegments: List[FloatSegment], 
      gradeTypes: List[GradeType], 
      semester: Semester): List[CourseSegStat] = {
    val stats = CollectUtils.newArrayList()
    var iter = courses.iterator()
    while (iter.hasNext) {
      val course = iter.next()
      val stat = new CourseSegStat(course, semester, null)
      stat.setScoreSegments(scoreSegments)
      stat.stat(gradeTypes)
      stats.add(stat)
    }
    stats
  }
}
