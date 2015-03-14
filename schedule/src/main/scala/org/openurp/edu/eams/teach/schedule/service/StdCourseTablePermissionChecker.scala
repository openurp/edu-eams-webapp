package org.openurp.edu.eams.teach.schedule.service

import org.openurp.edu.base.Student

import scala.collection.JavaConversions._

trait StdCourseTablePermissionChecker {

  def check(std: Student, kind: String, ids: String): String
}
