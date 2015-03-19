package org.openurp.edu.eams.teach.schedule.service.impl

import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.schedule.service.StdCourseTablePermissionChecker
import org.openurp.edu.eams.teach.schedule.util.CourseTable



class DefaultStdCourseTablePermissionChecker extends BaseServiceImpl with StdCourseTablePermissionChecker {

  def check(std: Student, kind: String, ids: String): String = {
    if (Strings.isBlank(kind)) {
      return null
    }
    kind = Strings.trim(kind)
    if (Strings.isBlank(ids)) {
      return null
    }
    ids = Strings.trim(ids)
    if (CourseTable.STD == kind) {
      try {
        if (ids != std.id + "") {
          return "没有权限"
        }
      } catch {
        case e: Exception => 
      }
      return null
    } else if (CourseTable.CLASS == kind) {
      try {
        var adminclass = std.getAdminclass
        if (null == adminclass) {
          val student = entityDao.get(classOf[Student], std.id)
          adminclass = student.getAdminclass
        }
        if (null == adminclass.id || ids == std.getAdminclass.id + "") {
          return "没有权限"
        }
      } catch {
        case e: Exception => 
      }
      return null
    }
    "没有权限"
  }
}
