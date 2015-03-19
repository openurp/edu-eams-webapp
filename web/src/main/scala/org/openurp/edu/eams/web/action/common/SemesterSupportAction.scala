package org.openurp.edu.eams.web.action.common




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
