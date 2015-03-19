package org.openurp.edu.eams.teach.grade.adminclass.web.action




import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.web.action.common.ProjectSupportAction



class AlertAction extends ProjectSupportAction {

  private def getAlertData(): Map[Student, List[CourseGrade]] = {
    var credits = getFloat("credits")
    if (null == credits || credits < 0.5) credits = new java.lang.Float(10.0)
    val adminclassId = getInt("adminclass.id")
    val stdId = getLong("std.id")
    val semester = entityDao.get(classOf[Semester], getInt("semester.id"))
    val gradeMap = CollectUtils.newHashMap()
    val builder = OqlBuilder.from(classOf[CourseGrade], "cg")
    builder.select("cg.std.id,cg.course.id,cg.course.credits")
      .groupBy("cg.std.id,cg.course.id,cg.course.credits")
      .where("cg.passed=false")
      .where("cg.status=" + Grade.Status.PUBLISHED)
      .where("not exists(from " + classOf[CourseGrade].getName + 
      " cg2 where cg2.std=cg.std and cg2.course=cg.course and cg2.id!=cg.id and cg2.passed=true " + 
      " and cg2.semester.beginOn<=:beginOn)", semester.beginOn)
      .where("cg.semester.beginOn<=:beginOn", semester.beginOn)
    if (null != adminclassId) {
      val adminclass = entityDao.get(classOf[Adminclass], adminclassId)
      builder.where("cg.std.adminclass =:adminclass", adminclass)
      put("adminclass", adminclass)
    } else if (null != stdId) {
      builder.where("cg.std =:std", entityDao.get(classOf[Student], stdId))
    }
    val statList = entityDao.search(builder)
    val stdCredits = CollectUtils.newHashMap()
    for (data <- statList) {
      val tuple = data.asInstanceOf[Array[Any]]
      var unpassed = stdCredits.get(tuple(0))
      unpassed = if (null == unpassed) java.lang.Float.valueOf(tuple(2).asInstanceOf[Number].floatValue()) else java.lang.Float.valueOf(unpassed.floatValue() + tuple(2).asInstanceOf[Number].floatValue())
      stdCredits.put(tuple(0), unpassed)
      if (unpassed.floatValue() > credits) {
        val std = entityDao.get(classOf[Student], tuple(0).asInstanceOf[java.lang.Long])
        gradeMap.put(std, getGrades(std, semester))
      }
    }
    put("semester", semester)
    put("credits", credits)
    gradeMap
  }

  def index(): String = {
    put("gradeMap", getAlertData)
    forward()
  }

  def report(): String = {
    put("gradeMap", getAlertData)
    forward()
  }

  private def getGrades(std: Student, semester: Semester): List[CourseGrade] = {
    val builder = OqlBuilder.from(classOf[CourseGrade], "cg")
    builder.where("cg.passed=false").where("cg.status=" + Grade.Status.PUBLISHED)
      .where("not exists(from " + classOf[CourseGrade].getName + 
      " cg2 where cg2.std=cg.std and cg2.course=cg.course and cg2.id!=cg.id " + 
      " and cg2.passed=true and cg2.semester.beginOn<=:beginOn)", semester.beginOn)
      .where("cg.std =:std and cg.semester.beginOn<=:beginOn", std, semester.beginOn)
    val courses = CollectUtils.newHashSet()
    val grades = CollectUtils.newArrayList()
    for (grade <- entityDao.search(builder) if !courses.contains(grade.getCourse)) {
      courses.add(grade.getCourse)
      grades.add(grade)
    }
    grades
  }
}
