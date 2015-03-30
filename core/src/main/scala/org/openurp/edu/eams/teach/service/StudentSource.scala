package org.openurp.edu.eams.teach.service


import org.openurp.edu.base.Student



trait StudentSource {

  def students: Iterable[Student]
}
