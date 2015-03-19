package org.openurp.edu.eams.core.web.action.biz


import java.util.Date

import org.openurp.edu.eams.core.code.ministry.Discipline




class Disc(firstdisc: Discipline) {

  
  var id: java.lang.Integer = firstdisc.id

  
  var name: String = firstdisc.getName

  
  var code: String = firstdisc.getCode

  
  var parent: Disc = _

  
  var children: List[Disc] = new ArrayList[Disc]()

  val now = new Date()

  for (child <- firstdisc.getChildren if child.getEffectiveAt.compareTo(now) <= 0 if child.getInvalidAt == null || child.getInvalidAt.compareTo(now) >= 0) {
    val t_child = new Disc(child)
    t_child.setParent(this)
    this.children.add(t_child)
  }
}
