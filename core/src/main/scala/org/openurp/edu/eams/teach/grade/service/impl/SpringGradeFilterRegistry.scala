package org.openurp.edu.eams.teach.grade.service.impl

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.springframework.beans.BeansException
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.openurp.edu.teach.grade.domain.impl.GradeFilterRegistry
import org.openurp.edu.teach.grade.domain.GradeFilter

class SpringGradeFilterRegistry extends GradeFilterRegistry with ApplicationContextAware with InitializingBean {

  val filters = Collections.newMap[String, GradeFilter]

  var applicationContext: ApplicationContext = _

  def setApplicationContext(applicationContext: ApplicationContext) {
    this.applicationContext = applicationContext
  }

  def afterPropertiesSet() {
    if (null == applicationContext) return
    val names = applicationContext.getBeanNamesForType(classOf[GradeFilter])
    if (null != names && names.length > 0) {
      for (name <- names) {
        filters.put(name, applicationContext.getBean(name).asInstanceOf[GradeFilter])
      }
    }
  }

  def getFilter(name: String): GradeFilter = filters.get(name).orNull

  def getFilters(name: String): Seq[GradeFilter] = {
    if (Strings.isBlank(name)) return List.empty

    val filterNames = Strings.split(name, Array('|', ','))
    val myFilters = Collections.newBuffer[GradeFilter]
    for (filterName <- filterNames) {
      val filter = filters.get(filterName).orNull
      if (null != filter) myFilters += filter
    }
    myFilters
  }
}
