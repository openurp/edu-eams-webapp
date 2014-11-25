package org.openurp.eams.grade.teacher.action

import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.teach.grade.CourseGrade
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.teach.lesson.Lesson
import org.beangle.data.model.dao.EntityDao
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.mapping
import org.beangle.webmvc.api.annotation.param
import org.openurp.eams.grade.CourseGradeState
import org.openurp.eams.grade.model.CourseGradeStateBean
import scala.collection.mutable.ListBuffer
import org.openurp.teach.code.GradeType

class IndexAction extends ActionSupport {
  var entityDao: EntityDao = _

  @mapping(value = "{id}")
  def info(@param("id") id: String): String = {
    val lesson = entityDao.get(classOf[Lesson], Integer.valueOf(id))
    put("lesson", lesson)

    val query = OqlBuilder.from(classOf[CourseGrade])
    query.where("courseGrade.lesson=:lesson", lesson)
    val grades = entityDao.search(query)
    put("grades", grades)

//    val query2 = OqlBuilder.from(classOf[CourseGradeState])
//    query2.where("courseGradeState.lesson=:lesson", lesson)
    //entityDao.search(query2)
    //gradeState.get(0)之后再put
    val gradeState = new CourseGradeStateBean
    
    //put("gradeState", gradeState)
    val gradeTypes = new ListBuffer[GradeType]
    if(null!=gradeState){
      gradeState.examStates .foreach(es => gradeTypes += es.gradeType)
      gradeState.gaStates .foreach(es => gradeTypes += es.gradeType)
    }
    put("gradeTypes",gradeTypes)
    forward()
  }
}