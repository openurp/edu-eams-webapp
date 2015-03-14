package org.openurp.edu.eams.util

import org.beangle.commons.collection.page.Limit
import org.beangle.commons.collection.page.PageLimit
import org.openurp.edu.eams.system.security.DataRealm
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

class DataRealmLimit extends Limit() {

  @BeanProperty
  var pageLimit: PageLimit = new PageLimit()

  @BeanProperty
  var dataRealm: DataRealm = new DataRealm()

  def this(stdTypeIds: String, departIds: String) {
    this()
    this.dataRealm.setStudentTypeIdSeq(stdTypeIds)
    this.dataRealm.setDepartmentIdSeq(departIds)
  }
}
