package org.openurp.edu.eams.teach.schedule.util

import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.model.Entity



class MultiCourseTable {

  var tables: collection.mutable.Buffer[CourseTable] = Collections.newBuffer[CourseTable]

  var resources: collection.mutable.Buffer[Entity[_]] = Collections.newBuffer[Entity[_]]

  var order: Order = _

}
