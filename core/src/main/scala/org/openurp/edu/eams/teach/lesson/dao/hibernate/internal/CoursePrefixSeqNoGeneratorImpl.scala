package org.openurp.edu.eams.teach.lesson.dao.hibernate.internal



import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Course
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import CoursePrefixSeqNoGeneratorImpl._




object CoursePrefixSeqNoGeneratorImpl {

  val initSeqNo = "01"

  private val tail_length = 2
}

class CoursePrefixSeqNoGeneratorImpl extends HibernateEntityDao with LessonSeqNoGenerator {

  
  var infix: String = "."

  def genLessonSeqNo(lesson: Lesson) {
    if (Strings.isNotEmpty(lesson.getNo)) {
      return
    }
    val prefix = getPrefix(lesson)
    synchronized (this) {
      val seqNos = getLessonNos(lesson.getProject, lesson.getSemester, lesson.getCourse)
      var newSeqNo = "00"
      for (seqNo <- seqNos) {
        if (gap(seqNo, newSeqNo) >= 2) {
          //break
        } else {
          newSeqNo = seqNo
        }
      }
      newSeqNo = rollUp(newSeqNo)
      lesson.setNo(prefix + leftPadding(newSeqNo))
    }
  }

  private def allocate(seqNos: List[String], count: Int): List[String] = {
    var newSeqNo = "00"
    var allocated = 0
    val allocatedSeqnos = CollectUtils.newArrayList()
    var iter = seqNos.iterator()
    while (iter.hasNext) {
      val seqNo = iter.next()
      if (gap(seqNo, newSeqNo) >= 2) {
        val emptySlots = gap(seqNo, newSeqNo) - 1
        for (i <- 0 until emptySlots) {
          newSeqNo = rollUp(newSeqNo)
          allocatedSeqnos.add(leftPadding(newSeqNo))
          allocated += 1
          if (allocated >= count) {
            //break
          }
        }
        if (allocated >= count) {
          //break
        }
      }
      newSeqNo = seqNo
    }
    while (allocated < count) {
      newSeqNo = rollUp(newSeqNo)
      allocatedSeqnos.add(leftPadding(newSeqNo))
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
      ac(onePos) += 1
    } else {
      ac(onePos) = '0'
      if (ac(tenPos) < '9') {
        ac(tenPos) += 1
      } else if (ac(tenPos) == '9') {
        ac(tenPos) = 'A'
      } else if (ac(tenPos) >= 'A') {
        ac(tenPos) += 1
      }
    }
    String.valueOf(ac)
  }

  def genLessonSeqNos(lessons: Iterable[Lesson]) {
    synchronized {
      if (lessons.isEmpty) return
      val courseLessons = CollectUtils.newHashMap()
      for (lesson <- lessons if Strings.isEmpty(lesson.getNo)) {
        var matches = courseLessons.get(lesson.getCourse)
        if (null == matches) {
          matches = new ArrayList[Lesson]()
          courseLessons.put(lesson.getCourse, matches)
        }
        matches.add(lesson)
      }
      for (course <- courseLessons.keySet) {
        val myLessons = courseLessons.get(course).asInstanceOf[Iterable[Lesson]]
        val firstLesson = myLessons.iterator().next()
        val allSeqNos = getLessonNos(firstLesson.getProject, firstLesson.getSemester, course)
        genLessonSeqNos(myLessons, getPrefix(firstLesson), allSeqNos)
      }
    }
  }

  protected def genLessonSeqNos(lessons: Iterable[Lesson], prefix: String, seqNos: List[String]) {
    val lessonIter = lessons.iterator()
    val newSeqNos = allocate(seqNos, lessons.size)
    var iter = newSeqNos.iterator()
    while (iter.hasNext) {
      val seqNo = iter.next()
      val lesson = lessonIter.next().asInstanceOf[Lesson]
      lesson.setNo(prefix + seqNo)
    }
  }

  private def leftPadding(newSeqNo: String): String = {
    Strings.repeat("0", tail_length - newSeqNo.length) + newSeqNo
  }

  protected def getPrefix(lesson: Lesson): String = {
    var courseCode = lesson.getCourse.getCode
    if (Strings.isEmpty(courseCode)) {
      courseCode = get(classOf[Course], lesson.getCourse.id).getCode
    }
    if (Strings.isBlank(infix)) courseCode else (courseCode + infix)
  }

  private def getLessonNos(project: Project, semester: Semester, course: Course): List[String] = {
    var courseCode = course.getCode
    if (Strings.isEmpty(courseCode)) {
      courseCode = get(classOf[Course], course.id).getCode
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
