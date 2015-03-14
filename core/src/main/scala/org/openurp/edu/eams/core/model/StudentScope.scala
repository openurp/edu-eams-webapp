package org.openurp.edu.eams.core.model

import collection.mutable
import collection.mutable.HashSet

import org.beangle.commons.collection.Collections
import org.beangle.data.model.Component
import org.beangle.data.model.annotation.code
import org.openurp.base.Department
import org.openurp.code.edu.Education
import org.openurp.edu.base.{ Direction, Major, Project }
import org.openurp.edu.base.code.StdType

class StudentScope extends Component {

  var grades: String = _

  var project: Project = _

  var educations: mutable.Set[Education] = new HashSet[Education]

  var stdTypes: mutable.Set[StdType] = new HashSet[StdType]()

  var departments: mutable.Set[Department] = new HashSet[Department]()

  var majors: mutable.Set[Major] = new HashSet[Major]()

  var directions: mutable.Set[Direction] = new HashSet[Direction]()

  def overlappedWith(scope: StudentScope): Boolean = {
    scope.project == this.project &&
      Collections.intersection(scope.stdTypes.toList, this.stdTypes.toList)
      .size >
      0 &&
      Collections.intersection(scope.educations.toList, this.educations.toList)
      .size >
      0
  }
}
