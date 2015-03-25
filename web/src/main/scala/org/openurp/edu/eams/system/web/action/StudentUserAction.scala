package org.openurp.edu.eams.system.web.action






import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.security.blueprint.model.UserBean
import org.beangle.struts2.helper.QueryHelper
import org.openurp.edu.base.Student
import org.openurp.edu.eams.core.service.StudentService
import org.openurp.edu.eams.system.security.EamsUserService
import org.openurp.edu.eams.util.DataRealmUtils
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class StudentUserAction extends RestrictionSupportAction {

  var eamsUserService: EamsUserService = _

  var studentService: StudentService = _

  def search(): String = {
    val entityQuery = OqlBuilder.from(classOf[Student], "student")
    QueryHelper.populateConditions(entityQuery, "student.type.id")
    entityQuery.join("left", "student.major", "major")
    entityQuery.limit(getPageLimit)
    entityQuery.orderBy(Order.parse(get("orderBy")))
    val stdTypeId = getLong("student.stdType.id")
    DataRealmUtils.addDataRealms(entityQuery, Array("student.type.id", "student.department.id"), restrictionHelper.getDataRealmsWith(stdTypeId))
    val stds = entityDao.search(entityQuery)
    val stdUserMap = new HashMap()
    var iter = stds.iterator()
    while (iter.hasNext) {
      val std = iter.next().asInstanceOf[Student]
      val user = eamsUserService.get(std.getCode)
      if (null != user) {
        stdUserMap.put(std.id.toString, user)
      }
    }
    put("stds", stds)
    put("stdUserMap", stdUserMap)
    forward()
  }

  def activate(): String = {
    val stdCodes = get("stdCodes")
    val activate = getBoolean("isActivate")
    val isActivate = if ((activate == null)) false else activate.booleanValue()
    try {
      val users = entityDao.get(classOf[User], "name", Strings.split(stdCodes, ","))
      var iter = users.iterator()
      while (iter.hasNext) {
        val user = iter.next().asInstanceOf[User]
        user.setEnabled(if (isActivate) true else false)
      }
      entityDao.saveOrUpdate(users)
    } catch {
      case e: Exception => {
        logHelper.info("Failure in alert status stdUser nos:" + stdCodes, e)
        return forwardError("error.occurred")
      }
    }
    var msg = "info.activate.success"
    if (!isActivate) msg = "info.unactivate.success"
    redirect("search", msg)
  }

  def index(): String = {
    put("departmentList", getColleges)
    put("stdTypeList", getStdTypes)
    forward()
  }

  def info(): String = {
    val userId = getLong("userId")
    var user: User = null
    if (null != userId && userId.intValue() != 0) user = entityDao.get(classOf[User], userId).asInstanceOf[User] else {
      return forwardError("error.model.notExist")
    }
    put("user", user)
    forward()
  }

  def edit(): String = {
    val stdCode = get("stdCode")
    var stdUser: User = null
    var std: Student = null
    if (Strings.isNotEmpty(stdCode)) {
      std = studentService.getStudent(stdCode)
      if (null == std) return forwardError(Array("entity.student", "error.model.notExsits"))
      stdUser = eamsUserService.get(stdCode)
      if (null == stdUser) {
        val curUser = entityDao.get(classOf[User], getUserId)
        stdUser = eamsUserService.createStdUser(curUser, std)
      }
    }
    put("user", stdUser)
    forward()
  }

  def save(): String = {
    val userId = getLong("user.id")
    val savedUser = eamsUserService.get(userId).asInstanceOf[UserBean]
    if (null == savedUser) return forwardError(Array("entity.student", "error.model.notExsits"))
    savedUser.setMail(get("user.email"))
    savedUser.setPassword(get("user.password"))
    try {
      logHelper.info("Update stdUser acount:" + savedUser.getName)
      eamsUserService.saveOrUpdate(savedUser)
    } catch {
      case e: Exception => {
        logHelper.info("Failure in Update stdUser :" + savedUser.getName)
        redirect("search", "info.save.failure")
      }
    }
    redirect("search", "info.save.success")
  }

  def add(): String = {
    val stdCodeSeq = get("stdCodes")
    if (Strings.isEmpty(stdCodeSeq)) return forwardError(Array("entity.student", "error.model.id.needed"))
    val stdCodes = Strings.split(stdCodeSeq, ",")
    val curUser = entityDao.get(classOf[User], getUserId)
    try {
      logHelper.info("Add count for std Nos:" + stdCodeSeq)
      val stds = entityDao.get(classOf[Student], "code", stdCodes)
      var it = stds.iterator()
      while (it.hasNext) {
        val one = it.next().asInstanceOf[Student]
        eamsUserService.createStdUser(curUser, one)
      }
    } catch {
      case e: Exception => {
        logHelper.info("Failure Add count for std Nos:" + stdCodes, e)
        return forwardError("error.occurred")
      }
    }
    redirect("search", "info.add.success")
  }

  def promptToManager(): String = {
    val names = Strings.split(get("stdCodes"), ",")
    val manager = entityDao.get(classOf[User], getUserId)
    for (i <- 0 until names.length) {
      var one = eamsUserService.get(names(i))
      if (null == one) {
        logHelper.info("Add teacher acount for:" + names(i))
        val std = studentService.getStudent(names(i))
        one = eamsUserService.createStdUser(manager, std)
      }
      eamsUserService.saveOrUpdate(one)
    }
    eamsUserService.saveOrUpdate(manager.asInstanceOf[User])
    redirect("search", "info.update.success")
  }
}
