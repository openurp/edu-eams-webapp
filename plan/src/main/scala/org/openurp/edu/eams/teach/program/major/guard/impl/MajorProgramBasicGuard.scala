package org.openurp.edu.eams.teach.program.major.guard.impl



import org.beangle.commons.collection.CollectUtils
import com.ekingstar.eams.base.Department
import com.ekingstar.eams.core.Project
import com.ekingstar.eams.core.code.industry.Education
import com.ekingstar.eams.core.code.school.StdType
import com.ekingstar.eams.exception.EamsException
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.exception.MajorProgramGuardException
import org.openurp.edu.eams.teach.program.major.guard.MajorProgramOperateType
//remove if not needed


class MajorProgramBasicGuard extends AbstractMajorProgramGuard {

  protected override def iWantGuard(operType: MajorProgramOperateType): Boolean = true

  protected override def doGuard(operType: MajorProgramOperateType, program: Program, context: Map[String, Any]) {
    if (program == null) {
      throw new MajorProgramGuardException("对不起，您没有选择要操作的培养计划")
    }
    val checkDataRealm = context.get("realm/checkMe").asInstanceOf[java.lang.Boolean]
    if (true == checkDataRealm) {
      val project = context.get("realm/project").asInstanceOf[Project]
      val departs = context.get("realm/departs").asInstanceOf[List[Department]]
      val educations = context.get("realm/educations").asInstanceOf[List[Education]]
      val stdTypes = context.get("realm/stdTypes").asInstanceOf[List[StdType]]
      if (program.getMajor.getProject != project) {
        throw new EamsException("对不起，您没有权限！")
      }
      if (CollectUtils.isEmpty(departs) || !departs.contains(program.getDepartment)) {
        throw new EamsException("对不起，您没有权限！")
      }
      if (CollectUtils.isEmpty(stdTypes) || !stdTypes.contains(program.getStdType)) {
        throw new EamsException("对不起，您没有权限！")
      }
      if (program.getEducation != null && 
        (CollectUtils.isEmpty(educations) || !educations.contains(program.getEducation))) {
        throw new EamsException("对不起，您没有权限！")
      }
    }
  }
}
