package org.openurp.edu.eams.web.action.common


import scala.collection.JavaConversions._

abstract class SemesterSupportAction extends ProjectSupportAction {

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    indexSetting()
    forward()
  }

  def setSemesterDataRealm(realmScope: Int) {
    setDataRealm(realmScope)
    putSemester(getProject)
  }
}
