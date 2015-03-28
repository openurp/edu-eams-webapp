package org.openurp.edu.eams.teach.lesson.dao.hibernate.internal

import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Course
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import CoursePrefixSeqNoGeneratorImpl._
import org.hibernate.SessionFactory
import scala.collection.mutable.Buffer

object CoursePrefixSeqNoGeneratorImpl {

  val initSeqNo = "01"

  private val tail_length = 2
}

class CoursePrefixSeqNoGeneratorImpl(sf: SessionFactory) extends HibernateEntityDao(sf) with LessonSeqNoGenerator {

  var infix: String = "."

  def genLessonSeqNo(lesson: Lesson) {
    if (Strings.isNotEmpty(lesson.no)) {
      return
    }
    val prefix = getPrefix(lesson)
    this.synchronized {
      val seqNos = getLessonNos(lesson.project, lesson.semester, lesson.course)
      var newSeqNo = "00"
      var break = false
      val iter = seqNos.iterator
      while (iter.hasNext && !break) {
        val seqNo = iter.next()
        if (gap(seqNo, newSeqNo) >= 2) {
          break = true
        } else {
          newSeqNo = seqNo
        }
      }
      newSeqNo = rollUp(newSeqNo)
      lesson.no = (prefix + leftPadding(newSeqNo))
    }
  }

  private def allocate(seqNos: List[String], count: Int): Seq[String] = {
    var newSeqNo = "00"
    var allocated = 0
    val allocatedSeqnos = Collections.newBuffer[String]
    var iter = seqNos.iterator
    var break = false
    while (iter.hasNext && !break) {
      val seqNo = iter.next()
      if (gap(seqNo, newSeqNo) >= 2) {
        val emptySlots = gap(seqNo, newSeqNo) - 1
        for (i <- 0 until emptySlots) {
          newSeqNo = rollUp(newSeqNo)
          allocatedSeqnos += leftPadding(newSeqNo)
          allocated += 1
          if (allocated >= count) {
            break = true
          }
        }
        if (allocated >= count) {
          break = true
        }
      }
      newSeqNo = seqNo
    }
    while (allocated < count) {
      newSeqNo = rollUp(newSeqNo)
      allocatedSeqnos += leftPadding(newSeqNo)
      allocated += 1
    }
    allocatedSeqnos
  }

  protected def gap(a: String, b: String): Int = {
    if (a == "A0" && b == "99") {
      return 1
    }
    val ac = a.toCharArray()
    val bc = b.toCharArray()
    ac(0) * 10 + ac(1) - (bc(0) * 10 + bc(1))
  }

  protected def rollUp(a: String): String = {
    val tenPos = 0
    val onePos = 1
    val ac = a.toCharArray()
    if (ac(onePos) < '9') {
      ac(onePos) = (ac(onePos) + 1).asInstanceOf[Char]
    } else {
      ac(onePos) = '0'
      if (ac(tenPos) < '9') {
        ac(tenPos) = (ac(tenPos) + 1).asInstanceOf[Char]
      } else if (ac(tenPos) == '9') {
        ac(tenPos) = 'A'
      } else if (ac(tenPos) >= 'A') {
        ac(tenPos) = (ac(tenPos) + 1).asInstanceOf[Char]
      }
    }
    String.valueOf(ac)
  }

  def genLessonSeqNos(lessons: Iterable[Lesson]) {
    synchronized {
      if (lessons.isEmpty) return
      val courseLessons = Collections.newMap[Course, Buffer[Lesson]]
      for (lesson <- lessons if Strings.isEmpty(lesson.no)) {
        var matches = courseLessons.get(lesson.course).orNull
        if (null == matches) {
          matches = Collections.newBuffer[Lesson]
          courseLessons.put(lesson.course, matches)
        }
        matches += (lesson)
      }
      for (course <- courseLessons.keySet) {
        val myLessons = courseLessons.get(course).asInstanceOf[Iterable[Lesson]]
        val firstLesson = myLessons.iterator.next()
        val allSeqNos = getLessonNos(firstLesson.project, firstLesson.semester, course)
        genLessonSeqNos(myLessons, getPrefix(firstLesson), allSeqNos)
      }
    }
  }

  protected def genLessonSeqNos(lessons: Iterable[Lesson], prefix: String, seqNos: List[String]) {
    val lessonIter = lessons.iterator
    val newSeqNos = allocate(seqNos, lessons.size)
    var iter = newSeqNos.iterator
    while (iter.hasNext) {
      val seqNo = iter.next()
      val lesson = lessonIter.next().asInstanceOf[Lesson]
      lesson.no = (prefix + seqNo)
    }
  }

  private def leftPadding(newSeqNo: String): String = {
    Strings.repeat("0", tail_length - newSeqNo.length) + newSeqNo
  }

  protected def getPrefix(lesson: Lesson): String = {
    var courseCode = lesson.course.code
    if (Strings.isEmpty(courseCode)) {
      courseCode = get(classOf[Course], lesson.course.id).code
    }
    if (Strings.isBlank(infix)) courseCode else (courseCode + infix)
  }

  private def getLessonNos(project: Project, semester: Semester, course: Course): List[String] = {
    var courseCode = course.code
    if (Strings.isEmpty(courseCode)) {
      courseCode = get(classOf[Course], course.id).code
    }
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.project = :project and lesson.semester=:semster", project, semester)
    builder.where("lesson.course=:course and lesson.no is not null", course)
    builder.orderBy("lesson.no")
    builder.select("substr(lesson.no," +
      (courseCode.length + 1 + (if (null == infix) 0 else infix.length)) +
      ")")
    search(builder).asInstanceOf[List[String]]
  }
}
