package org.openurp.edu.eams.teach.grade.transcript.service.impl



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.eams.student.freshman.StdExaminee
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider



class TranscriptStdExamineeProvider extends BaseServiceImpl with TranscriptDataProvider {

  def getDataName(): String = "stdExaminees"

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val datas = CollectUtils.newHashMap()
    for (std <- stds) {
      datas.put(std, getData(std, options).asInstanceOf[T])
    }
    datas
  }

  def getData[T](std: Student, options: Map[String, String]): T = {
    val query = OqlBuilder.from(classOf[StdExaminee], "stdExaminee")
    query.where("stdExaminee.std=:std", std)
    val stdExaminees = entityDao.search(query)
    if (stdExaminees.isEmpty) {
      null
    } else {
      stdExaminees.get(0).asInstanceOf[T]
    }
  }
}
