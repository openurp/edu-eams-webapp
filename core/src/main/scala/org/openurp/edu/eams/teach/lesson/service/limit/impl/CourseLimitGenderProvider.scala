package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.Map
import java.util.Map.Entry
import java.util.Set
import org.beangle.ems.dictionary.service.BaseCodeService
import org.openurp.code.person.Gender

import scala.collection.JavaConversions._

class CourseLimitGenderProvider extends AbstractCourseLimitNamedEntityProvider[Gender, Integer] {

  protected var baseCodeService: BaseCodeService = _

  protected var excludedIds: Set[Integer] = new HashSet[Integer]()

  def setExcludedIds(excludedIds: Set[Integer]) {
    this.excludedIds = excludedIds
  }

  protected override def getContentMap(content: Array[Serializable]): Map[String, Gender] = {
    val contentMap = super.getContentMap(content)
    val results = new LinkedHashMap[String, Gender]()
    for ((key, value) <- contentMap) {
      val gender = value
      val id = gender.getId
      if (!excludedIds.contains(id)) {
        results.put(key, gender)
      }
    }
    results
  }
}
