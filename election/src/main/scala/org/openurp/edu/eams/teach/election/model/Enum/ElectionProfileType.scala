package org.openurp.edu.eams.teach.election.model.Enum





object ElectionProfileType extends Enumeration {

  val STD = new ElectionProfileType("学生")

  val DEPART_ADMIN = new ElectionProfileType("院系管理员")

  val ADMIN = new ElectionProfileType("管理员")

  class ElectionProfileType( var title: String) extends Val {

    def id(): String = toString
  }

  implicit def convertValue(v: Value): ElectionProfileType = v.asInstanceOf[ElectionProfileType]
}
