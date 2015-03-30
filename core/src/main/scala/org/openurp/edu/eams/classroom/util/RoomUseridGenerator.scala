package org.openurp.edu.eams.classroom.util

import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.openurp.base.code.RoomUsage

object RoomUseridGenerator {

  object Usage extends Enumeration {

    val COURSE = new Usage()

    val EXAM = new Usage()

    val OTHER = new Usage()

    class Usage extends Val

    import scala.language.implicitConversions
    implicit def convertValue(v: Value): Usage = v.asInstanceOf[Usage]
  }

  import Usage._

  def gen(obj: AnyRef, usage: Usage): String = {
    var objId: AnyRef = null
    objId = if (obj.isInstanceOf[Entity[_]]) obj.asInstanceOf[Entity[AnyRef]].id else obj
    if (null == objId) {
      throw new RuntimeException("cannot find object id for room user")
    }
    Strings.concat(String.valueOf(objId), "@", usage.toString)
  }

  def gen(obj: AnyRef, usage1: Usage, usage2: Usage): Array[String] = {
    Array(gen(obj, usage1), gen(obj, usage2))
  }
}
