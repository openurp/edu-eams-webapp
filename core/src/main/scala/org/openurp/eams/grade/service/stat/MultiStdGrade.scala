package org.openurp.eams.grade.service.stat

import java.util.ArrayList
import java.util.Collections
import java.util.HashSet
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Objects
import com.ekingstar.eams.base.Semester
import com.ekingstar.eams.core.Adminclass
import com.ekingstar.eams.core.Student
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.lesson.CourseGrade
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

/**
 * 多名学生的成绩打印模型<br>
 * 通常以一个班级为单位
 *
 * @author chaostone
 */
class MultiStdGrade( var semester: Semester, grades: Map[Student, Iterable[CourseGrade]],  var ratio: java.lang.Float)
    {

  private var adminClass: Adminclass = _

  
  var courses = new collection.mutable.ListBuffer[Course]

  /**
   * @see StdGrade
   */
  
  var stdGrades: List[StdGrade] = new ArrayList[StdGrade](gradesMap.values)

  
  var extraGradeMap: Map[String, List[CourseGrade]] = CollectUtils.newHashMap()

  
  var maxDisplay: java.lang.Integer = new java.lang.Integer(courses.size + maxExtra)

  if (grades.isEmpty) {
    return
  }

  val gradesMap = CollectUtils.newHashMap()

  val courseStdNumMap = CollectUtils.newHashMap()

  for ((key, value) <- grades) {
    val stdGrade = new StdGrade(key, value, null, null)
    gradesMap.put(key.getId, stdGrade)
    for (grade <- value) {
      val courseStdNum = courseStdNumMap.get(grade.course).asInstanceOf[CourseStdNum]
      if (null == courseStdNum) {
        courseStdNumMap.put(grade.course, new CourseStdNum(grade.course, new java.lang.Integer(1)))
      } else {
        courseStdNum.setCount(new java.lang.Integer(courseStdNum.getCount.intValue() + 1))
      }
    }
  }

  val courseStdNums = new ArrayList[CourseStdNum](courseStdNumMap.values)

  Collections.sort(courseStdNums)

  var maxStdCount = 0

  if (CollectUtils.isNotEmpty(courseStdNums)) {
    maxStdCount = (courseStdNums.get(0)).asInstanceOf[CourseStdNum].getCount
      .intValue()
  }

  for (i <- 0 until courseStdNums.size) {
    val rank = courseStdNums.get(i).asInstanceOf[CourseStdNum]
    if (new java.lang.Float(rank.getCount.intValue()).floatValue() / 
      maxStdCount > 
      ratio.floatValue()) {
      courses.add(rank.getCourse)
    }
  }

  var maxExtra = 0

  var iter = stdGrades.iterator()
  while (iter.hasNext) {
    val stdGrade = iter.next()
    var myExtra = 0
    val extraGrades = CollectUtils.newArrayList()
    val commonCourseSet = new HashSet[Course](courses)
    var iterator = stdGrade.getGrades.iterator()
    while (iterator.hasNext) {
      val courseGrade = iterator.next()
      if (!commonCourseSet.contains(coursegrade.course)) {
        extraGrades.add(courseGrade)
        myExtra += 1
      }
    }
    if (myExtra > maxExtra) {
      maxExtra = myExtra
    }
    if (!extraGrades.isEmpty) {
      extraGradeMap.put(stdGrade.getStd.getId.toString, extraGrades)
    }
  }

  def getAdminclass(): Adminclass = adminClass

  def setAdminclass(adminClass: Adminclass) {
    this.adminClass = adminClass
  }

  def sortStdGrades(cmpWhat: String, isAsc: Boolean) {
    if (null != stdGrades) {
      val cmp = new PropertyComparator(cmpWhat, isAsc)
      Collections.sort(stdGrades, cmp)
    }
  }

  /**
   * 返回超出显示课程数量之外的课程数
   *
   * @return
   */
  def getExtraCourseNum(): Int = {
    getMaxDisplay.intValue() - getCourses.size
  }
}

/**
 * 课程对应的学生人数
 *
 * @author chaostone
 */
class CourseStdNum(course2: Course, var count: java.lang.Integer) extends Comparable[_] {

  var course: Course = course2

  def getCount(): java.lang.Integer = count

  def setCount(count: java.lang.Integer) {
    this.count = count
  }

  def getCourse(): Course = course

  def setCourse(course: Course) {
    this.course = course
  }

  /**
   * @see java.lang.Comparable#compareTo(Object)
   */
  def compareTo(`object`: AnyRef): Int = {
    val myClass = `object`.asInstanceOf[CourseStdNum]
    Objects.compareBuilder().add(myClass.count, this.count)
      .toComparison()
  }
}
