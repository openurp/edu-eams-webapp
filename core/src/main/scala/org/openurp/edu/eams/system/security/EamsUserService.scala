package org.openurp.edu.eams.system.security

import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.service.UserService
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher

import scala.collection.JavaConversions._

trait EamsUserService extends UserService {

  def createStdUser(creator: User, std: Student): User

  def createTeacherUser(creator: User, teacher: Teacher): User
}
