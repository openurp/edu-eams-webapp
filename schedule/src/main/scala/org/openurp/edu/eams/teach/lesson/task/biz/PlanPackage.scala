package org.openurp.edu.eams.teach.lesson.task.biz




import java.util.TreeSet
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.biz.comparator.LessonComparator
import org.openurp.edu.teach.plan.MajorPlan




class PlanPackage {

  
  var term: Int = _

  
  var plan: MajorPlan = _

  
  var groupPackages: List[CourseGroupPackage] = new ArrayList[CourseGroupPackage]()

  
  var classPackages: List[AdminclassPackage] = new ArrayList[AdminclassPackage]()

  
  var otherClassPackage: AdminclassPackage = new AdminclassPackage()

  def getGroupPackage(courseType: CourseType): CourseGroupPackage = {
    groupPackages.find(_.getCourseGroup.getCourseType == courseType)
      .getOrElse(null)
  }

  def getLessons(): List[Lesson] = {
    val result = new TreeSet[Lesson](LessonComparator.COMPARATOR)
    for (pkg <- classPackages) {
      result.addAll(pkg.getLessons)
    }
    result.addAll(otherClassPackage.getLessons)
    new ArrayList[Lesson](result)
  }

  def getCourseTypesOfPlan(): List[CourseType] = {
    val result = new ArrayList[CourseType]()
    for (pkg <- groupPackages) {
      result.add(pkg.getCourseGroup.getCourseType)
    }
    for (classPackage <- classPackages; courseTypePackage <- classPackage.getCourseTypePackages if !result.contains(courseTypePackage.getCourseType)) {
      result.add(courseTypePackage.getCourseType)
    }
    for (courseTypePackage <- otherClassPackage.getCourseTypePackages if !result.contains(courseTypePackage.getCourseType)) {
      result.add(courseTypePackage.getCourseType)
    }
    result
  }

  def getCourseTypesOfPackage(): List[CourseType] = {
    val result = getCourseTypesOfPlan
    for (pkg <- classPackages; pkg1 <- pkg.getCourseTypePackages if !result.contains(pkg1.getCourseType)) {
      result.add(pkg1.getCourseType)
    }
    result
  }
}
