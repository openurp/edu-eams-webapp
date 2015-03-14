package org.openurp.edu.eams.teach.service.impl

import java.util.Set
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.service.StudentSource

class StaticStudentSource extends StudentSource {

  var students: Set[Student] = _

  def getStudents(): Set[Student] = students

  def setStudents(students: Set[Student]) {
    this.students = students
  }
}
