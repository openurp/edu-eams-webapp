package org.openurp.edu.eams.util

import org.beangle.commons.collection.page.Limit
import org.beangle.commons.collection.page.PageLimit
import org.openurp.edu.eams.system.security.DataRealm




class DataRealmLimit extends Limit() {

  
  var pageLimit: PageLimit = _

  
  var dataRealm: DataRealm =_

  def this(stdTypeIds: String, departIds: String) {
    this()
    this.dataRealm.studentTypeIdSeq=stdTypeIds
    this.dataRealm.departmentIdSeq=departIds
  }
}
