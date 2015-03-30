package org.openurp.edu.eams.util.stat

import scala.collection.mutable.HashSet
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer
import org.beangle.commons.collection.Collections

object StatUtils {

  def setValueToMap(key: String,
    tempValue: AnyRef,
    tempType: String,
    m: collection.mutable.Map[Any, Any]) {
    if ("integer" == tempType) {
      if (m.contains(key)) {
        m.put(key, new java.lang.Integer(m.get(key).asInstanceOf[java.lang.Integer].intValue() +
          tempValue.asInstanceOf[java.lang.Integer].intValue()))
      } else {
        m.put(key, tempValue.asInstanceOf[java.lang.Integer])
      }
    } else if ("float" == tempType) {
      if (m.contains(key)) {
        m.put(key, new java.lang.Float(m.get(key).asInstanceOf[java.lang.Float].floatValue() +
          tempValue.asInstanceOf[java.lang.Float].floatValue()))
      } else {
        m.put(key, (tempValue).asInstanceOf[java.lang.Float])
      }
    } else if ("list" == tempType) {
      var tempList = new ListBuffer[Any]
      if (m.contains(key)) {
        tempList = m.get(key).asInstanceOf[ListBuffer[Any]]
      }
      tempList += tempValue
      m.put(key, tempList)
    } else if ("set" == tempType) {
      var tempSet = Collections.newSet[Any]
      if (m.contains(key)) {
        tempSet = m.get(key).asInstanceOf[collection.mutable.Set[Any]]
      }
      tempSet.add(tempValue)
      m.put(key, tempSet)
    }
  }
}
