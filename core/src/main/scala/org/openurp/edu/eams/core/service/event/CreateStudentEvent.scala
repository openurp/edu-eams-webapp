package org.openurp.edu.eams.core.service.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.base.Student



@SerialVersionUID(6912654970490765968L)
class CreateStudentEvent(source: Student) extends BusinessEvent(source)
