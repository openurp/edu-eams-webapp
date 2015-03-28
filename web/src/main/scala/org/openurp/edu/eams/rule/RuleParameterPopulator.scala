package org.openurp.edu.eams.rule


import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.ems.rule.Rule
import org.beangle.ems.rule.RuleParameter
import org.beangle.struts2.helper.Params
import org.beangle.struts2.helper.PopulateHelper



object RuleParameterPopulator {

  def populateParams(rule: Rule, prefix: String): Set[RuleParameter] = {
    val size = Params.getInt(prefix + "_param_size")
    val params = Collections.newSet[Any]
    for (i <- 0 until size) {
      val ruleParam = PopulateHelper.populate(classOf[RuleParameter], prefix + "_" + i)
      if (Strings.isBlank(ruleParam.getName)) {
        //continue
      }
      ruleParam.setId(null)
      ruleParam.setChildren(populateParams(rule, prefix + "_" + i))
      ruleParam.setRule(rule)
      params.add(ruleParam)
    }
    params
  }
}
