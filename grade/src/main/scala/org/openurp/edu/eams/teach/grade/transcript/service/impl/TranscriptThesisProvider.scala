package org.openurp.edu.eams.teach.grade.transcript.service.impl

import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.eams.teach.thesis.Thesis

import scala.collection.JavaConversions._

class TranscriptThesisProvider extends TranscriptDataProvider {

  private var entityDao: EntityDao = _

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val datas = CollectUtils.newHashMap()
    val query = OqlBuilder.from(classOf[Thesis], "thesis")
    query.where("thesis.std  in :stds", stds)
    val thesises = entityDao.search(query)
    for (thesis <- thesises) {
      datas.put(thesis.getStd, thesis.asInstanceOf[T])
    }
    datas
  }

  def getData[T](std: Student, options: Map[String, String]): T = {
    val query = OqlBuilder.from(classOf[Thesis], "thesis")
    query.where("thesis.std =:std", std)
    val thesises = entityDao.search(query)
    if (thesises.isEmpty) {
      null
    } else {
      thesises.get(0).asInstanceOf[T]
    }
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }

  def getDataName(): String = "thesises"
}
