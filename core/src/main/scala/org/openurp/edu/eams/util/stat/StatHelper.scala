package org.openurp.edu.eams.util.stat







import org.beangle.commons.dao.EntityDao
import org.beangle.data.model.Entity
import StatHelper._



object StatHelper {

  def replaceIdWith(datas: Iterable[_], clazzes: Array[Class[_]], entityDao: EntityDao) {
    var iter = datas.iterator()
    while (iter.hasNext) {
      val data = iter.next().asInstanceOf[Array[Any]]
      for (i <- 0 until clazzes.length) {
        if (null == clazzes(i)) //continue
        if (null != data(i)) {
          val id = data(i).asInstanceOf[Number].longValue()
          data(i) = entityDao.get(clazzes(i), new java.lang.Long(id))
        }
      }
    }
  }
}

class StatHelper(private var entityDao: EntityDao) {

  def this() {
    super()
  }

  private def setStatEntities(statMap: Map[_,_], entityClass: Class[_]): List[_] = {
    val entities = entityDao.get(entityClass, "id", statMap.keySet)
    var iter = entities.iterator()
    while (iter.hasNext) {
      val entity = iter.next().asInstanceOf[Entity[_]]
      val stat = statMap.get(entity.id).asInstanceOf[StatItem]
      stat.what=entity
    }
    new ArrayList(statMap.values)
  }

  private def buildStatMap(stats: Iterable[_]): Map[_,_] = {
    val statMap = new HashMap()
    var iter = stats.iterator()
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatItem]
      statMap.put(element.what, element)
    }
    statMap
  }

  def replaceIdWith(datas: Iterable[_], clazzes: Array[Class[_]]) {
    var iter = datas.iterator()
    while (iter.hasNext) {
      val data = iter.next().asInstanceOf[Array[Any]]
      for (i <- 0 until clazzes.length) {
        if (null == clazzes(i)) {
          //continue
        }
        if (null != data(i)) {
          val id = data(i).asInstanceOf[Number].longValue()
          data(i) = entityDao.get(clazzes(i), new java.lang.Long(id).intValue())
        }
      }
    }
  }

  def setStatEntities(stats: Iterable[_], entityClass: Class[_]): List[_] = {
    val statMap = buildStatMap(stats)
    setStatEntities(statMap, entityClass)
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
