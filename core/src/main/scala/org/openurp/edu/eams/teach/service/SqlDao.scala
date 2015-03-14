package org.openurp.edu.eams.teach.service

import java.util.List
import java.util.Map

import scala.collection.JavaConversions._

trait SqlDao {

  def executeUpdate(sql: String, params: Map[_,_]): Unit

  def batchUpdate(sqls: List[String]): Unit
}
