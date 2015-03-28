package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable

import org.beangle.commons.collection.Collections




@SerialVersionUID(-6253573939352498929L)
class ElectCourseSubstitution extends Serializable {

  
  var origins: Set[Long] = Collections.newSet[Any]

  
  var substitutes: Set[Long] = Collections.newSet[Any]
}
