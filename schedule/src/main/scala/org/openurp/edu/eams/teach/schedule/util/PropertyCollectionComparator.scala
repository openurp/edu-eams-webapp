package org.openurp.edu.eams.teach.schedule.util

import java.util.Comparator

import org.openurp.edu.teach.lesson.Lesson
import PropertyCollectionComparator._




object PropertyCollectionComparator {

  object ArrangeOrder extends Enumeration {

    val TEACHER = new ArrangeOrder("按教师排序")

    val LESSONNO = new ArrangeOrder("按课程序号排序")

    val COURSE = new ArrangeOrder("按课程名称排序")

    class ArrangeOrder private () extends Val {

      
      var fullName: String = _

      private def this(fullName: String) {
        this()
        this.fullName = fullName
      }

      def getEngName(): String = this.name()
    }

    implicit def convertValue(v: Value): ArrangeOrder = v.asInstanceOf[ArrangeOrder]
  }
}

class PropertyCollectionComparator[T] extends Comparator[T]() {

  
  var asc: Boolean = true

  
  var arrangeOrder: ArrangeOrder = ArrangeOrder.TEACHER

  
  var pinyinComparator: PinyinComparator = new PinyinComparator(asc)

  private def genStr(lessonProperties: Seq[_]): String = {
    var cmpStr = ""
    try {
      if (!lessonProperties.isEmpty) {
        cmpStr += PropertyUtils.getProperty(lessonProperties(0), "name")
      }
    } catch {
      case e: Exception => return null
    }
    cmpStr
  }

  def this(arrangeOrder: ArrangeOrder, asc: Boolean) {
    this()
    this.asc = asc
    this.arrangeOrder = arrangeOrder
  }

  def compare(arg0: AnyRef, arg1: AnyRef): Int = {
    var what0: String = null
    var what1: String = null
    val lesson0 = arg0.asInstanceOf[Lesson]
    val lesson1 = arg1.asInstanceOf[Lesson]
    this.arrangeOrder match {
      case TEACHER => 
        what0 = genStr(lesson0.teachers)
        what1 = genStr(lesson1.teachers)

    }
    (if (asc) 1 else -1) * (pinyinComparator.compare(what0, what1))
  }
}
