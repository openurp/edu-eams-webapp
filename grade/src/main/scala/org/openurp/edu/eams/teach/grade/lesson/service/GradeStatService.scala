package org.openurp.edu.eams.teach.grade.lesson.service


import org.openurp.base.Semester
import org.openurp.edu.base.Teacher
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.util.stat.FloatSegment



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
