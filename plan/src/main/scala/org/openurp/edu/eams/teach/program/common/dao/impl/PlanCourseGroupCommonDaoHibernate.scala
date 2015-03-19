package org.openurp.edu.eams.teach.program.common.dao.impl




import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.data.jpa.hibernate.HibernateEntityDao
import com.ekingstar.eams.teach.Course
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.eams.teach.program.common.dao.PlanCommonDao
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.eams.teach.program.common.helper.ProgramHibernateClassGetter
import org.openurp.edu.eams.teach.program.helper.PlanTermCreditTool
import org.openurp.edu.teach.plan.MajorPlanCourse
import org.openurp.edu.teach.plan.MajorCourseGroup
import org.openurp.edu.eams.teach.program.util.PlanUtils
import PlanCourseGroupCommonDaoHibernate._
//remove if not needed


object PlanCourseGroupCommonDaoHibernate {

  protected def getPreOrderTraversalChildren(group: CourseGroup): List[CourseGroup] = {
    val result = CollectUtils.newArrayList()
    result.add(group)
    for (child <- group.getChildren) {
      result.addAll(getPreOrderTraversalChildren(child))
    }
    result
  }

  protected def reSortCourseGroups(plan: CoursePlan) {
    val tGroups = new ArrayList()
    for (courseGroup <- plan.getTopCourseGroups) {
      tGroups.addAll(getPreOrderTraversalChildren(courseGroup))
    }
    plan.getGroups.clear()
    plan.getGroups.addAll(tGroups)
  }

  protected def addGroup(plan: CoursePlan, group: CourseGroup) {
    if (null == plan.getGroups) {
      plan.setGroups(new ArrayList[CourseGroup]())
    }
    plan.getGroups.add(group)
    group.updateCoursePlan(plan)
  }
}

class PlanCourseGroupCommonDaoHibernate extends HibernateEntityDao with PlanCourseGroupCommonDao {

  private var planCommonDao: PlanCommonDao = _

  def saveOrUpdateCourseGroup(group: CourseGroup) {
    if (null != group.getParent) updateParentGroupCredits(group)
    group.getPlan.setCredits(planCommonDao.statPlanCredits(group.getPlan))
    saveOrUpdate(group.getPlan)
  }

  def updateCourseGroupParent(group: CourseGroup, newParent: CourseGroup, plan: CoursePlan) {
  }

  def addCourseGroupToPlan(group: CourseGroup, plan: CoursePlan) {
    group.setParent(null)
    addGroup(plan, group)
    saveOrUpdate(group)
    saveOrUpdate(plan)
    reSortCourseGroups(plan)
    updateGroupTreeCredits(getTopGroup(group))
    plan.setCredits(planCommonDao.statPlanCredits(plan))
    saveOrUpdate(plan)
  }

  def addCourseGroupToPlan(group: CourseGroup, parent: CourseGroup, plan: CoursePlan) {
    group.setParent(parent)
    if (parent != null) {
      parent.addChildGroup(group)
      saveOrUpdate(parent)
    }
    addGroup(plan, group)
    saveOrUpdate(group)
    saveOrUpdate(plan)
    reSortCourseGroups(plan)
    updateGroupTreeCredits(getTopGroup(group))
    plan.setCredits(planCommonDao.statPlanCredits(plan))
    saveOrUpdate(plan)
  }

  def removeCourseGroup(group: CourseGroup) {
    val plan = group.getPlan
    val parent = group.getParent
    val topGroup = getTopGroup(group)
    val isCompulsory = group.isCompulsory
    if (group.getChildren != null && group.getChildren.size > 0) {
      val t_children = new ArrayList[CourseGroup](group.getChildren)
      for (child <- t_children) {
        removeCourseGroup(child.asInstanceOf[MajorCourseGroup])
      }
    }
    if (parent != null) {
      parent.getChildren.remove(group)
      group.setParent(null)
    }
    plan.getGroups.remove(group)
    group.setPlan(null)
    reSortCourseGroups(plan)
    remove(group)
    if (parent == null) {
      plan.setCredits(planCommonDao.statPlanCredits(plan))
    } else {
      updateGroupTreeCredits(topGroup)
      plan.setCredits(planCommonDao.statPlanCredits(plan))
    }
    saveOrUpdate(plan)
  }

  def getTopGroup(group: CourseGroup): CourseGroup = {
    if (group.getParent != null) {
      return getTopGroup(group.getParent)
    }
    group
  }

  private def statGroupCredits(group: CourseGroup) {
    var credits = 0f
    var courseNum = 0
    val terms = group.getPlan.asInstanceOf[CoursePlan].getTermsCount
    var termCredits = Strings.repeat(",0", terms) + ","
    if (group.isCompulsory && 
      (CollectUtils.isNotEmpty(group.getChildren) || CollectUtils.isNotEmpty(group.getPlanCourses))) {
      for (child <- group.getChildren) {
        courseNum += child.getCourseNum
        credits += child.getCredits
        termCredits = PlanTermCreditTool.mergeTermCredits(termCredits, child.getTermCredits)
      }
      for (pcourse <- group.getPlanCourses if pcourse.isCompulsory) {
        courseNum += 1
        credits += pcourse.getCourse.getCredits
        termCredits = addCreditsInTerms(termCredits, pcourse)
      }
    } else {
      courseNum += group.getCourseNum
      termCredits = group.getTermCredits
      credits = group.getCredits
    }
    group.setCourseNum(courseNum)
    group.setTermCredits(termCredits)
    if (java.lang.Float.compare(credits, group.getCredits) > 0) {
      group.setCredits(credits)
    }
    saveOrUpdate(group)
  }

  def updateParentGroupCredits(group: CourseGroup) {
    val parent = group.getParent
    if (null != parent) {
      statGroupCredits(parent)
      updateParentGroupCredits(parent)
    }
  }

  def updateGroupTreeCredits(group: CourseGroup) {
    for (child <- group.getChildren) updateGroupTreeCredits(child)
    statGroupCredits(group)
  }

  private def addCreditsInTerms(termCredits: String, planCourse: PlanCourse): String = {
    val credits = termCredits.replaceAll("^,", "").replaceAll(",$", "")
      .split(",")
    val terms = planCourse.getTerms.replaceAll("^,", "").replaceAll(",$", "")
      .split(",")
    for (i <- 0 until terms.length) {
      if (terms(i) == "*") {
        //continue
      }
      try {
        val term = java.lang.Integer.valueOf(terms(i))
        if (term >= credits.length) {
          val credit = java.lang.Float.valueOf(credits(credits.length - 1)) + 
            planCourse.getCourse.getCredits
          credits(credits.length - 1) = String.valueOf(credit).replaceAll(".0$", "")
        } else {
          val credit = java.lang.Float.valueOf(credits(term - 1)) + planCourse.getCourse.getCredits
          credits(term - 1) = String.valueOf(credit).replaceAll(".0$", "")
        }
      } catch {
        case e: NumberFormatException => {
          val term = terms(i)
          credits(0) = term.replaceAll(".0$", "")
        }
      }
    }
    "," + Strings.join(credits, ",") + ","
  }

  def updateCourseGroupMoveDown(courseGroup: CourseGroup) {
    val plan = courseGroup.getPlan
    val parent = courseGroup.getParent
    if (null == parent) {
      val meInTopIndex = plan.getTopCourseGroups.indexOf(courseGroup)
      if (meInTopIndex == plan.getTopCourseGroups.size - 1) {
        throw new RuntimeException("CourseGroup cannot be moved down, because it's already the last one")
      }
      val meHindGroup = plan.getTopCourseGroups.get(meInTopIndex + 1).asInstanceOf[MajorCourseGroup]
      val meInPreOrderIndex = plan.getGroups.indexOf(courseGroup)
      val meHindGroupInPreOrderIndex = plan.getGroups.indexOf(meHindGroup)
      swap(plan.getGroups, meInPreOrderIndex, meHindGroupInPreOrderIndex)
    } else {
      val meIndex = parent.getChildren.indexOf(courseGroup)
      if (meIndex == parent.getChildren.size - 1) {
        throw new RuntimeException("CourseGroup cannot be moved down, because it's already the last one in it's parent CourseGroup")
      }
      swap(parent.getChildren, meIndex, meIndex + 1)
      saveOrUpdate(parent)
    }
    reSortCourseGroups(plan)
    saveOrUpdate(plan)
  }

  def updateCourseGroupMoveUp(courseGroup: CourseGroup) {
    val plan = courseGroup.getPlan
    val parent = courseGroup.getParent
    if (null == parent) {
      val meInTopIndex = plan.getTopCourseGroups.indexOf(courseGroup)
      if (meInTopIndex == 0) {
        throw new RuntimeException("CourseGroup cannot be moved up, because it's already the first one")
      }
      val meFrontGroup = plan.getTopCourseGroups.get(meInTopIndex - 1).asInstanceOf[MajorCourseGroup]
      val meInPreOrderIndex = plan.getGroups.indexOf(courseGroup)
      val meFrontGroupInPreOrderIndex = plan.getGroups.indexOf(meFrontGroup)
      swap(plan.getGroups, meInPreOrderIndex, meFrontGroupInPreOrderIndex)
    } else {
      val meIndex = parent.getChildren.indexOf(courseGroup)
      if (meIndex == 0) {
        throw new RuntimeException("CourseGroup cannot be moved up, because it's already the first one in it's parent CourseGroup")
      }
      swap(parent.getChildren, meIndex, meIndex - 1)
      saveOrUpdate(parent)
    }
    reSortCourseGroups(plan)
    saveOrUpdate(plan)
  }

  private def swap(anyList: List[_], index1: Int, index2: Int) {
    val o1 = anyList.get(index1)
    val o2 = anyList.get(index2)
    anyList.set(index2, o1)
    anyList.set(index1, o2)
  }

  def getCourseGroupByCourseType(planGroup: CourseGroup, planId: java.lang.Long, courseTypeId: java.lang.Integer): MajorCourseGroup = {
    val oql = OqlBuilder.from(ProgramHibernateClassGetter.hibernateClass(planGroup), "cgroup")
    oql.where("cgroup.courseType.id = :typeId", courseTypeId)
    oql.where("cgroup.plan.id = :planId", planId)
    val l = search(oql)
    if (l != null && l.size > 0) {
      return l.get(0)
    }
    null
  }

  def extractCourseInCourseGroup(group: MajorCourseGroup, terms: String): List[Course] = {
    val courses = new HashSet[Course]()
    val findTerm = Strings.splitNumSeq(terms)
    if (null == findTerm || findTerm.length == 0) {
      return Collections.EMPTY_LIST
    }
    for (i <- 0 until findTerm.length; planCourse <- PlanUtils.getPlanCourses(group, findTerm(i))) {
      courses.add(planCourse.getCourse)
    }
    new ArrayList[Course](courses)
  }

  def extractPlanCourseInCourseGroup(group: MajorCourseGroup, terms: Set[String]): List[MajorPlanCourse] = {
    val result = CollectUtils.newHashSet()
    for (term <- terms) {
      result.addAll(PlanUtils.getPlanCourses(group, java.lang.Integer.valueOf(term.asInstanceOf[String])).asInstanceOf[List[_]])
    }
    new ArrayList[MajorPlanCourse](result)
  }

  def setPlanCommonDao(planCommonDao: PlanCommonDao) {
    this.planCommonDao = planCommonDao
  }
}
