package org.openurp.edu.eams.teach.election.service.rule.election


import org.beangle.commons.collection.CollectUtils
import org.openurp.base.Semester
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.time.util.TermCalculator



class CurrentTermPrepare extends ElectRulePrepare {

  protected var calMap: Map[Semester, TermCalculator] = CollectUtils.newHashMap()

  protected var semesterService: SemesterService = _

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.CURRENT_TERM)) {
      context.getState.getParams.put("CURRENT_TERM", calcTerm(context))
      context.addPreparedDataName(PreparedDataName.CURRENT_TERM)
    }
  }

  protected def calcTerm(context: PrepareContext): java.lang.Integer = {
    if (null == context.getPlan) return 1
    val semester = context.getProfile.getSemester
    var calc = calMap.get(semester)
    if (null == calc) {
      calc = new TermCalculator(semesterService, semester)
      calMap.put(semester, calc)
    }
    calc.getTerm(context.getPlan.getEffectiveOn, true)
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def getTerm(context: PrepareContext): java.lang.Integer = {
    val term = context.getState.getParams.get("CURRENT_TERM").asInstanceOf[java.lang.Integer]
    if (null == term) prepare(context)
    term
  }
}
