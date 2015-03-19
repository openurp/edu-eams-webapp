package org.openurp.edu.eams.teach.election.dao


import org.beangle.commons.dao.EntityDao
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson



trait ElectionDao {

  def saveElection(std: Student, 
      task: Lesson, 
      takeType: CourseTakeType, 
      state: ElectState, 
      checkMaxLimit: Boolean): Int

  def saveElection(courseTake: CourseTake, checkMaxLimit: Boolean): Int

  def updatePitchOn(task: Lesson, stdIds: Iterable[Long], isPitchOn: java.lang.Boolean): Unit

  def updateStdCount(sql: String, lessonId: java.lang.Long): Int

  def updateStdCount(sql: String, lessonId: java.lang.Long, minLimit: java.lang.Integer): Int

  def removeElection(courseTake: CourseTake, updateStdCount: Boolean): Int

  def removeElection(lesson: Lesson, state: ElectState): Int

  def removeAllElection(task: Lesson, semesterId: java.lang.Integer, stdId: java.lang.Long): Int

  def getEntityDao(): EntityDao
}
