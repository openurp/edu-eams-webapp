package org.openurp.edu.eams.teach.grade.service.impl


import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.springframework.beans.BeansException
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware



class SpringGradeFilterRegistry extends GradeFilterRegistry with ApplicationContextAware with InitializingBean {

  val filters = CollectUtils.newHashMap()

  var applicationContext: ApplicationContext = _

  def setApplicationContext(applicationContext: ApplicationContext) {
    this.applicationContext = applicationContext
  }

  def afterPropertiesSet() {
    if (null == applicationContext) return
    val names = applicationContext.beanNamesForType(classOf[GradeFilter])
    if (null != names && names.length > 0) {
      for (name <- names) {
        filters.put(name, applicationContext.bean(name).asInstanceOf[GradeFilter])
      }
    }
  }

  def getFilter(name: String): GradeFilter = filters.get(name)

  def getFilters(name: String): List[GradeFilter] = {
    if (Strings.isBlank(name)) return Collections.emptyList()
    val filterNames = Strings.split(name, Array('|', ','))
    val myFilters = CollectUtils.newArrayList()
    for (filterName <- filterNames) {
      val filter = filters.get(filterName)
      if (null != filter) myFilters.add(filter)
    }
    myFilters
  }
}
