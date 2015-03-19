package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import java.util.LinkedHashMap


import org.beangle.commons.collection.page.PageLimit
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Arrays
import org.openurp.edu.base.Program



class CourseLimitGradeProvider extends AbstractCourseLimitContentProvider[String] {

  protected def getContentMap(content: Array[Serializable]): Map[String, String] = {
    val results = new LinkedHashMap[String, String]()
    for (value <- content) {
      val grade = value.asInstanceOf[String]
      results.put(grade, grade)
    }
    results
  }

  protected override def getOtherContents(content: Array[Serializable], term: String, limit: PageLimit): List[String] = {
    val builder = OqlBuilder.from(classOf[Program].getName + " program")
    if (!Arrays.isEmpty(content)) {
      builder.where("program.grade not in(:grades)", content)
    }
    if (null != term) {
      builder.where("program.grade like :grade", "%" + term + "%")
    }
    builder.select("distinct program.grade")
    builder.orderBy("grade")
    builder.limit(limit)
    entityDao.search(builder)
  }

  def getContentIdTitleMap(content: String): Map[String, String] = super.getContents(content)

  protected override def getCascadeContents(content: Array[Serializable], 
      term: String, 
      limit: PageLimit, 
      cascadeField: Map[Long, String]): List[String] = null
}
