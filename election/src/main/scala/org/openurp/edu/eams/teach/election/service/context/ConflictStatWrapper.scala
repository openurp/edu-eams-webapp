package org.openurp.edu.eams.teach.election.service.context


import org.beangle.commons.collection.CollectUtils




class ConflictStatWrapper( var id: Long) {

  
  var conflicts: List[Long] = CollectUtils.newArrayList()
}
