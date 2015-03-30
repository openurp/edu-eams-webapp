package org.openurp.edu.eams.teach.grade.service.stat

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Objects
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.grade.CourseGrade
import scala.collection.mutable.HashSet
import java.util.ArrayList
import org.beangle.commons.collection.Collections
import scala.collection.mutable.Buffer
import org.beangle.commons.bean.orderings.PropertyOrdering

class MultiStdGrade(var semester: Semester, grades: collection.Map[Student, Seq[CourseGrade]], var ratio: java.lang.Float) {

  private var adminClass: Adminclass = _

  var courses: Buffer[Course] = Collections.newBuffer[Course]

  var stdGrades: Buffer[StdGrade] = Collections.newBuffer[StdGrade]

  var extraGradeMap = Collections.newMap[String, Iterable[CourseGrade]]

  var maxDisplay: java.lang.Integer = new java.lang.Integer(courses.size + maxExtra)

  val gradesMap = Collections.newMap[java.lang.Long, StdGrade]

  val courseStdNumMap = Collections.newMap[Course, CourseStdNum]

  for ((key, value) <- grades) {
    val stdGrade = new StdGrade(key, value, null, null)
    gradesMap.put(key.id, stdGrade)
    for (grade <- value) {
      val courseStdNum = courseStdNumMap.get(grade.course).orNull.asInstanceOf[CourseStdNum]
      if (null == courseStdNum) {
        courseStdNumMap.put(grade.course, new CourseStdNum(grade.course, new java.lang.Integer(1)))
      } else {
        courseStdNum.count = (new java.lang.Integer(courseStdNum.count.intValue() + 1))
      }
    }
  }

  stdGrades ++= (gradesMap.values)

  val courseStdNums = Collections.newBuffer[CourseStdNum]
  courseStdNums ++= courseStdNumMap.values
  courseStdNums.sorted

  var maxStdCount = 0

  if (Collections.isNotEmpty(courseStdNums)) {
    maxStdCount = (courseStdNums(0)).asInstanceOf[CourseStdNum].count
      .intValue()
  }

  for (i <- 0 until courseStdNums.size) {
    val rank = courseStdNums(i).asInstanceOf[CourseStdNum]
    if (new java.lang.Float(rank.count.intValue()).floatValue() /
      maxStdCount >
      ratio.floatValue()) {
      courses += (rank.course)
    }
  }

  var maxExtra = 0

  var iter = stdGrades.iterator
  while (iter.hasNext) {
    val stdGrade = iter.next()
    var myExtra = 0
    val extraGrades = Collections.newBuffer[CourseGrade]
    val commonCourseSet = courses.toSet
    var iterator = stdGrade.grades.iterator
    while (iterator.hasNext) {
      val courseGrade = iterator.next()
      if (!commonCourseSet.contains(courseGrade.course)) {
        extraGrades += courseGrade
        myExtra += 1
      }
    }
    if (myExtra > maxExtra) {
      maxExtra = myExtra
    }
    if (!extraGrades.isEmpty) {
      extraGradeMap.put(stdGrade.std.id.toString, extraGrades)
    }
  }

  def getAdminclass(): Adminclass = adminClass

  def setAdminclass(adminClass: Adminclass) {
    this.adminClass = adminClass
  }

  def sortStdGrades(cmpWhat: String, isAsc: Boolean) {
    if (null != stdGrades) {
      val cmp = new PropertyOrdering(cmpWhat, isAsc)
      stdGrades.sorted(cmp)
    }
  }

  def getExtraCourseNum(): Int = {
    maxDisplay.intValue() - courses.size
  }
}

class CourseStdNum(course2: Course, var count: java.lang.Integer) extends Comparable[CourseStdNum] {

  var course: Course = course2

  def getCount(): java.lang.Integer = count

  def setCount(count: java.lang.Integer) {
    this.count = count
  }

  def getCourse(): Course = course

  def setCourse(course: Course) {
    this.course = course
  }

  override def compareTo(myClass: CourseStdNum): Int = {
    Objects.compareBuilder.add(myClass.count, this.count)
      .toComparison()
  }
}
