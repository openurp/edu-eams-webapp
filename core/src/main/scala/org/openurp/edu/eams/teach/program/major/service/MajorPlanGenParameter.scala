package org.openurp.edu.eams.teach.program.major.service

import java.sql.Date
import org.openurp.base.Department
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Student
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.base.Program
import org.openurp.code.edu.Degree
import org.openurp.code.edu.StudyType

class MajorPlanGenParameter {

  var name: String = _

  var grade: String = _

  var education: Education = _

  var stdType: StdType = _

  var department: Department = _

  var major: Major = _

  var direction: Direction = _

  var effectiveOn: Date = _

  var invalidOn: Date = _

  var duration: Float = _

  var studyType: StudyType = _

  var degree: Degree = _

  var student: Student = _

  var termsCount: Int = _
}
