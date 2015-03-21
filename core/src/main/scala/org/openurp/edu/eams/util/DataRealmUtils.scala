package org.openurp.edu.eams.util


import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.system.security.DataRealm
import org.beangle.commons.collection.CollectUtils
import java.util.ArrayList
import org.beangle.data.model.dao.Condition



object DataRealmUtils {

  def addDataRealm(query: OqlBuilder[_], attrs: Array[String], dataRealm: DataRealm) {
    addDataRealms(query, attrs,List(dataRealm))
  }

  def inDataRealms(query: OqlBuilder[_], attrs: Array[String], dataRealms: List[_]) {
    if (dataRealms == null || dataRealms.isEmpty) return
    val conditions = CollectUtils.newArrayList[Condition]
    val datas = CollectUtils.newArrayList[Any]
    for (i <- 0 until dataRealms.size) {
      val dataRealm = dataRealms(i).asInstanceOf[DataRealm]
      val buffer = new StringBuffer("")
      if (attrs.length > 0) {
        if (Strings.isNotEmpty(dataRealm.studentTypeIdSeq) && Strings.isNotEmpty(attrs(0))) {
          buffer.append(attrs(0) + " in (:mytypeIds" + randomInt() + ")")
          datas += Strings.transformToLong(Strings.split(dataRealm.studentTypeIdSeq))
        }
      }
      if (attrs.length > 1) {
        if (Strings.isNotEmpty(dataRealm.departmentIdSeq) && Strings.isNotEmpty(attrs(1))) {
          if (buffer.length > 0) {
            buffer.append(" and ")
          }
          buffer.append(attrs(1) + " in (:myDepartIds" + randomInt() + ")")
          datas += Strings.transformToLong(Strings.split(dataRealm.departmentIdSeq))
        }
      }
      if (buffer.length > 0) {
        conditions += new Condition(buffer.toString)
      }
    }
    val buffer = new StringBuffer("(")
    for (i <- 0 until conditions.size) {
      val condition = conditions(i)
      if (i != 0) {
        buffer.append(" or ")
      }
      buffer.append(condition.content)
    }
    buffer.append(")")
    val con = new Condition(buffer.toString)
    con.params(datas)
    query.where(con)
  }

  def addDataRealms(query: OqlBuilder[_], attrs: Array[String], dataRealms: Seq[_]) {
    if (dataRealms == null || dataRealms.isEmpty) return
    val conditions = CollectUtils.newArrayList[Condition]
    val datas = CollectUtils.newArrayList[Any]
    for (i <- 0 until dataRealms.size) {
      val dataRealm = dataRealms(i).asInstanceOf[DataRealm]
      val buffer = new StringBuffer("")
      if (attrs.length > 0) {
        if (Strings.isNotEmpty(dataRealm.studentTypeIdSeq) && Strings.isNotEmpty(attrs(0))) {
          buffer.append(" exists (from " + classOf[StdType].getName + " mytype where mytype.id =" + 
            attrs(0))
          buffer.append(" and mytype.id in(:mytypeIds" + randomInt() + "))")
          datas+= Strings.transformToLong(Strings.split(dataRealm.studentTypeIdSeq))
        }
      }
      if (attrs.length > 1) {
        if (Strings.isNotEmpty(dataRealm.departmentIdSeq) && Strings.isNotEmpty(attrs(1))) {
          if (buffer.length > 0) {
            buffer.append(" and ")
          }
          buffer.append(" exists (from " + classOf[Department].getName + " mydepart where mydepart.id =" + 
            attrs(1))
          buffer.append(" and mydepart.id in(:myDepartIds" + randomInt() + "))")
          datas += Strings.transformToLong(Strings.split(dataRealm.departmentIdSeq))
        }
      }
      if (buffer.length > 0) {
        conditions += new Condition((buffer.toString))
      }
    }
    val buffer = new StringBuffer("(")
    for (i <- 0 until conditions.size) {
      val condition = conditions(i).asInstanceOf[Condition]
      if (i != 0) {
        buffer.append(" or ")
      }
      buffer.append(condition.content)
    }
    buffer.append(")")
    val con = new Condition(buffer.toString)
    con.params(datas)
    query.where(con)
  }

  private def randomInt(): String = {
    var d = String.valueOf(Math.random())
    d = Strings.replace(d, ".", "")
    d = d.substring(0, 8)
    d
  }
}
