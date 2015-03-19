package org.openurp.edu.eams.teach.grade.adminclass.web.action
import java.util.Date


import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.security.blueprint.SecurityUtils
import org.openurp.base.Department
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class IndexAction extends SemesterSupportAction {

  def indexSetting() {
    var builder = OqlBuilder.from(classOf[Adminclass], "adc").join("adc.instructors", "instructor")
    builder.where("instructor.code=:myname", SecurityUtils.getUsername)
    builder.where("adc.effectiveAt <=:now and (adc.invalidAt is null or adc.invalidAt >=:now)", new Date())
    val classes = CollectUtils.newHashSet(entityDao.search(builder))
    val departs = getDeparts
    if (!departs.isEmpty) {
      builder = OqlBuilder.from(classOf[Adminclass], "adc").where("adc.department in(:departs)", departs)
      builder.where("adc.effectiveAt <=:now and (adc.invalidAt is null or adc.invalidAt >=:now)", new Date())
      classes.addAll(entityDao.search(builder))
    }
    val adminclasses = CollectUtils.newArrayList(classes)
    Collections.sort(adminclasses, new PropertyComparator("name desc"))
    put("adminclasses", adminclasses)
  }

  def info(): String = {
    val adminclass = entityDao.get(classOf[Adminclass], getInt("adminclass.id"))
    put("adminclass", adminclass)
    forward()
  }
}
