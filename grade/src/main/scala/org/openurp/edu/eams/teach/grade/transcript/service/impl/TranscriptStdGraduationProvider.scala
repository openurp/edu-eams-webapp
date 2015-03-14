package org.openurp.edu.eams.teach.grade.transcript.service.impl

import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.eams.student.graduation.StdGraduation
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider

import scala.collection.JavaConversions._

class TranscriptStdGraduationProvider extends BaseServiceImpl with TranscriptDataProvider {

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val datas = CollectUtils.newHashMap()
    for (std <- stds) {
      datas.put(std, getData(std, options).asInstanceOf[T])
    }
    datas
  }

  def getData[T](std: Student, options: Map[String, String]): T = {
    val query = OqlBuilder.from(classOf[StdGraduation], "stdGraduation")
    query.where("stdGraduation.std =:std", std)
    val stdGraduations = entityDao.search(query)
    if (stdGraduations.isEmpty) {
      null
    } else {
      stdGraduations.get(0).asInstanceOf[T]
    }
  }

  def getDataName(): String = "stdGraduations"
}
