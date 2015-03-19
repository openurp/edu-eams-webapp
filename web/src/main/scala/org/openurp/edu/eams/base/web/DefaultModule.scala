package org.openurp.edu.eams.base.web

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.base.web.action.BuildingAction
import org.openurp.edu.eams.base.web.action.CampusAction
import org.openurp.edu.eams.base.web.action.RoomAction
import org.openurp.edu.eams.base.web.action.RoomSearchAction
import org.openurp.edu.eams.base.web.action.DepartmentAction
import org.openurp.edu.eams.base.web.action.DepartmentSearchAction
import org.openurp.edu.eams.base.web.action.HolidayAction
import org.openurp.edu.eams.base.web.action.SchoolAction
import org.openurp.edu.eams.base.web.action.SemesterAction
import org.openurp.edu.eams.base.web.action.TimeSettingAction
import org.openurp.edu.eams.base.web.action.code.ManageAction
import org.openurp.edu.eams.base.web.action.code.QueryAction
import org.openurp.edu.eams.base.web.action.code.SearchAction



class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[QueryAction])
    bind(classOf[ManageAction])
    bind(classOf[SearchAction])
    bind(classOf[SchoolAction], classOf[RoomSearchAction], classOf[DepartmentSearchAction], classOf[CampusAction], 
      classOf[BuildingAction], classOf[RoomAction], classOf[DepartmentAction], classOf[SemesterAction], 
      classOf[HolidayAction], classOf[TimeSettingAction])
  }
}
