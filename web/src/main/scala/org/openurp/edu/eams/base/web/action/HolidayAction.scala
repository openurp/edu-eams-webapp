package org.openurp.edu.eams.base.web.action

import org.openurp.edu.eams.base.Holiday
import org.openurp.edu.eams.web.action.common.RestrictionSupportAction



class HolidayAction extends RestrictionSupportAction {

  def index(): String = forward()

  protected override def getEntityName(): String = classOf[Holiday].getName
}
