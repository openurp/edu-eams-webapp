package org.openurp.edu.eams.web.action.util





import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.util.DataAuthorityPredicate



object DataAuthorityUtil {

  var predicates: Map[_,_] = new HashMap()

  var predicateWithSimpleName: DataAuthorityPredicate = new DataAuthorityPredicate("", "", "stdType", 
    "department")

  var departPpredicate: DataAuthorityPredicate = new DataAuthorityPredicate("", "", "", "department")

  val taskForTeachDepartPredicate = new DataAuthorityPredicate("", "", "teachClass.stdType", "teachDepart")

  predicates.put("Adminclass", predicateWithSimpleName)

  predicates.put("Teacher", departPpredicate)

  predicates.put("Major", predicateWithSimpleName)

  predicates.put("Course", predicateWithSimpleName)

  predicates.put("TeachPlan", predicateWithSimpleName)

  predicates.put("TeachTaskForTeachDepart", taskForTeachDepartPredicate)

  predicates.put("Student", new DataAuthorityPredicate("", "", "stdType", "department"))

  def isInDataRealm(category: String, 
      entity: AnyRef, 
      stdTypeIdSeq: String, 
      departIdSeq: String): Boolean = {
    getPredicate(category, entity, stdTypeIdSeq, departIdSeq)
      .apply(entity)
  }

  def isInDataRealm(predicate: DataAuthorityPredicate, 
      entity: AnyRef, 
      stdTypeIdSeq: String, 
      departIdSeq: String): Boolean = {
    predicate.setStdTypeDataRealm(stdTypeIdSeq)
    predicate.setDepartDataRealm(departIdSeq)
    predicate.apply(entity)
  }

  private def getPredicate(category: String, 
      entity: AnyRef, 
      stdTypeIdSeq: String, 
      departIdSeq: String): DataAuthorityPredicate = {
    val predicate = predicates.get(category).asInstanceOf[DataAuthorityPredicate]
    if (null == predicate) throw new RuntimeException("un registed predicate for " + entity.getClass.getName)
    predicate.setStdTypeDataRealm(stdTypeIdSeq)
    predicate.setDepartDataRealm(departIdSeq)
    predicate
  }

  private def getPredicate(predicateName: String, stdTypeIdSeq: String, departIdSeq: String): DataAuthorityPredicate = {
    val predicate = predicates.get(predicateName).asInstanceOf[DataAuthorityPredicate]
    if (null == predicate) throw new RuntimeException("un registed predicate for " + predicateName)
    predicate.setStdTypeDataRealm(stdTypeIdSeq)
    predicate.setDepartDataRealm(departIdSeq)
    predicate
  }

  def filter(entities: Iterable[_], 
      category: String, 
      stdTypeIdSeq: String, 
      departIdSeq: String) {
    if (null == entities || entities.isEmpty) return
    CollectUtils.filter(entities, getPredicate(category, entities.iterator().next(), stdTypeIdSeq, departIdSeq))
  }

  def filter(predicateName: String, 
      entities: Iterable[_], 
      stdTypeIdSeq: String, 
      departIdSeq: String) {
    if (null == entities || entities.isEmpty) return
    CollectUtils.filter(entities, getPredicate(predicateName, stdTypeIdSeq, departIdSeq))
  }

  def select(predicateName: String, 
      entities: List[_], 
      stdTypeIdSeq: String, 
      departIdSeq: String): List[_] = {
    CollectUtils.select(entities, getPredicate(predicateName, stdTypeIdSeq, departIdSeq))
  }

  def select(entities: List[_], 
      category: String, 
      stdTypeIdSeq: String, 
      departIdSeq: String): List[_] = {
    CollectUtils.select(entities, getPredicate(category, entities.iterator().next(), stdTypeIdSeq, departIdSeq))
  }

  def register(entityClass: Class[_], predicate: DataAuthorityPredicate) {
    predicates.put(entityClass.getName, predicate)
  }

  def register(predicateName: String, predicate: DataAuthorityPredicate) {
    predicates.put(predicateName, predicate)
  }
}
