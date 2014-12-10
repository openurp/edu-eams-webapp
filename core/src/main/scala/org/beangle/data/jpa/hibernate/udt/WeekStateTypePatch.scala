package org.beangle.data.jpa.hibernate.udt

import java.sql.{ PreparedStatement, ResultSet, Types }

import org.beangle.commons.lang.time.WeekState
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.usertype.UserType

import java.io.{Serializable => JSerializable}

class WeekStateType extends UserType {

  override def sqlTypes() = Array(Types.BIGINT)

  override def returnedClass = classOf[WeekState]

  override def equals(x: Object, y: Object) = {
    x.asInstanceOf[WeekState].value == y.asInstanceOf[WeekState].value
  }

  override def hashCode(x: Object) = x.hashCode()

  override def nullSafeGet(resultSet: ResultSet, names: Array[String], session: SessionImplementor, owner: Object): Object = {
    val value = resultSet.getLong(names(0))
    if (resultSet.wasNull()) null
    else new WeekState(value)
  }

  override def nullSafeSet(statement: PreparedStatement, value: Object, index: Int, session: SessionImplementor): Unit = {
    if (value == null) {
      statement.setNull(index, Types.BIGINT)
    } else {
      statement.setLong(index, value.asInstanceOf[WeekState].value)
    }
  }

  override def deepCopy(value: Object): Object = value
  override def isMutable() = false
  override def disassemble(value: Object) = value.asInstanceOf[JSerializable]
  override def assemble(cached: JSerializable, owner: Object): Object = cached.asInstanceOf[Object]
  override def replace(original: Object, target: Object, owner: Object) = original
}