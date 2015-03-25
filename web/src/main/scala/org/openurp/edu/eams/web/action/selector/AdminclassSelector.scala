package org.openurp.edu.eams.web.action.selector


import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.service.AdminclassService
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class AdminclassSelector extends RestrictionSupportAction {

  var adminClassService: AdminclassService = _

  def withMajor(): String = {
    var moduleName = get("moduleName")
    if (Strings.isEmpty(moduleName)) {
      moduleName = "AdminclassManager"
    }
    val adminClassId = getLong("adminClassId")
    val selectorId = getLong("selectorId")
    val adminClass = populate(classOf[Adminclass], "adminClass").asInstanceOf[Adminclass]
    val adminClassList = CollectUtils.newArrayList()
    put("adminClassList", adminClassList)
    put("majorId", if (adminClass.major != null && adminClass.major.isPersisted) adminClass.major.id else null)
    put("directionId", if (adminClass.direction != null && adminClass.direction.isPersisted) adminClass.direction.id else null)
    put("departmentId", if (adminClass.department != null && adminClass.department.isPersisted) adminClass.department.id else null)
    put("gradeId", if (Strings.isEmpty(adminClass.grade)) null else adminClass.grade)
    put("adminClassId", adminClassId)
    put("selectorId", selectorId)
    forward("success")
  }
}
