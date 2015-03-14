package org.openurp.edu.eams.teach.grade.transcript.service.impl

import java.util.Collections
import java.util.List
import java.util.Map
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.springframework.beans.BeansException
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider

import scala.collection.JavaConversions._

class SpringTranscriptDataProviderRegistry extends ApplicationContextAware with InitializingBean {

  val providers = CollectUtils.newHashMap()

  var applicationContext: ApplicationContext = _

  def setApplicationContext(applicationContext: ApplicationContext) {
    this.applicationContext = applicationContext
  }

  def afterPropertiesSet() {
    if (null == applicationContext) return
    val names = applicationContext.getBeanNamesForType(classOf[TranscriptDataProvider])
    if (null != names && names.length > 0) {
      for (name <- names) {
        providers.put(name, applicationContext.getBean(name).asInstanceOf[TranscriptDataProvider])
      }
    }
  }

  def getProvider(name: String): TranscriptDataProvider = providers.get(name)

  def getProviders(name: String): List[TranscriptDataProvider] = {
    if (Strings.isBlank(name)) return Collections.emptyList()
    val filterNames = Strings.split(name, Array('|', ','))
    val myFilters = CollectUtils.newArrayList()
    for (filterName <- filterNames) {
      val filter = providers.get(filterName)
      if (null != filter) myFilters.add(filter)
    }
    myFilters
  }
}
