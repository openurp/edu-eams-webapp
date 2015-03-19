package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable

import org.beangle.commons.collection.CollectUtils




@SerialVersionUID(-6253573939352498929L)
class ElectCourseSubstitution extends Serializable {

  
  var origins: Set[Long] = CollectUtils.newHashSet()

  
  var substitutes: Set[Long] = CollectUtils.newHashSet()
}
