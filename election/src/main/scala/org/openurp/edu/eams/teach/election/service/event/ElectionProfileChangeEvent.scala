package org.openurp.edu.eams.teach.election.service.event

import org.beangle.commons.event.BusinessEvent
import org.openurp.edu.eams.teach.election.service.cache.ProfileLessonDataProvider



@SerialVersionUID(1527167981497100427L)
class ElectionProfileChangeEvent(source: ProfileLessonDataProvider) extends BusinessEvent(source)
