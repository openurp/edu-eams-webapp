package org.openurp.edu.eams.teach.lesson.dao.hibernate.internal

import java.text.MessageFormat
import java.util.ArrayList
import java.util.Collection
import java.util.Iterator
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.orm.hibernate.HibernateEntityDao
import org.openurp.base.Semester
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.dao.LessonSeqNoGenerator
import LessonSeqNoGeneratorImpl._

import scala.collection.JavaConversions._

object LessonSeqNoGeneratorImpl {

  val initSeqNo = "0001"
}

class LessonSeqNoGeneratorImpl extends HibernateEntityDao with LessonSeqNoGenerator {

  def genLessonSeqNo(lesson: Lesson) {
    if (Strings.isNotEmpty(lesson.getNo)) {
      return
    }
    synchronized (this) {
      val hql = MessageFormat.format("select no from org.openurp.edu.teach.lesson.Lesson lesson where lesson.semester.id={0} and lesson.project.id={1} order by no", 
        lesson.getSemester.getId.toString, lesson.getProject.getId.toString)
      val lessonNos = search(hql)
      var newNo = 0
      for (seqNo <- lessonNos) {
        if (seqNo.matches(".*[^\\d]+.*")) {
          //continue
        }
        if (Numbers.toInt(seqNo) - newNo >= 2) {
          //break
        } else {
          newNo = Numbers.toInt(seqNo)
        }
      }
      newNo += 1
      lesson.setNo(Strings.repeat("0", 4 - String.valueOf(newNo).length) + 
        newNo)
    }
  }

  def genLessonSeqNos(lessons: Collection[Lesson]) {
    val semesterTasks = CollectUtils.newHashMap()
    for (lesson <- lessons if Strings.isEmpty(lesson.getNo)) {
      var matches = semesterTasks.get(lesson.getSemester)
      if (null == matches) {
        matches = new ArrayList[Lesson]()
        semesterTasks.put(lesson.getSemester, matches)
      }
      matches.add(lesson)
    }
    var iter = semesterTasks.keySet.iterator()
    while (iter.hasNext) {
      val semester = iter.next()
      genLessonSeqNos(semester, semesterTasks.get(semester))
    }
  }

  private def genLessonSeqNos(semester: Semester, tasks: Collection[_]) {
    if (tasks.isEmpty) {
      return
    }
    synchronized (this) {
      val iter1 = tasks.iterator()
      var projectId: java.lang.Integer = null
      if (iter1.hasNext) {
        projectId = iter1.next().asInstanceOf[Lesson].getProject.getId
      }
      val hql = MessageFormat.format("select no from org.openurp.edu.teach.lesson.Lesson lesson where lesson.semester.id={0} and lesson.project.id={1} order by no", 
        semester.getId.toString, projectId.toString)
      val allSeqNos = search(hql)
      var newSeqNo = 0
      var seq = 0
      var allocated = 0
      val taskIter = tasks.iterator()
      var iter = allSeqNos.iterator()
      while (iter.hasNext) {
        val seqNo = iter.next().asInstanceOf[String]
        seq = Numbers.toInt(seqNo)
        if ((seq - newSeqNo >= 2)) {
          val gap = seq - newSeqNo - 1
          for (i <- 0 until gap) {
            allocated += 1
            newSeqNo += 1
            val task = taskIter.next().asInstanceOf[Lesson]
            task.setNo(Strings.repeat("0", 4 - String.valueOf(newSeqNo).length) + 
              newSeqNo)
            if (allocated >= tasks.size) //break
          }
          if (allocated >= tasks.size) //break
        }
        newSeqNo = seq
      }
      while (allocated < tasks.size) {
        newSeqNo += 1
        allocated += 1
        val task = taskIter.next().asInstanceOf[Lesson]
        task.setNo(Strings.repeat("0", 4 - String.valueOf(newSeqNo).length) + 
          newSeqNo)
      }
    }
  }
}
