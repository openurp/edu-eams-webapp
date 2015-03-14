package org.openurp.edu.eams.teach.grade.search.web.action

import java.util.Collections
import java.util.List
import org.beangle.commons.lang.Strings
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.service.GpaStatService
import org.openurp.edu.eams.teach.grade.service.impl.MultiStdGpa
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction
import org.openurp.edu.eams.web.helper.BaseInfoSearchHelper

import scala.collection.JavaConversions._

class MultiStdAction extends RestrictionSupportAction {

  private var baseInfoSearchHelper: BaseInfoSearchHelper = _

  private var gpaStatService: GpaStatService = _

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

  def setBaseInfoSearchHelper(baseInfoSearchHelper: BaseInfoSearchHelper) {
    this.baseInfoSearchHelper = baseInfoSearchHelper
  }

  def setGpaStatService(gpaStatService: GpaStatService) {
    this.gpaStatService = gpaStatService
  }
}
