package org.openurp.edu.eams.teach.grade.lesson.service

import java.util.List
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.util.stat.FloatSegment

import scala.collection.JavaConversions._

trait GradeStatService {

  def statTask(lessonIdSeq: String, 
      scoreSegments: List[FloatSegment], 
      gradeTypes: List[GradeType], 
      teacher: Teacher): List[LessonSegStat]

  def statCourse(courses: List[Course], 
      scoreSegments: List[FloatSegment], 
      gradeTypes: List[GradeType], 
      semester: Semester): List[CourseSegStat]
}
