package org.openurp.edu.eams.teach.grade.transcript.service.impl


import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.code.industry.OtherExamSubject
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.eams.teach.other.OtherGrade




class TranscriptOtherGradeProvider extends BaseServiceImpl with TranscriptDataProvider {

  
  var best: Boolean = true

  def getDatas[T](stds: List[Student], options: Map[String, String]): Map[Student, T] = {
    val datas = CollectUtils.newHashMap()
    for (std <- stds) {
      datas.put(std, getData(std, options).asInstanceOf[T])
    }
    datas
  }

  def getData[T](std: Student, options: Map[String, String]): T = {
    val query = OqlBuilder.from(classOf[OtherGrade], "grade")
    query.where("grade.std.id=" + std.id)
    query.orderBy("grade.subject.category.id,grade.score")
    val grades = entityDao.search(query)
    if (grades.isEmpty) {
      Collections.emptyList().asInstanceOf[T]
    } else {
      if (best) {
        val gradesMap = CollectUtils.newHashMap()
        for (grade <- grades) {
          gradesMap.put(grade.getSubject, grade)
        }
        gradesMap.values.asInstanceOf[T]
      } else {
        grades.asInstanceOf[T]
      }
    }
  }

  def getDataName(): String = "otherGrades"
}
