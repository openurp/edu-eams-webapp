package org.openurp.edu.eams.teach.grade.course.web.action


import java.util.Date



import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Semester
import org.openurp.edu.eams.core.StdPerson
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.core.service.StudentService
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.eams.teach.grade.model.StdGpa
import org.openurp.edu.eams.teach.grade.service.CourseGradeProvider
import org.openurp.edu.eams.teach.grade.service.GpaStatService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.AbstractStudentProjectSupportAction



class PersonAction extends AbstractStudentProjectSupportAction {

  var lessonGradeService: LessonGradeService = _

  var semesterService: SemesterService = _

  var gpaStatService: GpaStatService = _

  var studentService: StudentService = _

  var courseGradeProvider: CourseGradeProvider = _

  override def innerIndex(): String = {
    var acturalQueryStd = getLoginStudent
    val minorStudent = studentService.getMinorProjectStudent(acturalQueryStd.getPerson.asInstanceOf[StdPerson])
    if (minorStudent != null) {
      put("hasMinor", true)
    }
    val projectType = get("projectType")
    if ("MINOR" == projectType && minorStudent != null) {
      acturalQueryStd = minorStudent
    }
    putSemester(getProject)
    put("std", acturalQueryStd)
    forward()
  }

  def search(): String = {
    val semesterId = getInt("semesterId")
    var acturalQueryStd = getLoginStudent
    val minorStudent = studentService.getMinorProjectStudent(acturalQueryStd.getPerson.asInstanceOf[StdPerson])
    val projectType = get("projectType")
    if (minorStudent != null && "MINOR" == projectType) {
      acturalQueryStd = minorStudent
    }
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
    builder.where("courseGrade.std = :std", acturalQueryStd)
    builder.where("courseGrade.semester.id = :semesterId", semesterId)
    builder.where("courseGrade.status =:status", Grade.Status.PUBLISHED)
    val courseGrades = courseGradeProvider.getPublished(acturalQueryStd, Model.newInstance(classOf[Semester], 
      semesterId))
    put("grades", courseGrades)
    putGradeTypes()
    put("std", acturalQueryStd)
    forward()
  }

  private def putGradeTypes() {
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType], GradeTypeConstants.USUAL_ID, GradeTypeConstants.MIDDLE_ID, 
      GradeTypeConstants.END_ID, GradeTypeConstants.MAKEUP_ID, GradeTypeConstants.DELAY_ID, GradeTypeConstants.GA_ID))
  }

  def historyCourseGrade(): String = {
    var acturalQueryStd = getLoginStudent
    val minorStudent = studentService.getMinorProjectStudent(acturalQueryStd.getPerson.asInstanceOf[StdPerson])
    val projectType = get("projectType")
    if (minorStudent != null && "MINOR" == projectType) {
      acturalQueryStd = minorStudent
    }
    put("grades", courseGradeProvider.getPublished(acturalQueryStd))
    putGradeTypes()
    put("stdGpa", gpaStatService.statGpa(acturalQueryStd))
    forward()
  }

  def scorePrint(): String = {
    val MAX_COUNT = 15
    val SEMESTER_LIST = Collections.newBuffer[Any]("1", "2")
    val std = getLoginStudent
    val stdMap = Collections.newMap[Any]
    val stdSemesetrMap = Collections.newMap[Any]
    val stdSemesterNameMap = Collections.newMap[Any]
    val lineCountMap = Collections.newMap[Any]
    var maxLineCount = 1
    val builder = OqlBuilder.from(classOf[CourseGrade], "courseGradeSemester")
    builder.where("courseGradeSemester.std = :std", std)
    builder.select("courseGradeSemester.semester.id")
    builder.groupBy("courseGradeSemester.semester.id")
    builder.orderBy("courseGradeSemester.semester.id")
    val semesters = entityDao.search(builder)
    val semesterSchoolYear = Collections.newBuffer[Any]
    val semesterMap = Collections.newMap[Any]
    var itor = semesters.iterator()
    while (itor.hasNext) {
      val semesterId = itor.next().asInstanceOf[java.lang.Integer]
      val semester = entityDao.get(classOf[Semester], semesterId)
      val query = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
      query.where("courseGrade.semester =:semester", semester)
      query.where("courseGrade.std =:student", std)
      query.where("courseGrade.status =:status", Grade.Status.PUBLISHED)
      val courseGrades = entityDao.search(query)
      var courseMap = semesterMap.get(semester.getSchoolYear)
      if (courseMap == null) {
        courseMap = Collections.newMap[Any]
        courseMap.put(semester.getName, courseGrades)
        semesterMap.put(semester.getSchoolYear, courseMap)
      }
      courseMap.put(semester.getName, courseGrades)
      if (!semesterSchoolYear.contains(semester.getSchoolYear)) {
        semesterSchoolYear.add(semester.getSchoolYear)
      }
      if (courseGrades.size > maxLineCount) {
        maxLineCount = courseGrades.size
      }
    }
    stdSemesterNameMap.put(std.getCode, SEMESTER_LIST)
    stdSemesetrMap.put(std.getCode, semesterSchoolYear)
    stdMap.put(std.getCode, semesterMap)
    if (maxLineCount < MAX_COUNT) {
      maxLineCount = MAX_COUNT
    }
    lineCountMap.put(std.getCode, java.lang.Integer.valueOf(maxLineCount))
    val stdGpaMap = Collections.newMap[Any]
    val stdGpaBuilder = OqlBuilder.from(classOf[StdGpa], "stdGpa")
    stdGpaBuilder.where("stdGpa.std =:student", std)
    val stdGpas = entityDao.search(stdGpaBuilder)
    for (stdGpa <- stdGpas) {
      stdGpaMap.put(stdGpa.getStd.getCode, stdGpa)
    }
    put("std", std)
    put("stdMap", stdMap)
    put("nowDate", new Date())
    put("school", getProject.getSchool)
    if (stdGpaMap.size > 0) {
      put("stdGpaMap", stdGpaMap)
    }
    put("lineCountMap", lineCountMap)
    put("stdSemesetrMap", stdSemesetrMap)
    put("stdSemesterNameMap", stdSemesterNameMap)
    put("maxCount", MAX_COUNT)
    put("semesterName", SEMESTER_LIST)
    put("examModel", entityDao.get(classOf[ExamMode], ExamMode.NORMAL))
    forward()
  }
}
