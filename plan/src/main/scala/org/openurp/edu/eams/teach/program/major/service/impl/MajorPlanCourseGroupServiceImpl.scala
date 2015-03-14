package org.openurp.edu.eams.teach.program.major.service.impl

import java.util.Collections
import java.util.List
import java.util.Set
import org.apache.commons.lang3.StringUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.common.dao.PlanCourseGroupCommonDao
import org.openurp.edu.teach.plan.MajorPlanCourseGroup
import org.openurp.edu.eams.teach.program.major.service.MajorPlanCourseGroupService
import org.openurp.edu.eams.teach.program.major.service.MajorPlanService
import scala.reflect.{BeanProperty, BooleanBeanProperty}
//remove if not needed
import scala.collection.JavaConversions._

class MajorPlanCourseGroupServiceImpl extends BaseServiceImpl with MajorPlanCourseGroupService {

  @BeanProperty
  var majorPlanService: MajorPlanService = _

  private var planCourseGroupCommonDao: PlanCourseGroupCommonDao = _

  def removeCourseGroup(groupId: java.lang.Long) {
    val group = entityDao.get(classOf[MajorPlanCourseGroup], groupId)
    removeCourseGroup(group)
  }

  def removeCourseGroup(group: MajorPlanCourseGroup) {
    planCourseGroupCommonDao.removeCourseGroup(group)
  }

  def saveOrUpdateCourseGroup(group: MajorPlanCourseGroup) {
    planCourseGroupCommonDao.saveOrUpdateCourseGroup(group)
  }

  def courseGroupMoveDown(courseGroup: MajorPlanCourseGroup) {
    planCourseGroupCommonDao.updateCourseGroupMoveDown(courseGroup)
  }

  def courseGroupMoveUp(courseGroup: MajorPlanCourseGroup) {
    planCourseGroupCommonDao.updateCourseGroupMoveUp(courseGroup)
  }

  def setPlanCourseGroupCommonDao(planCourseGroupCommonDao: PlanCourseGroupCommonDao) {
    this.planCourseGroupCommonDao = planCourseGroupCommonDao
  }

  def move(node: CourseGroup, location: CourseGroup, index: Int) {
    if (Objects.==(node.getParent, location)) {
      if (Numbers.toInt(node.getIndexno) != index) {
        shiftCode(node, location, index)
      }
    } else {
      if (null != node.getParent) {
        node.getParent.getChildren.remove(node)
      }
      node.setParent(location)
      shiftCode(node, location, index)
    }
  }

  private def shiftCode(node: CourseGroup, newParent: CourseGroup, index: Int) {
    var sibling: List[CourseGroup] = null
    if (null != newParent) sibling = newParent.getChildren else {
      sibling = CollectUtils.newArrayList()
      for (m <- node.getPlan.getTopCourseGroups if null == m.getParent) sibling.add(m)
    }
    Collections.sort(sibling)
    sibling.remove(node)
    index -= 1
    if (index > sibling.size) {
      index = sibling.size
    }
    sibling.add(index, node)
    val nolength = String.valueOf(sibling.size).length
    val nodes = CollectUtils.newHashSet()
    var seqno = 1
    while (seqno <= sibling.size) {
      val one = sibling.get(seqno - 1)
      generateCode(one, Strings.leftPad(String.valueOf(seqno), nolength, '0'), nodes)
      seqno += 1
    }
    entityDao.saveOrUpdate(nodes)
    entityDao.refresh(node)
    entityDao.refresh(node.getPlan)
  }

  def genIndexno(group: CourseGroup, indexno: String) {
    if (null == group.getParent) {
      group.setIndexno(indexno)
    } else {
      if (StringUtils.isEmpty(indexno)) {
        group.setIndexno(Strings.concat(group.getParent.getIndexno, ".", String.valueOf(group.getIndex)))
      } else {
        group.setIndexno(Strings.concat(group.getParent.getIndexno, ".", indexno))
      }
    }
  }

  private def generateCode(node: CourseGroup, indexno: String, nodes: Set[CourseGroup]) {
    nodes.add(node)
    if (null != indexno) {
      genIndexno(node, indexno)
    } else {
      genIndexno(node, null)
    }
    if (null != node.getChildren) {
      for (m <- node.getChildren) {
        generateCode(m, null, nodes)
      }
    }
  }

  def hasSameGroupInOneLevel(courseGroup: CourseGroup, plan: CoursePlan, parent: CourseGroup): Boolean = {
    val builder = OqlBuilder.from(classOf[MajorPlanCourseGroup], "courseGroup")
    builder.where("courseGroup.courseType = :courseType", courseGroup.getCourseType)
    builder.where("courseGroup.plan = :plan", plan)
    if (courseGroup.isPersisted) {
      builder.where("courseGroup.id <> :groupId", courseGroup.getId)
    }
    if (parent == null) {
      builder.where("courseGroup.parent is null")
    } else {
      builder.where("courseGroup.parent = :parent", parent)
    }
    !entityDao.search(builder).isEmpty
  }
}
