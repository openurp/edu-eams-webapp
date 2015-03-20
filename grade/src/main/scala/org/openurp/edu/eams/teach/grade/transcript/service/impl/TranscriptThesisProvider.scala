package org.openurp.edu.eams.teach.grade.transcript.service.impl



import org.beangle.commons.collection.CollectUtils
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.eams.teach.thesis.Thesis



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
