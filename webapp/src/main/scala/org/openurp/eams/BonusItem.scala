package org.openurp.eams

import scala.collection.mutable.ListBuffer
import org.beangle.data.model.bean.{ CodedBean, IntIdBean, NamedBean }
import org.openurp.base.Semester
import org.openurp.teach.code.StdLabel
import org.openurp.teach.core.Course
import org.openurp.teach.core.model.CourseBean
import scala.collection.mutable.Buffer

class BonusItem extends IntIdBean with CodedBean with NamedBean {

  /**年级**/
  var grade: String = _

  /**学生标签**/
  var stdLabel: StdLabel = _

  /**课程**/
  var courses: collection.mutable.Set[Course] = new collection.mutable.HashSet[Course]

  /**最大加分值**/
  var maxScore: Int = _

  /**开始学期**/
  var beginTime: Semester = _

  /**结束学期**/
  var endTime: Semester = _

}