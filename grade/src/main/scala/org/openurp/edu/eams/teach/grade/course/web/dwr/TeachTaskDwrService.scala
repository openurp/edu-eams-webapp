package org.openurp.edu.eams.teach.grade.course.web.dwr



import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.lesson.Lesson



class TeachTaskDwrService extends BaseServiceImpl {

  protected var semesterService: SemesterService = _

  private var courseGradeService: CourseGradeService = _

  def getTeachTask(taskSeqNo: String, 
      projectId: java.lang.Integer, 
      schoolYear: String, 
      semesterName: String): Map[String, Any] = {
    if (null == taskSeqNo || null == projectId || Strings.isEmpty(schoolYear) || 
      Strings.isEmpty(semesterName)) {
      return null
    }
    val project = entityDao.get(classOf[Project], projectId).asInstanceOf[Project]
    val semester = semesterService.getSemester(project, schoolYear, semesterName)
    getTeachTask(taskSeqNo, semester.id)
  }

  private def getTeachTask(taskSeqNo: String, semesterId: java.lang.Integer): Map[String, Any] = {
    if (!Strings.isEmpty(taskSeqNo) && null != semesterId) {
      val query = OqlBuilder.from(classOf[Lesson], "task").where(new Condition("task.seqNo like (:seqNo)", 
        taskSeqNo))
        .where(new Condition("task.semester.id = (:semesterId)", semesterId))
      val list = entityDao.search(query)
      if (list == null || list.isEmpty) {
        return null
      }
      val task = (list).get(0).asInstanceOf[Lesson]
      val taskMap = CollectUtils.newHashMap()
      taskMap.put("id", task.id)
      taskMap.put("course.code", task.getCourse.getCode)
      taskMap.put("course.name", task.getCourse.getName)
      val courseGradeState = courseGradeService.getState(task)
      if (null != courseGradeState) {
        taskMap.put("task.gradeState.precision", courseGradeState.getPrecision)
        taskMap.put("task.gradeState.markStyle.id", courseGradeState.getScoreMarkStyle.id)
        taskMap.put("task.gradeState.markStyle.name", courseGradeState.getScoreMarkStyle.getName)
      } else {
        var setted = false
        if (null != task.getCourse.getExamMode) {
          if (task.getCourse.getExamMode.id != ExamMode.NORMAL) {
            setted = true
            taskMap.put("task.gradeState.markStyle.id", ScoreMarkStyle.RANK_EN)
            taskMap.put("task.gradeState.markStyle.name", "英文等级制")
          }
        }
        if (!setted) {
          taskMap.put("task.gradeState.markStyle.id", ScoreMarkStyle.PERCENT)
          taskMap.put("task.gradeState.markStyle.name", "百分制")
        }
      }
      return taskMap
    }
    null
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setCourseGradeService(courseGradeService: CourseGradeService) {
    this.courseGradeService = courseGradeService
  }
}
