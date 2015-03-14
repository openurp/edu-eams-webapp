package org.openurp.edu.eams.teach.election.service

import java.util.Date
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.election.ElectionProfile

import scala.collection.JavaConversions._

trait FilterMessageService {

  def sendWithdrawMessage(project: Project, 
      semester: Semester, 
      profile: ElectionProfile, 
      lessonIds: Array[Long], 
      sender: User): Unit

  def sendWithdrawMessage(project: Project, 
      semester: Semester, 
      startAt: Date, 
      endAt: Date, 
      lessonIds: Array[Long], 
      sender: User): Unit
}
