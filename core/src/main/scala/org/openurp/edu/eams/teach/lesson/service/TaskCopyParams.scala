package org.openurp.edu.eams.teach.lesson.service

import org.openurp.base.Semester



class TaskCopyParams {

  var semester: Semester = _

  var copyCourseTakes: Boolean = _

  var copyCount: Int = _

  def getCopyCount(): Int = copyCount

  def setCopyCount(copyCount: Int) {
    this.copyCount = copyCount
  }

  def getSemester(): Semester = semester

  def setSemester(semester: Semester) {
    this.semester = semester
  }

  def isCopyCourseTakes(): Boolean = copyCourseTakes

  def setCopyCourseTakes(copyCourseTakes: Boolean) {
    this.copyCourseTakes = copyCourseTakes
  }
}
