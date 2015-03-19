package org.openurp.edu.eams.teach.grade.lesson.service


import org.openurp.base.Semester
import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.CourseGrade




class CourseSegStat( var course: Course,  var semester: Semester, courseGrades: List[CourseGrade])
    extends GradeSegStats {

  this.courseGrades = courseGrades

  def this(segs: Int) {
    super(segs)
  }
}
