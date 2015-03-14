package org.openurp.edu.eams.teach.grade.transcript.service

import java.util.List
import java.util.Map
import org.openurp.edu.base.Student

import scala.collection.JavaConversions._

trait TranscriptDataProvider {

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T]

  def getData[T](std: Student, options: Map[String, String]): T

  def getDataName(): String
}
