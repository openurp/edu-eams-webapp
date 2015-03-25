package org.openurp.edu.eams.teach.program.student.web.action


import java.util.Locale
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.text.seq.SeqPattern
import com.ekingstar.eams.core.CommonAuditState
import com.ekingstar.eams.core.StdPerson
import com.ekingstar.eams.core.Student
import com.ekingstar.eams.core.service.StudentService
import com.ekingstar.eams.teach.code.school.CourseHourType
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.MajorCourseSubstitution
import org.openurp.edu.eams.teach.program.StdCourseSubstitution
import org.openurp.edu.eams.teach.program.StudentProgram
import org.openurp.edu.eams.teach.program.doc.model.ProgramDocBean
import org.openurp.edu.eams.teach.program.major.web.action.HanZi2SeqStyle
import org.openurp.edu.eams.teach.program.major.web.action.LuomaSeqStyle
import org.openurp.edu.eams.teach.program.service.CoursePlanProvider
import org.openurp.edu.eams.teach.program.service.CourseSubstitutionService
import com.ekingstar.eams.web.action.common.AbstractStudentProjectSupportAction
//remove if not needed


class MyPlanAction extends AbstractStudentProjectSupportAction {

  var coursePlanProvider: CoursePlanProvider = _

  var courseSubstitutionService: CourseSubstitutionService = _

  var studentService: StudentService = _

  def innerIndex(): String = {
    var acturalQueryStd = getLoginStudent
    val minorStudent = studentService.getMinorProjectStudent(acturalQueryStd.getPerson.asInstanceOf[StdPerson])
    if (minorStudent != null) {
      put("hasMinor", true)
    }
    val projectType = get("projectType")
    if ("MINOR" == projectType && minorStudent != null) {
      acturalQueryStd = minorStudent
    }
    val plan = coursePlanProvider.getCoursePlan(acturalQueryStd)
    if (plan == null) {
      addError("还没有属于您的培养计划。")
    }
    val majorCourseSubstitutions = courseSubstitutionService.getMajorCourseSubstitutions(acturalQueryStd)
    val stdCourseSubstitutions = courseSubstitutionService.getStdCourseSubstitutions(acturalQueryStd)
    put("plan", plan)
    put("loginStd", acturalQueryStd)
    put("courseHourTypes", baseCodeService.getCodes(classOf[CourseHourType]))
    put("majorCourseSubstitutions", majorCourseSubstitutions)
    put("stdCourseSubstitutions", stdCourseSubstitutions)
    forward()
  }

  def programDoc(): String = {
    val std = getLoginStudent
    val sp = entityDao.uniqueResult(OqlBuilder.from(classOf[StudentProgram], "sp").where("sp.std=:std", 
      std))
    if (null != sp && 
      sp.getProgram.getAuditState == CommonAuditState.ACCEPTED) {
      val request_locale = getLocale
      val builder = OqlBuilder.from(classOf[ProgramDocBean], "pd")
      builder.where("pd.program =:program", sp.getProgram)
      if (request_locale == null) {
        builder.where("pd.locale=:locale", new Locale("zh", "CN"))
      } else {
        builder.where("pd.locale=:locale", request_locale)
      }
      var seqPattern: SeqPattern = null
      seqPattern = if (request_locale == new Locale("zh", "CN")) new SeqPattern(new HanZi2SeqStyle(), 
        "{1}") else new SeqPattern(new LuomaSeqStyle(), "{1}")
      put("seqPattern", seqPattern)
      val docs = entityDao.search(builder)
      var doc: ProgramDocBean = null
      if (docs.size > 0) doc = docs.get(0)
      put("doc", doc)
    }
    forward()
  }
}
