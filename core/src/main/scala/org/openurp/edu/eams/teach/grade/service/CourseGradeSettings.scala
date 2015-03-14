package org.openurp.edu.eams.teach.grade.service

import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.grade.model.CourseGradeSetting

import scala.collection.JavaConversions._

trait CourseGradeSettings {

  def getSetting(project: Project): CourseGradeSetting
}
