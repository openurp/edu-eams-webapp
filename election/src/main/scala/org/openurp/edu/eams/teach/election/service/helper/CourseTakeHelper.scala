package org.openurp.edu.eams.teach.election.service.helper

import java.util.Date
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseTakeBean



object CourseTakeHelper {

  def genCourseTake(lesson: Lesson, 
      std: Student, 
      courseTakeType: CourseTakeType, 
      electionMode: ElectionMode, 
      turn: java.lang.Integer, 
      date: Date): CourseTake = {
    val courseTake = new CourseTakeBean()
    courseTake.setLesson(lesson)
    courseTake.setStd(std)
    courseTake.setCourseTakeType(courseTakeType)
    courseTake.setElectionMode(electionMode)
    if (null != turn) courseTake.setRemark(String.valueOf(turn))
    courseTake.setCreatedAt(date)
    courseTake.setUpdatedAt(date)
    courseTake
  }
}
