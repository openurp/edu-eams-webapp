package org.openurp.edu.eams.util

import org.beangle.commons.collection.page.Limit
import org.beangle.commons.collection.page.PageLimit
import org.openurp.edu.eams.system.security.DataRealm




class DataRealmLimit extends Limit() {

  
  var pageLimit: PageLimit = new PageLimit()

  
  var dataRealm: DataRealm = new DataRealm()

  def this(stdTypeIds: String, departIds: String) {
    this()
    this.dataRealm.setStudentTypeIdSeq(stdTypeIds)
    this.dataRealm.setDepartmentIdSeq(departIds)
  }
}
