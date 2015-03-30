package org.openurp.edu.eams.system.security

import java.io.Serializable



import org.beangle.data.model.Component
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import DataRealm._




object DataRealm {

  def mergeAll(realms: List[_]): DataRealm = {
    val realm = new DataRealm("","")
    if (null == realms || realms.isEmpty) {
      return realm
    }
    var iter = realms.iterator
    while (iter.hasNext) {
      val thisRealm = iter.next().asInstanceOf[DataRealm]
      realm.merge(thisRealm)
    }
    realm
  }

  private def evictComma(str: String): String = {
    if (Strings.isEmpty(str)) str else {
      if (str.startsWith(",") && str.endsWith(",")) str.substring(1, str.length - 1) else if (str.startsWith(",")) {
        str.substring(1)
      } else if (str.endsWith(",")) {
        str.substring(0, str.length - 1)
      } else {
        str
      }
    }
  }
}

@SerialVersionUID(-75303778825269630L)
@Deprecated
class DataRealm(var studentTypeIdSeq: String, var departmentIdSeq: String) extends Component with Serializable {

  def id(): java.lang.Long = null

  def getIsValid(): java.lang.Boolean = null

  def getItems(): Set[_] = null

  def getLongId(): Serializable = null

  def isSaved(): Boolean = false

  def isValidEntity(): Boolean = false

  def key(): String = null

  def isPO(): Boolean = false

  def isVO(): Boolean = false

  override def clone(): AnyRef = {
    new DataRealm(studentTypeIdSeq, departmentIdSeq)
  }


  def merge(other: DataRealm): DataRealm = {
    if (null == other) this else {
      this.departmentIdSeq = evictComma(Strings.mergeSeq(departmentIdSeq, other.departmentIdSeq))
      this.studentTypeIdSeq = evictComma(Strings.mergeSeq(studentTypeIdSeq, other.studentTypeIdSeq))
      this
    }
  }

  def shrink(other: DataRealm): DataRealm = {
    if (null == other) this else {
      this.departmentIdSeq = evictComma(Strings.subtractSeq(departmentIdSeq, other.departmentIdSeq))
      this.studentTypeIdSeq = evictComma(Strings.subtractSeq(studentTypeIdSeq, other.studentTypeIdSeq))
      this
    }
  }

  override def toString(): String = {
    Objects.toStringBuilder(this.getClass).add("studentTypeIdSeq", this.studentTypeIdSeq)
      .add("departmentIdSeq", this.departmentIdSeq)
      .toString
  }
}
