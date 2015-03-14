package org.openurp.edu.eams.teach.program.major.service.impl

import java.util.List
import com.ekingstar.eams.system.security.DataRealm
import org.openurp.edu.eams.teach.program.major.dao.MajorPlanStatDao
import org.openurp.edu.eams.teach.program.major.service.MajorPlanStatService
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanStatServiceImpl extends MajorPlanStatService {

  var majorPlanStatDao: MajorPlanStatDao = _

  def statByDepart(realm: DataRealm, grade: String): List[_] = {
    majorPlanStatDao.statByDepart(realm, grade)
  }

  def statByStdType(realm: DataRealm, grade: String): List[_] = {
    majorPlanStatDao.statByStdType(realm, grade)
  }

  def getGrades(realm: DataRealm): List[_] = majorPlanStatDao.getGrades(realm)

  def setMajorPlanStatDao(majorPlanStatDao: MajorPlanStatDao) {
    this.majorPlanStatDao = majorPlanStatDao
  }
}
