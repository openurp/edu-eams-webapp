package org.openurp.edu.eams.teach.service

import java.util.Set
import org.openurp.edu.base.Student

import scala.collection.JavaConversions._

trait StudentSource {

  def getStudents(): Set[Student]
}
