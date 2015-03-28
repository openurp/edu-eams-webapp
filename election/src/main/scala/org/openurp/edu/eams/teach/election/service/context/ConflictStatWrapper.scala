package org.openurp.edu.eams.teach.election.service.context


import org.beangle.commons.collection.Collections




class ConflictStatWrapper( var id: Long) {

  
  var conflicts: List[Long] = Collections.newBuffer[Any]
}
