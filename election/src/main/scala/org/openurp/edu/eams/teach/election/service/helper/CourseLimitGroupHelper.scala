package org.openurp.edu.eams.teach.election.service.helper

import java.util.Comparator


import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import CourseLimitGroupComparator._




object CourseLimitGroupHelper {

  
  var comparator: CourseLimitGroupComparator = new CourseLimitGroupComparator()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    null != getMatchCourseLimitGroup(lesson, state)
  }

  def isElectable(lesson: Lesson, state: ElectState, types: Iterable[CourseLimitMetaEnum]): Boolean = {
    null != getMatchCourseLimitGroup(lesson, state, types)
  }

  def isElectable(lesson: Lesson, state: ElectState, types: CourseLimitMetaEnum*): Boolean = {
    null != getMatchCourseLimitGroup(lesson, state, types)
  }

  def getMatchCourseLimitGroup(lesson: Lesson, state: ElectState, types: Iterable[CourseLimitMetaEnum]): CourseLimitGroup = {
    val metaIds = CollectUtils.newHashSet()
    if (null != types) {
      for (metaEnum <- types) {
        metaIds.add(metaEnum.getMetaId)
      }
    }
    getMatchCourseLimitGroup(lesson, state, metaIds)
  }

  def getMatchCourseLimitGroup(lesson: Lesson, state: ElectState, types: CourseLimitMetaEnum*): CourseLimitGroup = {
    val metaIds = CollectUtils.newHashSet()
    if (null != types) {
      for (metaEnum <- types) {
        metaIds.add(metaEnum.getMetaId)
      }
    }
    getMatchCourseLimitGroup(lesson, state, metaIds)
  }

  def getMatchCountCourseLimitGroup(lesson: Lesson, state: ElectState): CourseLimitGroup = {
    val groups = lesson.getTeachClass.getLimitGroups
    Collections.sort(groups, comparator)
    for (group <- lesson.getTeachClass.getLimitGroups) {
      var groupPass = true
      if (group.getMaxCount != 0 && group.getCurCount >= group.getMaxCount) {
        //continue
      }
      for (item <- group.getItems) {
        var itemPass = true
        val op = item.getOperator
        val metaId = item.getMeta.id
        val values = CollectUtils.newHashSet(item.getContent.split(","))
        val sb = new StringBuilder()
        if (metaId == CourseLimitMetaEnum.ADMINCLASS.getMetaId) {
          sb.append(state.getStd.getAdminclassId)
        } else if (metaId == CourseLimitMetaEnum.DEPARTMENT.getMetaId) {
          sb.append(state.getStd.getDepartId)
        } else if (metaId == CourseLimitMetaEnum.DIRECTION.getMetaId) {
          sb.append(state.getStd.directionId)
        } else if (metaId == CourseLimitMetaEnum.EDUCATION.getMetaId) {
          sb.append(state.getStd.educationId)
        } else if (metaId == CourseLimitMetaEnum.GENDER.getMetaId) {
          sb.append(state.getStd.getGenderId)
        } else if (metaId == CourseLimitMetaEnum.GRADE.getMetaId) {
          sb.append(state.getStd.grade)
        } else if (metaId == CourseLimitMetaEnum.MAJOR.getMetaId) {
          sb.append(state.getStd.majorId)
        } else if (metaId == CourseLimitMetaEnum.STDTYPE.getMetaId) {
          sb.append(state.getStd.stdTypeId)
        } else if (metaId == CourseLimitMetaEnum.PROGRAM.getMetaId) {
          sb.append(state.getStd.getProgramId)
        }
        val value = sb.toString
        itemPass = if (op == Operator.EQUAL || op == Operator.IN) values.isEmpty || values.contains(value) else !values.isEmpty && !values.contains(value)
        if (!itemPass) {
          groupPass = false
          //break
        }
      }
      if (groupPass) {
        return group
      }
    }
    null
  }

  def getMatchCourseLimitGroup(lesson: Lesson, state: ElectState): CourseLimitGroup = {
    val emptySet = Collections.emptySet()
    getMatchCourseLimitGroup(lesson, state, emptySet)
  }

  def getMatchCourseLimitGroup(lesson: Lesson, state: ElectState, types: Set[Long]): CourseLimitGroup = {
    val groups = lesson.getTeachClass.getLimitGroups
    Collections.sort(groups, comparator)
    for (group <- lesson.getTeachClass.getLimitGroups) {
      var groupPass = true
      for (item <- group.getItems) {
        var itemPass = true
        val op = item.getOperator
        val metaId = item.getMeta.id
        if (CollectUtils.isNotEmpty(types) && !types.contains(metaId)) {
          //continue
        }
        val values = CollectUtils.newHashSet(item.getContent.split(","))
        val sb = new StringBuilder()
        if (metaId == CourseLimitMetaEnum.ADMINCLASS.getMetaId) {
          sb.append(state.getStd.getAdminclassId)
        } else if (metaId == CourseLimitMetaEnum.DEPARTMENT.getMetaId) {
          sb.append(state.getStd.getDepartId)
        } else if (metaId == CourseLimitMetaEnum.DIRECTION.getMetaId) {
          sb.append(state.getStd.directionId)
        } else if (metaId == CourseLimitMetaEnum.EDUCATION.getMetaId) {
          sb.append(state.getStd.educationId)
        } else if (metaId == CourseLimitMetaEnum.GENDER.getMetaId) {
          sb.append(state.getStd.getGenderId)
        } else if (metaId == CourseLimitMetaEnum.GRADE.getMetaId) {
          sb.append(state.getStd.grade)
        } else if (metaId == CourseLimitMetaEnum.MAJOR.getMetaId) {
          sb.append(state.getStd.majorId)
        } else if (metaId == CourseLimitMetaEnum.STDTYPE.getMetaId) {
          sb.append(state.getStd.stdTypeId)
        } else if (metaId == CourseLimitMetaEnum.PROGRAM.getMetaId) {
          sb.append(state.getStd.getProgramId)
        }
        val value = sb.toString
        itemPass = if (op == Operator.EQUAL || op == Operator.IN) values.isEmpty || values.contains(value) else !values.isEmpty && !values.contains(value)
        if (!itemPass) {
          groupPass = false
          //break
        }
      }
      if (groupPass) {
        return group
      }
    }
    null
  }

  def getMatchCourseLimitGroup(lesson: Lesson, student: Student): CourseLimitGroup = {
    val groups = lesson.getTeachClass.getLimitGroups
    Collections.sort(groups, comparator)
    for (group <- lesson.getTeachClass.getLimitGroups) {
      var groupPass = true
      for (item <- group.getItems) {
        var itemPass = true
        val op = item.getOperator
        val metaId = item.getMeta.id
        val values = CollectUtils.newHashSet(item.getContent.split(","))
        var value: String = null
        if (metaId == CourseLimitMetaEnum.ADMINCLASS.getMetaId) {
          value = if (student.getAdminclass == null) "" else student.getAdminclass.id + ""
        } else if (metaId == CourseLimitMetaEnum.DEPARTMENT.getMetaId) {
          value = student.department.id + ""
        } else if (metaId == CourseLimitMetaEnum.DIRECTION.getMetaId) {
          value = if (student.direction == null) "" else student.direction.id + ""
        } else if (metaId == CourseLimitMetaEnum.EDUCATION.getMetaId) {
          value = student.education.id + ""
        } else if (metaId == CourseLimitMetaEnum.GENDER.getMetaId) {
          value = student.getGender.id + ""
        } else if (metaId == CourseLimitMetaEnum.GRADE.getMetaId) {
          value = student.grade
        } else if (metaId == CourseLimitMetaEnum.MAJOR.getMetaId) {
          value = student.major.id + ""
        } else if (metaId == CourseLimitMetaEnum.STDTYPE.getMetaId) {
          value = student.getType.id + ""
        }
        itemPass = if (op == Operator.EQUAL || op == Operator.IN) values.isEmpty || values.contains(value) else !values.isEmpty && !values.contains(value)
        if (!itemPass) {
          groupPass = false
          //break
        }
      }
      if (groupPass) {
        return group
      }
    }
    null
  }

  object CourseLimitGroupComparator {

    private val MAXPRIORITY = 20000

    private val HIGHPRIORITY = 10000

    private val NORMALPRIORITY = 5000

    private val LOWPRIORITY = 2500

    private val ZEROPRIORITY = 500
  }

  private class CourseLimitGroupComparator extends Comparator[CourseLimitGroup] {

    private var CourseLimitMetaIds: List[Long] = CollectUtils.newArrayList(CourseLimitMetaEnum.ADMINCLASS.getMetaId, 
      CourseLimitMetaEnum.DIRECTION.getMetaId, CourseLimitMetaEnum.PROGRAM.getMetaId, CourseLimitMetaEnum.MAJOR.getMetaId, 
      CourseLimitMetaEnum.DEPARTMENT.getMetaId)

    def compare(o1: CourseLimitGroup, o2: CourseLimitGroup): Int = {
      val priorty1 = getPriority(o1)
      val priorty2 = getPriority(o2)
      if (priorty1 == priorty2) {
        (o2.getMaxCount - o2.getCurCount) - (o1.getMaxCount - o1.getCurCount)
      } else {
        priorty2 - priorty1
      }
    }

    private def getPriority(group: CourseLimitGroup): Int = {
      var priority = 0
      val hasMax = false
      for (courseLimitItem <- group.getItems) {
        if (Operator.NOT_IN == courseLimitItem.getOperator || Operator.NOT_EQUAL == courseLimitItem.getOperator) {
          if (CourseLimitMetaEnum.PROGRAM.getMetaId == courseLimitItem.getMeta.id || 
            CourseLimitMetaEnum.ADMINCLASS.getMetaId == courseLimitItem.getMeta.id) {
            if (!hasMax) {
              priority += MAXPRIORITY / 2
            }
          } else if (CourseLimitMetaEnum.DIRECTION.getMetaId == courseLimitItem.getMeta.id) {
            priority += HIGHPRIORITY / 2
          } else if (CourseLimitMetaEnum.MAJOR.getMetaId == courseLimitItem.getMeta.id) {
            priority += NORMALPRIORITY / 2
          } else if (CourseLimitMetaEnum.DEPARTMENT.getMetaId == courseLimitItem.getMeta.id) {
            priority += LOWPRIORITY / 2
          } else if (!CourseLimitMetaIds.contains(courseLimitItem.getMeta.id)) {
            priority += ZEROPRIORITY / 2
          }
        } else {
          if (CourseLimitMetaEnum.PROGRAM.getMetaId == courseLimitItem.getMeta.id || 
            CourseLimitMetaEnum.ADMINCLASS.getMetaId == courseLimitItem.getMeta.id) {
            if (!hasMax) {
              priority += MAXPRIORITY
            }
          } else if (CourseLimitMetaEnum.DIRECTION.getMetaId == courseLimitItem.getMeta.id) {
            priority += HIGHPRIORITY
          } else if (CourseLimitMetaEnum.MAJOR.getMetaId == courseLimitItem.getMeta.id) {
            priority += NORMALPRIORITY
          } else if (CourseLimitMetaEnum.DEPARTMENT.getMetaId == courseLimitItem.getMeta.id) {
            priority += LOWPRIORITY
          } else if (!CourseLimitMetaIds.contains(courseLimitItem.getMeta.id)) {
            priority += ZEROPRIORITY
          }
        }
      }
      priority
    }
  }
}
