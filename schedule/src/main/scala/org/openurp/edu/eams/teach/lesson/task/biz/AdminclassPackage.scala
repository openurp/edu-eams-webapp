package org.openurp.edu.eams.teach.lesson.task.biz

import java.util.ArrayList
import java.util.List
import java.util.Set
import java.util.TreeSet
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.task.biz.comparator.LessonComparator
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class AdminclassPackage {

  @BeanProperty
  var adminclass: Adminclass = _

  @BeanProperty
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
