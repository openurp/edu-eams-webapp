package org.openurp.edu.eams.core.web.action.biz

import java.util.ArrayList
import java.util.List
import org.openurp.edu.eams.core.code.ministry.DisciplineCatalog
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class Catalog(catalog: DisciplineCatalog) {

  @BeanProperty
  var id: java.lang.Integer = catalog.getId

  @BeanProperty
  var name: String = catalog.getName

  @BeanProperty
  var code: String = catalog.getCode

  @BeanProperty
  var firstdiscs: List[Disc] = new ArrayList[Disc]()
}
