package org.openurp.edu.eams.classroom.util

import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.Strings

import scala.collection.JavaConversions._

object RoomUseridGenerator {

  object Usage extends Enumeration {

    val COURSE = new Usage()

    val EXAM = new Usage()

    val OTHER = new Usage()

    class Usage extends Val

    implicit def convertValue(v: Value): Usage = v.asInstanceOf[Usage]
  }

  def gen(obj: AnyRef, usage: Usage): String = {
    var objId: AnyRef = null
    objId = if (obj.isInstanceOf[Entity]) obj.asInstanceOf[Entity[_]].getId else obj
    if (null == objId) {
      throw new RuntimeException("cannot find object id for room user")
    }
    Strings.concat(String.valueOf(objId), "@", usage.toString)
  }

  def gen(obj: AnyRef, usage1: Usage, usage2: Usage): Array[String] = {
    Array(gen(obj, usage1), gen(obj, usage2))
  }
}
