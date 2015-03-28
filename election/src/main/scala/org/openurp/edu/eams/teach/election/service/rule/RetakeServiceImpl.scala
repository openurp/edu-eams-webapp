package org.openurp.edu.eams.teach.election.service.rule



import org.beangle.commons.collection.Collections
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonByTeachClassFilter
import org.openurp.edu.eams.teach.election.service.rule.election.filter.ElectableLessonNoRetakeFilter



class RetakeServiceImpl {

  def isRetakeCourse(state: ElectState, courseId: java.lang.Long): Boolean = state.isRetakeCourse(courseId)

  def isRetakeProfile(state: ElectState): Boolean = {
    true != state.getParams.get(ElectableLessonNoRetakeFilter.PARAM)
  }

  def isCheckTeachClass(configs: Set[RuleConfig]): Boolean = {
    val keys = Collections.newHashSet("是", "TRUE", "1", "T", "Y", "YES")
    for (ruleConfig <- configs if ruleConfig.getRule.getServiceName.toLowerCase() == classOf[ElectableLessonByTeachClassFilter].getSimpleName
      .toLowerCase()) {
      val it = ruleConfig.getParams.iterator()
      if (it.hasNext) {
        return keys.contains(it.next().getValue.toUpperCase())
      }
    }
    false
  }
}
