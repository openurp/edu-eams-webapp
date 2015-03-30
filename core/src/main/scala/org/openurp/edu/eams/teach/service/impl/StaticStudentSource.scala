package org.openurp.edu.eams.teach.service.impl


import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.service.StudentSource

class StaticStudentSource extends StudentSource {

  var students: Set[Student] = _

}
