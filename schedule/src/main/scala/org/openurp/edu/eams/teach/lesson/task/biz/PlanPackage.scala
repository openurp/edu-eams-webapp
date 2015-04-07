package org.openurp.edu.eams.teach.lesson.task.biz




import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.biz.comparator.LessonComparator
import org.openurp.edu.teach.plan.MajorPlan
import org.beangle.commons.collection.Collections
import scala.collection.mutable.HashSet
import java.util.ArrayList




class PlanPackage {

  
  var term: Int = _

  
  var plan: MajorPlan = _

  
  var groupPackages = Collections.newBuffer[CourseGroupPackage]

  
  var classPackages = Collections.newBuffer[AdminclassPackage]

  
  var otherClassPackage: AdminclassPackage = new AdminclassPackage()

  def getGroupPackage(courseType: CourseType): CourseGroupPackage = {
    groupPackages.find(_.courseGroup.courseType == courseType)
      .getOrElse(null)
  }

  def getLessons(): HashSet[Lesson] = {
//    val result = new TreeSet[Lesson](LessonComparator.COMPARATOR)
    val result = new HashSet[Lesson]
    for (pkg <- classPackages) {
      result++=(pkg.getLessons)
    }
    result++=(otherClassPackage.getLessons)
    result
  }

  def getCourseTypesOfPlan() = {
    val result = Collections.newBuffer[CourseType]
    for (pkg <- groupPackages) {
      result += (pkg.courseGroup.courseType)
    }
    for (classPackage <- classPackages; courseTypePackage <- classPackage.courseTypePackages if !result.contains(courseTypePackage.courseType)) {
      result +=(courseTypePackage.courseType)
    }
    for (courseTypePackage <- otherClassPackage.courseTypePackages if !result.contains(courseTypePackage.courseType)) {
      result += (courseTypePackage.courseType)
    }
    result
  }

  def getCourseTypesOfPackage = {
    val result = getCourseTypesOfPlan
    for (pkg <- classPackages; pkg1 <- pkg.courseTypePackages if !result.contains(pkg1.courseType)) {
      result+=(pkg1.courseType)
    }
    result
  }
}
