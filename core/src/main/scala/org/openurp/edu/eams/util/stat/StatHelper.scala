package org.openurp.edu.eams.util.stat

import org.beangle.data.model.dao.EntityDao
import org.beangle.data.model.Entity
import StatHelper._
import org.beangle.commons.collection.Collections

object StatHelper {

  def replaceIdWith(datas: Iterable[_], clazzes: Array[Class[_]], entityDao: EntityDao) {
    var iter = datas.iterator
    while (iter.hasNext) {
      val data = iter.next().asInstanceOf[Array[Any]]
      for (i <- 0 until clazzes.length) {
        if (null == clazzes(i)) //continue
          if (null != data(i)) {
            val id = data(i).asInstanceOf[Number].longValue()
            val c = clazzes(i).asInstanceOf[Class[Entity[java.lang.Long]]]
            data(i) = entityDao.get(c, new java.lang.Long(id))
          }
      }
    }
  }
}

class StatHelper(private var entityDao: EntityDao) {

  private def setStatEntities(statMap: Map[java.io.Serializable, _], entityClass: Class[_]): Iterable[_] = {
    val clazz = entityClass.asInstanceOf[Class[Entity[java.io.Serializable]]]
    val entities = entityDao.find(clazz, statMap.keySet)
    var iter = entities.iterator
    while (iter.hasNext) {
      val entity = iter.next().asInstanceOf[Entity[java.io.Serializable]]
      val stat = statMap.get(entity.id).asInstanceOf[StatItem]
      stat.what = entity
    }
    statMap.values
  }

  private def buildStatMap(stats: Iterable[_]): collection.Map[Any, StatItem] = {
    val statMap = Collections.newMap[Any, StatItem]
    var iter = stats.iterator
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatItem]
      statMap.put(element.what, element)
    }
    statMap
  }

  def replaceIdWith(datas: Iterable[_], clazzes: Array[Class[_]]) {
    var iter = datas.iterator
    while (iter.hasNext) {
      val data = iter.next().asInstanceOf[Array[Any]]
      for (i <- 0 until clazzes.length if null != clazzes(i)) {
        if (null != data(i)) {
          val id = data(i).asInstanceOf[Number].longValue()
          val c = clazzes(i).asInstanceOf[Class[Entity[java.lang.Long]]]
          data(i) = entityDao.get(c, new java.lang.Long(id))
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
