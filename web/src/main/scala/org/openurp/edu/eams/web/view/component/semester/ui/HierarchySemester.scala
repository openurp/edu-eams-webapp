package org.openurp.edu.eams.web.view.component.semester.ui



import org.beangle.commons.entity.HierarchyEntity




@SerialVersionUID(1306627177271312843L)
class HierarchySemester extends HierarchyEntity[HierarchySemester, Integer] {

  
  var name: String = _

  
  var id: java.lang.Integer = _

  
  var parent: HierarchySemester = _

  
  var children: List[HierarchySemester] = new ArrayList[HierarchySemester]()

  def identifier(): java.lang.Integer = id

  def isPersisted(): Boolean = false

  def isTransient(): Boolean = true

  def getIndexno(): String = null
}
