package org.openurp.edu.eams.teach.grade.adminclass.web.action

import java.util.Collections
import java.util.Date
import java.util.List
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.Grade
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.common.ProjectSupportAction

import scala.collection.JavaConversions._

class AllAction extends ProjectSupportAction {

  private def putGradeTypes() {
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType], GradeTypeConstants.USUAL_ID, GradeTypeConstants.MIDDLE_ID, 
      GradeTypeConstants.END_ID, GradeTypeConstants.MAKEUP_ID, GradeTypeConstants.DELAY_ID, GradeTypeConstants.GA_ID))
  }

  def gradeTypeEnabled(gt: GradeType): Boolean = {
    val nowDate = new Date()
    if (gt.getInvalidAt == null) {
      return true
    }
    if (gt.getInvalidAt == null && gt.getEffectiveAt == null) {
      return true
    }
    if (nowDate.before(gt.getInvalidAt) && gt.getEffectiveAt == null) {
      return true
    }
    if (gt.getEffectiveAt != null && nowDate.before(gt.getInvalidAt) && 
      nowDate.after(gt.getEffectiveAt)) {
      return true
    }
    false
  }

  def index(): String = {
    val adminclass = entityDao.get(classOf[Adminclass], getInt("adminclass.id"))
    put("adminclass", adminclass)
    var stdId = getLong("std.id")
    val stds = CollectUtils.newArrayList(adminclass.getStudents)
    Collections.sort(stds, new PropertyComparator("code"))
    if (null == stdId && !stds.isEmpty) {
      stdId = stds.get(0).getId
    }
    put("students", stds)
    if (null != stdId) {
      val std = entityDao.get(classOf[Student], stdId)
      val builder = OqlBuilder.from(classOf[CourseGrade], "courseGrade")
      builder.where("courseGrade.std = :std", std)
      builder.where("courseGrade.status = :status", Grade.Status.PUBLISHED)
      builder.orderBy(Order.parse("courseGrade.semester.beginOn desc"))
      put("grades", entityDao.search(builder))
      putGradeTypes()
      put("std", std)
    }
    forward()
  }
}
