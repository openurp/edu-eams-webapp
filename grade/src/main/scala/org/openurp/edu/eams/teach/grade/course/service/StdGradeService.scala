package org.openurp.edu.eams.teach.grade.course.service


import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.ExamGrade



trait StdGradeService {

  def getStdByCode(stdCode: String, 
      project: Project, 
      departments: List[Department], 
      entityDao: EntityDao): Student

  def buildGradeTypeQuery(): OqlBuilder[GradeType]

  def buildGrade(grade: CourseGrade, gradeType: GradeType, markStyle: ScoreMarkStyle): ExamGrade

  def getStatus(lessonNo: String, 
      stdId: String, 
      semesterId: String, 
      entityDao: EntityDao): Array[Any]

  def checkStdGradeExists(std: Student, 
      semester: Semester, 
      course: Course, 
      project: Project): Boolean
}
