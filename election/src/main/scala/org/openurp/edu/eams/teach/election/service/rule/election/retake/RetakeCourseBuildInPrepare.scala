package org.openurp.edu.eams.teach.election.service.rule.election.retake

import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.ElectBuildInPrepare
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.CourseGradePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonNoRetakeFilter
import RetakeCourseBuildInPrepare._



object RetakeCourseBuildInPrepare {

  val STATE_PARAM = "UNPASSED_RETAKE_ENABLED"
}

class RetakeCourseBuildInPrepare extends ElectRulePrepare with ElectBuildInPrepare {

  private var courseGradePrepare: CourseGradePrepare = _

  def prepare(context: PrepareContext) {
    if (true != context.getState.getParams.get(ElectableLessonNoRetakeFilter.PARAM)) {
      if (!context.isPreparedData(PreparedDataName.RETAKE_COURSES) || 
        (null != context.getState.getParams.get(STATE_PARAM) && 
        !context.isPreparedData(PreparedDataName.UNPASSED_RETAKE_COURSES))) {
        courseGradePrepare.prepare(context)
      }
    }
  }

  def setCourseGradePrepare(courseGradePrepare: CourseGradePrepare) {
    this.courseGradePrepare = courseGradePrepare
  }
}
