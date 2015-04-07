package org.openurp.edu.eams.teach.lesson.task.biz




import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.biz.comparator.LessonComparator
import java.util.Arrays.ArrayList
import org.beangle.commons.collection.Collections




class AdminclassPackage {

  
  var adminclass: Adminclass = _

  
  var courseTypePackages = Collections.newBuffer[CourseTypePackage]

  def getCourseTypePackage(courseType: CourseType): CourseTypePackage = {
    courseTypePackages.find(_.courseType == courseType)
      .getOrElse(null)
  }

  def this(adminclass: Adminclass) {
    this()
    this.adminclass = adminclass
  }

  def getLessons(): collection.mutable.Set[Lesson] = {
//		  val result = new TreeSet[Lesson](LessonComparator.COMPARATOR)
    val result = Collections.newSet[Lesson]
    for (pkg <- courseTypePackages) {
      result++=(pkg.lessons)
    }
    result
  }
}
