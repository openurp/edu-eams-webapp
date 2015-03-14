package org.openurp.edu.eams.teach.exam

import java.util.Collection
import java.util.List
import java.util.Map
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamActivity
import org.openurp.edu.eams.teach.lesson.ExamTake
import org.openurp.edu.teach.lesson.Lesson

import scala.collection.JavaConversions._

trait ExamTakeService {

  def collisionStds(calendar: Semester, examType: ExamType, project: Project): Collection[_]

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
