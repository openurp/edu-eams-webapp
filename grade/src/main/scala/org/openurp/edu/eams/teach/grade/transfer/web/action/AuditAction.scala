package org.openurp.edu.eams.teach.grade.transfer.web.action




import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.grade.course.web.action.StdGradeSearchAction
import org.openurp.edu.teach.grade.CourseGrade



class AuditAction extends StdGradeSearchAction {

  def getProjects(): List[Project] = {
    entityDao.get(classOf[Project], "minor", true)
  }

  def transfer(): String = {
    val courseGradeIdSeq = get("courseGradeIds")
    if (Strings.isEmpty(courseGradeIdSeq)) {
      return forwardError("error.parameters.needed")
    }
    val query1 = OqlBuilder.from(classOf[CourseGrade], "grade")
    query1.where("grade.id in (:gradeId)", Strings.splitToLong(courseGradeIdSeq))
    val courseGrades = entityDao.search(query1)
    val untransferGrades = CollectUtils.newArrayList()
    val toSaveGrades = CollectUtils.newArrayList()
    val cacheSemesterMap = CollectUtils.newHashMap()
    for (grade <- courseGrades) {
      val student = grade.getStd
      grade.setProject(student.getProject)
      val key = grade.getProject.getName + "_" + grade.getSemester.getSchoolYear + 
        "_" + 
        grade.getSemester.getName
      var semester = cacheSemesterMap.get(key)
      if (null == semester) {
        semester = semesterService.getSemester(grade.getProject, grade.getSemester.getSchoolYear, grade.getSemester.getName)
        cacheSemesterMap.put(key, semester)
      }
      grade.setSemester(semester)
      toSaveGrades.add(grade)
    }
    entityDao.saveOrUpdate(toSaveGrades)
    if (CollectUtils.isNotEmpty(untransferGrades)) {
      put("untransferGrades", untransferGrades)
      return forward("problemGrade", "info.action.portionOfSuccess.no_minor")
    }
    redirect("search", "info.action.success")
  }
}
