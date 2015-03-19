package org.openurp.edu.eams.teach.lesson.task.biz




import java.util.TreeSet
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.biz.comparator.LessonComparator




class AdminclassPackage {

  
  var adminclass: Adminclass = _

  
  var courseTypePackages: List[CourseTypePackage] = new ArrayList[CourseTypePackage]()

  def getCourseTypePackage(courseType: CourseType): CourseTypePackage = {
    courseTypePackages.find(_.getCourseType == courseType)
      .getOrElse(null)
  }

  def this(adminclass: Adminclass) {
    super()
    this.adminclass = adminclass
  }

  def getLessons(): Set[Lesson] = {
    val result = new TreeSet[Lesson](LessonComparator.COMPARATOR)
    for (pkg <- courseTypePackages) {
      result.addAll(pkg.getLessons)
    }
    result
  }
}
