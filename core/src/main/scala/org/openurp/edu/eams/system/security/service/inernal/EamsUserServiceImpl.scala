package org.openurp.edu.eams.system.security.service.inernal

import java.util.Date
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.model.UserBean
import org.beangle.security.blueprint.service.internal.UserServiceImpl
import org.beangle.security.codec.EncryptUtil
import org.openurp.edu.base.Student
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.system.security.EamsUserService

import scala.collection.JavaConversions._

class EamsUserServiceImpl extends UserServiceImpl with EamsUserService {

  def createStdUser(creator: User, std: Student): User = {
    val user = get(std.getCode)
    if (null != user) {
      entityDao.saveOrUpdate(std)
      return user
    }
    val stdUser = Model.newInstance(classOf[User]).asInstanceOf[UserBean]
    stdUser.setName(std.getCode)
    stdUser.setPassword(EncryptUtil.encode(User.DEFAULT_PASSWORD))
    stdUser.setEnabled(true)
    stdUser.setEffectiveAt(std.getEnrollOn)
    stdUser.setCreator(creator)
    stdUser.setFullname(std.getName)
    var email: String = null
    if (Strings.isEmpty(email)) email = "default@unknown.com"
    stdUser.setMail(email)
    saveOrUpdate(stdUser)
    stdUser
  }

  def createTeacherUser(creator: User, teacher: Teacher): User = {
    if (Strings.isEmpty(teacher.getCode)) {
      return null
    }
    val user = get(teacher.getCode)
    if (null != user) {
      entityDao.saveOrUpdate(teacher)
      return user
    }
    val teacherUser = Model.newInstance(classOf[User]).asInstanceOf[UserBean]
    teacherUser.setName(teacher.getCode)
    teacherUser.setPassword(EncryptUtil.encode(User.DEFAULT_PASSWORD))
    teacherUser.setEnabled(true)
    teacherUser.setEffectiveAt(new Date())
    teacherUser.setFullname(teacher.getName)
    teacherUser.setCreator(creator)
    var email: String = null
    if (Strings.isEmpty(email)) email = "default@unknown.com"
    teacherUser.setMail(email)
    saveOrUpdate(teacherUser)
    teacherUser
  }
}
