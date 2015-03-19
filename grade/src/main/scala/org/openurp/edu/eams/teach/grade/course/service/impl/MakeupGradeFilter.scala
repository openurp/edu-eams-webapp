package org.openurp.edu.eams.teach.grade.course.service.impl

import org.openurp.edu.eams.teach.lesson.GradeTypeConstants.GA_ID
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants.MAKEUP_ID

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.metadata.Model
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.eams.teach.grade.service.impl.GradeFilter
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.ExamGrade



class MakeupGradeFilter extends GradeFilter {

  private var gradeRateService: GradeRateService = _

  def filter(grades: List[CourseGrade]): List[CourseGrade] = {
    val gradeList = CollectUtils.newArrayList()
    for (courseGrade <- grades) {
      var finded = false
      for (examGrade <- courseGrade.getExamGrades) {
        if (null == examGrade.gradeType) {
          //continue
        }
        if (examGrade.gradeType.id.longValue() == GA_ID.longValue() || 
          examGrade.gradeType.id.longValue() == MAKEUP_ID.longValue()) {
          val newGrade = Model.newInstance(classOf[CourseGrade])
          newGrade.setStd(courseGrade.getStd)
          newGrade.setSemester(courseGrade.getSemester)
          newGrade.setLesson(courseGrade.getLesson)
          newGrade.setCourse(courseGrade.getCourse)
          newGrade.setCourseType(courseGrade.getCourseType)
          newGrade.setLessonNo(courseGrade.getLessonNo)
          newGrade.setCourseType(courseGrade.getCourseType)
          newGrade.setCourseTakeType(courseGrade.getCourseTakeType)
          newGrade.setScore(examGrade.getScore)
          newGrade.setPassed(examGrade.isPassed)
          newGrade.setMarkStyle(examGrade.getMarkStyle)
          newGrade.setGp(gradeRateService.calcGp(newGrade))
          finded = true
          gradeList.add(newGrade)
        }
      }
      if (!finded) {
        gradeList.add(courseGrade)
      }
    }
    gradeList
  }

  def setGradeRateService(gradeRateService: GradeRateService) {
    this.gradeRateService = gradeRateService
  }
}
