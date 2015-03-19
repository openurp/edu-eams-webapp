package org.openurp.edu.eams.teach.election.service.rule.election.retake



import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.base.Semester
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectCourseSubstitution
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.eams.teach.election.service.rule.election.filter.AbstractElectableLessonFilter
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.lesson.Lesson
import RetakeCheatedChecker._



object RetakeCheatedChecker {

  val STATE_PARAM_CHECTED_COURSES = "CHECTED_COURSES"

  val STATE_PARAM_CHECTED_SUB_COURSES = "CHECTED_SUB_COURSES"

  private val RULE_PARAM_VIOLATIONS = "VIOLATIONS"

  private val RULE_PARAM_SEMESTER = "SEMESTER"
}

class RetakeCheatedChecker extends AbstractElectableLessonFilter with ElectRulePrepare {

  private var semesterService: SemesterService = _

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.CHECTED_COURSES)) {
      val cheatedCourseSubstitutionIds = CollectUtils.newHashSet()
      var examStatusNames: Array[String] = null
      val params = getParams(context.getState.getProfile(entityDao).getElectConfigs)
      var violationParam: RuleConfigParam = null
      var semesterParam: RuleConfigParam = null
      for (param <- params) {
        if (RULE_PARAM_VIOLATIONS == param.getParam.getName) {
          violationParam = param
        } else if (RULE_PARAM_SEMESTER == param.getParam.getName) {
          semesterParam = param
        }
      }
      if (violationParam == null) {
        violationParam = uniqueParam(context.getState.getProfile(entityDao).getElectConfigs)
      }
      if (null != violationParam && Strings.isNotBlank(violationParam.getValue)) {
        examStatusNames = Strings.split(Strings.trim(violationParam.getValue), ",")
      }
      val cheatedCourses = CollectUtils.newHashMap()
      if (null != examStatusNames) {
        val builder = OqlBuilder.from(classOf[CourseGrade].getName + " courseGrade")
        builder.select("select distinct courseGrade.course")
        builder.where("courseGrade.std=:std", context.getStudent)
        builder.join("courseGrade.examGrades", "examGrade")
        builder.where("examGrade.examStatus.name in(:violationNames)", examStatusNames)
        if (semesterParam != null) {
          val prevSemester = semesterService.getPrevSemester(context.getState.getSemester(entityDao))
          if (prevSemester != null) {
            builder.where("courseGrade.semester = :prevSemester", prevSemester)
          }
        }
        val cheatedCourseSet = CollectUtils.newHashSet(entityDao.search(builder))
        for (course <- cheatedCourseSet) {
          for (courseSubstitution <- context.getState.getCourseSubstitutions if courseSubstitution.getOrigins.contains(course.id)) {
            cheatedCourseSubstitutionIds.addAll(courseSubstitution.getSubstitutes)
          }
          val _course = Model.newInstance(classOf[Course], course.id)
          _course.setId(course.id)
          _course.setCode(course.getCode)
          _course.setName(course.getName)
          _course.setEngName(course.getEngName)
          _course.setCredits(course.getCredits)
          cheatedCourses.put(course.id, _course)
        }
      }
      context.getState.getParams.put(STATE_PARAM_CHECTED_COURSES, cheatedCourses)
      context.getState.getParams.put(STATE_PARAM_CHECTED_SUB_COURSES, cheatedCourseSubstitutionIds)
      context.addPreparedDataName(PreparedDataName.CHECTED_COURSES)
    }
  }

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    val cheatedCourses = state.getParams.get(STATE_PARAM_CHECTED_COURSES).asInstanceOf[Map[Long, Course]]
    val cheatedCourseSubstitutionIds = state.getParams.get(STATE_PARAM_CHECTED_SUB_COURSES).asInstanceOf[Set[Long]]
    !cheatedCourses.containsKey(lesson.getCourse.id) && 
      !cheatedCourseSubstitutionIds.contains(lesson.getCourse.id)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("有违纪记录,不能重修", ElectRuleType.ELECTION, false, context.getLesson))
    }
    result
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }
}
