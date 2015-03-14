package org.openurp.edu.eams.util.stat

import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import java.util.Iterator
import java.util.List
import java.util.Map
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.entity.Entity
import StatHelper._

import scala.collection.JavaConversions._

object StatHelper {

  def replaceIdWith(datas: Collection[_], clazzes: Array[Class[_]], entityDao: EntityDao) {
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
      val entity = iter.next().asInstanceOf[Entity]
      val stat = statMap.get(entity.getId).asInstanceOf[StatItem]
      stat.setWhat(entity)
    }
    new ArrayList(statMap.values)
  }

  private def buildStatMap(stats: Collection[_]): Map[_,_] = {
    val statMap = new HashMap()
    var iter = stats.iterator()
    while (iter.hasNext) {
      val element = iter.next().asInstanceOf[StatItem]
      statMap.put(element.getWhat, element)
    }
    statMap
  }

  def replaceIdWith(datas: Collection[_], clazzes: Array[Class[_]]) {
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

  def setStatEntities(stats: Collection[_], entityClass: Class[_]): List[_] = {
    val statMap = buildStatMap(stats)
    setStatEntities(statMap, entityClass)
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
