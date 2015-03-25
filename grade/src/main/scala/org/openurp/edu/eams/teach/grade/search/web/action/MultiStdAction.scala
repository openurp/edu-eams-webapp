package org.openurp.edu.eams.teach.grade.search.web.action

import org.beangle.commons.lang.Strings
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.service.GpaStatService
import org.openurp.edu.eams.teach.grade.service.impl.MultiStdGpa
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper



class MultiStdAction extends RestrictionSupportAction {

  var baseInfoSearchHelper: BaseInfoSearchHelper = _

  var gpaStatService: GpaStatService = _

  def index(): String = {
    setDataRealm(hasStdTypeCollege)
    forward()
  }

  def adminClassList(): String = {
    put("adminClasses", baseInfoSearchHelper.searchAdminclass())
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType]))
    forward()
  }

  def classGpaReport(): String = {
    var adminClassIds = get("adminClassIds")
    if (Strings.isEmpty(adminClassIds)) {
      adminClassIds = get("adminClassId")
    }
    val pageSize = getPageSize
    val adminClasses = entityDao.get(classOf[Adminclass], Strings.splitToInt(adminClassIds))
    val multiStdGpas = CollectUtils.newArrayList()
    for (adminClass <- adminClasses) {
      val multiStdGpa = gpaStatService.statGpas(adminClass.getStudents)
      multiStdGpas.add(multiStdGpa)
    }
    put("pageSize", new java.lang.Integer(pageSize))
    val orders = Order.parse(get("orderBy"))
    if (!orders.isEmpty) {
      val order = orders.get(0)
      val orderCmp = new PropertyComparator(order.getProperty, order.isAscending)
      Collections.sort(multiStdGpas, orderCmp)
    }
    put("multiStdGpas", multiStdGpas)
    forward()
  }
}
