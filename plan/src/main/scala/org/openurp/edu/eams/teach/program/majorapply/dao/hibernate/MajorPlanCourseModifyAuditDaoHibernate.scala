package org.openurp.edu.eams.teach.program.majorapply.dao.hibernate

import java.sql.Date
import org.beangle.orm.hibernate.HibernateEntityDao
import org.beangle.security.blueprint.User
import com.ekingstar.eams.teach.Course
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
import org.openurp.edu.eams.teach.program.major.MajorPlan
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.model.MajorPlanCourseBean
import org.openurp.edu.eams.teach.program.majorapply.dao.MajorPlanCourseModifyAuditDao
import org.openurp.edu.eams.teach.program.majorapply.exception.MajorPlanAuditException
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailAfterBean
import org.openurp.edu.eams.teach.program.majorapply.model.MajorPlanCourseModifyDetailBean
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseModifyAuditDaoHibernate extends HibernateEntityDao with MajorPlanCourseModifyAuditDao {

  private var planCourseCommonDao: PlanCourseCommonDao = _

  private var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def approved(apply: MajorPlanCourseModifyBean, assessor: User) {
    if (MajorPlanCourseModifyBean.INITREQUEST != apply.getFlag) {
      throw new MajorPlanAuditException("只能对待审核的申请进行审核")
    }
    val plan = get(classOf[MajorPlan], apply.getMajorPlan.getId)
    if (plan == null) {
      throw new MajorPlanAuditException("您要修改的专业培养计划已经不存在。")
    }
    if (MajorPlanCourseModifyBean.ADD == apply.getRequisitionType) {
      val after = apply.getNewPlanCourse
      val planCourse = new MajorPlanCourseBean()
      planCourse.setCourse(after.getCourse)
      planCourse.setTerms(PlanTermCreditTool.normalizeTerms(after.getTerms))
      planCourse.setRemark(after.getRemark)
      planCourse.setCompulsory(after.isCompulsory)
      val mg = get(classOf[MajorPlanCourseGroup], after.getFakeCourseGroup.getId)
      if (mg == null) {
        throw new MajorPlanAuditException("课程组不存在：" + after.getFakeCourseGroup.getCourseType.getName)
      }
      planCourse.setCourseGroup(mg)
      planCourse.setDepartment(after.getDepartment)
      planCourseCommonDao.addPlanCourse(planCourse, plan)
    } else if (MajorPlanCourseModifyBean.DELETE == apply.getRequisitionType) {
      val before = apply.getOldPlanCourse
      val course = before.getCourse
      val planCourse = planCourseCommonDao.getMajorPlanCourseByCourse(plan, course)
      if (planCourse == null) {
        throw new MajorPlanAuditException("课程不存在：" + course)
      }
      planCourseCommonDao.removePlanCourse(planCourse, plan)
    } else if (MajorPlanCourseModifyBean.MODIFY == apply.getRequisitionType) {
      val before = apply.getOldPlanCourse
      val planCourse = planCourseCommonDao.getMajorPlanCourseByCourse(plan, before.getCourse)
      if (planCourse == null) {
        throw new MajorPlanAuditException("课程不存在：" + before.getCourse)
      }
      val oldGroup = planCourse.getCourseGroup.asInstanceOf[MajorPlanCourseGroup]
      val after = apply.getNewPlanCourse
      planCourse.setCompulsory(after.isCompulsory)
      planCourse.setCourse(after.getCourse)
      planCourse.setTerms(after.getTerms)
      planCourse.setRemark(after.getRemark)
      val mg = get(classOf[MajorPlanCourseGroup], after.getFakeCourseGroup.getId)
      if (mg == null) {
        throw new MajorPlanAuditException("课程组不存在：" + after.getFakeCourseGroup.getCourseType.getName)
      }
      planCourse.setCourseGroup(mg)
      planCourse.setDepartment(after.getDepartment)
      planCourseCommonDao.updatePlanCourse(planCourse, plan)
      planCourseGroupCommonDao.saveOrUpdateCourseGroup(oldGroup)
    } else {
      throw new MajorPlanAuditException("错误的计划课程变更申请类型")
    }
    apply.setFlag(MajorPlanCourseModifyBean.ACCEPT)
    apply.setAssessor(assessor)
    apply.setReplyDate(new Date(System.currentTimeMillis()))
    saveOrUpdate(apply)
  }

  def rejected(apply: MajorPlanCourseModifyBean, assessor: User) {
    if (MajorPlanCourseModifyBean.INITREQUEST != apply.getFlag) {
      throw new MajorPlanAuditException("只能对待审核的申请进行审核")
    }
    apply.setFlag(MajorPlanCourseModifyBean.REFUSE)
    apply.setAssessor(assessor)
    apply.setReplyDate(new Date(System.currentTimeMillis()))
    saveOrUpdate(apply)
  }

  def setPlanCourseCommonDao(planCourseCommonDao: PlanCourseCommonDao) {
    this.planCourseCommonDao = planCourseCommonDao
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }
}
