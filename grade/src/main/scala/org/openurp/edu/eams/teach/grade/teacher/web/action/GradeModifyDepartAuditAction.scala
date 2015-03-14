package org.openurp.edu.eams.teach.grade.teacher.web.action

import java.util.Date
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.security.blueprint.User
import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean.GradeModifyStatus
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

class GradeModifyDepartAuditAction extends GradeModifyAuditSearchAction {

  override def search(): String = {
    put("gradeModifyApplys", search(getQueryBuilder))
    put("status", GradeModifyStatus.valueOf(get("applyStatus")))
    forward()
  }

  def updateStatus(): String = {
    val applies = getModels(classOf[GradeModifyApply], getLongIds("gradeModifyApply"))
    val passed = getBool("passed")
    val date = new Date()
    val statuses = CollectUtils.newArrayList()
    statuses.add(GradeModifyStatus.FINAL_AUDIT_PASSED)
    statuses.add(GradeModifyStatus.FINAL_AUDIT_UNPASSED)
    statuses.add(GradeModifyStatus.GRADE_DELETED)
    statuses.add(GradeModifyStatus.ADMIN_AUDIT_PASSED)
    statuses.add(GradeModifyStatus.ADMIN_AUDIT_UNPASSED)
    val user = entityDao.get(classOf[User], getUserId)
    for (gradeModifyApply <- applies) {
      if (statuses.contains(gradeModifyApply.getStatus)) {
        //continue
      }
      gradeModifyApply.setAuditer(user.getFullname + "(" + user.getName + ")")
      gradeModifyApply.setUpdatedAt(date)
      gradeModifyApply.setStatus(if (passed) GradeModifyStatus.DEPART_AUDIT_PASSED else GradeModifyStatus.DEPART_AUDIT_UNPASSED)
      val courseGrade = gradeModifyApplyService.getCourseGrade(gradeModifyApply)
      if (null == courseGrade) {
        gradeModifyApply.setStatus(GradeModifyStatus.GRADE_DELETED)
      }
    }
    try {
      entityDao.saveOrUpdate(applies)
    } catch {
      case e: Exception => return redirect("search", "info.save.failure", get("params"))
    }
    redirect("search", "info.save.success", get("params"))
  }
}
