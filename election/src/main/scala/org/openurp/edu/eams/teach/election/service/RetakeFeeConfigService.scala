package org.openurp.edu.eams.teach.election.service


import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.ems.dictionary.service.BaseCodeService
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import org.openurp.edu.teach.lesson.CourseTake



trait RetakeFeeConfigService extends BaseCodeService {

  def getFeeRuleScript(): String

  def getCurrOpenConfigs(): List[RetakeFeeConfig]

  def getOpenConfigs(semesters: Semester*): List[RetakeFeeConfig]

  def getOpenConfigs(project: Project, semesters: Semester*): List[RetakeFeeConfig]

  def getOpenConfigBuilder(project: Project, semesters: Semester*): OqlBuilder[RetakeFeeConfig]

  def getConfigs(project: Project, semesters: Semester*): List[RetakeFeeConfig]

  def doCheck(project: Project, semesters: Semester*): Boolean

  def getRetakeCourseTakes(student: Student, semesters: Semester*): List[CourseTake]

  def doCheck(config: RetakeFeeConfig): Boolean

  def saveOrUpdate(config: RetakeFeeConfig): Unit

  def getConfig(config: RetakeFeeConfig): RetakeFeeConfig
}
