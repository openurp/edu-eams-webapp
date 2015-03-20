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



class EamsUserServiceImpl extends UserServiceImpl with EamsUserService {

  def createStdUser(creator: User, std: Student): User = {
    val user = get(std.code)
    if (null != user) {
      entityDao.saveOrUpdate(std)
      return user
    }
    val stdUser = Model.newInstance(classOf[User]).asInstanceOf[UserBean]
    stdUser.name=std.code
    stdUser.password=EncryptUtil.encode(User.DEFAULT_PASSWORD)
    stdUser.enabled=true
    stdUser.effectiveAt=std.enrollOn
    stdUser.creator=creator
    stdUser.fullname=std.name
    var email: String = null
    if (Strings.isEmpty(email)) email = "default@unknown.com"
    stdUser.mail=email
    saveOrUpdate(stdUser)
    stdUser
  }

  def createTeacherUser(creator: User, teacher: Teacher): User = {
    if (Strings.isEmpty(teacher.code)) {
      return null
    }
    val user = get(teacher.code)
    if (null != user) {
      entityDao.saveOrUpdate(teacher)
      return user
    }
    val teacherUser = Model.newInstance(classOf[User]).asInstanceOf[UserBean]
    teacherUser.name=teacher.code
    teacherUser.password=EncryptUtil.encode(User.DEFAULT_PASSWORD)
    teacherUser.enabled=true
    teacherUser.effectiveAt=new Date()
    teacherUser.fullname=teacher.name
    teacherUser.creator=creator
    var email: String = null
    if (Strings.isEmpty(email)) email = "default@unknown.com"
    teacherUser.mail=email
    saveOrUpdate(teacherUser)
    teacherUser
  }
}
