package org.openurp.edu.eams.core.service.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.base.Student

@SerialVersionUID(4872517880133438020L)
class AlterStudentGraduateOnEvent(source: Student) extends BusinessEvent(source)
