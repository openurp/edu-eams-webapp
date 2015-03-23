package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable

import java.util.LinkedHashMap

import org.openurp.code.person.Gender

class LessonLimitGenderProvider extends AbstractLessonLimitNamedEntityProvider[Gender, Integer] {


  protected var excludedIds: Set[Integer] = new HashSet[Integer]()

  def setExcludedIds(excludedIds: Set[Integer]) {
    this.excludedIds = excludedIds
  }

  protected override def getContentMap(content: Array[Serializable]): Map[String, Gender] = {
    val contentMap = super.getContentMap(content)
    val results = new LinkedHashMap[String, Gender]()
    for ((key, value) <- contentMap) {
      val gender = value
      val id = gender.id
      if (!excludedIds.contains(id)) {
        results.put(key, gender)
      }
    }
    results
  }
}
