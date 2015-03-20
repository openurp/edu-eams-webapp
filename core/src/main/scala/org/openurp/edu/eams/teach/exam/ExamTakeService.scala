package org.openurp.edu.eams.teach.exam




import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.code.ExamType



trait ExamTakeService {

  def collisionStds(calendar: Semester, examType: ExamType, project: Project): Iterable[_]

  def canApplyDelayExam(std: Student, take: ExamTake): Boolean

  def statTakeCountWithTurn(calendar: Semester, examType: ExamType): List[_]

  def statTakeCountInCourse(calendar: Semester, examType: ExamType): List[_]

  def isTakeExam(std: Student, 
      semester: Semester, 
      lesson: Lesson, 
      examType: ExamType): Boolean

  def getSeatNum(exam: ExamTake): java.lang.Integer

  def autoGenTakesByActivity(courseTakes: List[CourseTake], activity: ExamActivity): List[ExamTake]

  def getAbsentExamCount(activity: ExamActivity): Int

  def getAbsentExamCount(activities: List[ExamActivity], semesterId: java.lang.Integer, examTypeId: java.lang.Integer): Map[String, Integer]
}
