package org.openurp.edu.eams.teach.grade.lesson.service

import java.util.List
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.teach.Course
import org.openurp.edu.teach.grade.CourseGrade
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class CourseSegStat(@BeanProperty var course: Course, @BeanProperty var semester: Semester, courseGrades: List[CourseGrade])
    extends GradeSegStats {

  this.courseGrades = courseGrades

  def this(segs: Int) {
    super(segs)
  }
}
