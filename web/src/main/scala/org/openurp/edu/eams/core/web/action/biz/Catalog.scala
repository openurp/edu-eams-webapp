package org.openurp.edu.eams.core.web.action.biz



import org.openurp.edu.eams.core.code.ministry.DisciplineCatalog




class Catalog(catalog: DisciplineCatalog) {

  
  var id: java.lang.Integer = catalog.id

  
  var name: String = catalog.getName

  
  var code: String = catalog.getCode

  
  var firstdiscs: List[Disc] = new ArrayList[Disc]()
}
