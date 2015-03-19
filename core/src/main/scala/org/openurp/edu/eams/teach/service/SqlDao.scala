package org.openurp.edu.eams.teach.service






trait SqlDao {

  def executeUpdate(sql: String, params: Map[_,_]): Unit

  def batchUpdate(sqls: List[String]): Unit
}
