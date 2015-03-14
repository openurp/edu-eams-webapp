package org.openurp.edu.eams.util.stat

import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set

import scala.collection.JavaConversions._

object StatUtils {

  def setValueToMap(key: String, 
      tempValue: AnyRef, 
      tempType: String, 
      m: Map[_,_]) {
    if ("integer" == tempType) {
      if (m.containsKey(key)) {
        m.put(key, new java.lang.Integer(m.get(key).asInstanceOf[java.lang.Integer].intValue() + 
          tempValue.asInstanceOf[java.lang.Integer].intValue()))
      } else {
        m.put(key, tempValue.asInstanceOf[java.lang.Integer])
      }
    } else if ("float" == tempType) {
      if (m.containsKey(key)) {
        m.put(key, new java.lang.Float(m.get(key).asInstanceOf[java.lang.Float].floatValue() + 
          tempValue.asInstanceOf[java.lang.Float].floatValue()))
      } else {
        m.put(key, (tempValue).asInstanceOf[java.lang.Float])
      }
    } else if ("list" == tempType) {
      var tempList = new ArrayList()
      if (m.containsKey(key)) {
        tempList = m.get(key).asInstanceOf[ArrayList]
      }
      tempList.add(tempValue)
      m.put(key, tempList)
    } else if ("set" == tempType) {
      var tempSet = new HashSet()
      if (m.containsKey(key)) {
        tempSet = m.get(key).asInstanceOf[HashSet]
      }
      tempSet.add(tempValue)
      m.put(key, tempSet)
    }
  }
}
