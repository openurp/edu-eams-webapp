package org.openurp.edu.eams.teach.election.service.helper

import java.util.Comparator


import org.beangle.commons.collection.Collections
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operator
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum
import LessonLimitGroupComparator._




object LessonLimitGroupHelper {

  
  var comparator: LessonLimitGroupComparator = new LessonLimitGroupComparator()

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    null != getMatchLessonLimitGroup(lesson, state)
  }

  def isElectable(lesson: Lesson, state: ElectState, types: Iterable[LessonLimitMetaEnum]): Boolean = {
    null != getMatchLessonLimitGroup(lesson, state, types)
  }

  def isElectable(lesson: Lesson, state: ElectState, types: LessonLimitMetaEnum*): Boolean = {
    null != getMatchLessonLimitGroup(lesson, state, types)
  }

  def getMatchLessonLimitGroup(lesson: Lesson, state: ElectState, types: Iterable[LessonLimitMetaEnum]): LessonLimitGroup = {
    val metaIds = Collections.newSet[Any]
    if (null != types) {
      for (metaEnum <- types) {
        metaIds.add(metaEnum.getMetaId)
      }
    }
    getMatchLessonLimitGroup(lesson, state, metaIds)
  }

  def getMatchLessonLimitGroup(lesson: Lesson, state: ElectState, types: LessonLimitMetaEnum*): LessonLimitGroup = {
    val metaIds = Collections.newSet[Any]
    if (null != types) {
      for (metaEnum <- types) {
        metaIds.add(metaEnum.getMetaId)
      }
    }
    getMatchLessonLimitGroup(lesson, state, metaIds)
  }

  def getMatchCountLessonLimitGroup(lesson: Lesson, state: ElectState): LessonLimitGroup = {
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
        val values = Collections.newHashSet(item.getContent.split(","))
        val sb = new StringBuilder()
        if (metaId == LessonLimitMeta.Adminclass.getMetaId) {
          sb.append(state.getStd.getAdminclassId)
        } else if (metaId == LessonLimitMeta.Department.getMetaId) {
          sb.append(state.getStd.getDepartId)
        } else if (metaId == LessonLimitMeta.Direction.getMetaId) {
          sb.append(state.getStd.directionId)
        } else if (metaId == LessonLimitMeta.Education.getMetaId) {
          sb.append(state.getStd.educationId)
        } else if (metaId == LessonLimitMeta.Gender.getMetaId) {
          sb.append(state.getStd.getGenderId)
        } else if (metaId == LessonLimitMeta.Grade.getMetaId) {
          sb.append(state.getStd.grade)
        } else if (metaId == LessonLimitMeta.Major.getMetaId) {
          sb.append(state.getStd.majorId)
        } else if (metaId == LessonLimitMeta.StdType.getMetaId) {
          sb.append(state.getStd.stdTypeId)
        } else if (metaId == LessonLimitMeta.Program.getMetaId) {
          sb.append(state.getStd.getProgramId)
        }
        val value = sb.toString
        itemPass = if (op == Operator.Equals || op == Operator.IN) values.isEmpty || values.contains(value) else !values.isEmpty && !values.contains(value)
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

  def getMatchLessonLimitGroup(lesson: Lesson, state: ElectState): LessonLimitGroup = {
    val emptySet = Collections.emptySet()
    getMatchLessonLimitGroup(lesson, state, emptySet)
  }

  def getMatchLessonLimitGroup(lesson: Lesson, state: ElectState, types: Set[Long]): LessonLimitGroup = {
    val groups = lesson.getTeachClass.getLimitGroups
    Collections.sort(groups, comparator)
    for (group <- lesson.getTeachClass.getLimitGroups) {
      var groupPass = true
      for (item <- group.getItems) {
        var itemPass = true
        val op = item.getOperator
        val metaId = item.getMeta.id
        if (Collections.isNotEmpty(types) && !types.contains(metaId)) {
          //continue
        }
        val values = Collections.newHashSet(item.getContent.split(","))
        val sb = new StringBuilder()
        if (metaId == LessonLimitMeta.Adminclass.getMetaId) {
          sb.append(state.getStd.getAdminclassId)
        } else if (metaId == LessonLimitMeta.Department.getMetaId) {
          sb.append(state.getStd.getDepartId)
        } else if (metaId == LessonLimitMeta.Direction.getMetaId) {
          sb.append(state.getStd.directionId)
        } else if (metaId == LessonLimitMeta.Education.getMetaId) {
          sb.append(state.getStd.educationId)
        } else if (metaId == LessonLimitMeta.Gender.getMetaId) {
          sb.append(state.getStd.getGenderId)
        } else if (metaId == LessonLimitMeta.Grade.getMetaId) {
          sb.append(state.getStd.grade)
        } else if (metaId == LessonLimitMeta.Major.getMetaId) {
          sb.append(state.getStd.majorId)
        } else if (metaId == LessonLimitMeta.StdType.getMetaId) {
          sb.append(state.getStd.stdTypeId)
        } else if (metaId == LessonLimitMeta.Program.getMetaId) {
          sb.append(state.getStd.getProgramId)
        }
        val value = sb.toString
        itemPass = if (op == Operator.Equals || op == Operator.IN) values.isEmpty || values.contains(value) else !values.isEmpty && !values.contains(value)
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

  def getMatchLessonLimitGroup(lesson: Lesson, student: Student): LessonLimitGroup = {
    val groups = lesson.getTeachClass.getLimitGroups
    Collections.sort(groups, comparator)
    for (group <- lesson.getTeachClass.getLimitGroups) {
      var groupPass = true
      for (item <- group.getItems) {
        var itemPass = true
        val op = item.getOperator
        val metaId = item.getMeta.id
        val values = Collections.newHashSet(item.getContent.split(","))
        var value: String = null
        if (metaId == LessonLimitMeta.Adminclass.getMetaId) {
          value = if (student.getAdminclass == null) "" else student.getAdminclass.id + ""
        } else if (metaId == LessonLimitMeta.Department.getMetaId) {
          value = student.department.id + ""
        } else if (metaId == LessonLimitMeta.Direction.getMetaId) {
          value = if (student.direction == null) "" else student.direction.id + ""
        } else if (metaId == LessonLimitMeta.Education.getMetaId) {
          value = student.education.id + ""
        } else if (metaId == LessonLimitMeta.Gender.getMetaId) {
          value = student.getGender.id + ""
        } else if (metaId == LessonLimitMeta.Grade.getMetaId) {
          value = student.grade
        } else if (metaId == LessonLimitMeta.Major.getMetaId) {
          value = student.major.id + ""
        } else if (metaId == LessonLimitMeta.StdType.getMetaId) {
          value = student.getType.id + ""
        }
        itemPass = if (op == Operator.Equals || op == Operator.IN) values.isEmpty || values.contains(value) else !values.isEmpty && !values.contains(value)
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

  object LessonLimitGroupComparator {

    private val MAXPRIORITY = 20000

    private val HIGHPRIORITY = 10000

    private val NORMALPRIORITY = 5000

    private val LOWPRIORITY = 2500

    private val ZEROPRIORITY = 500
  }

  private class LessonLimitGroupComparator extends Comparator[LessonLimitGroup] {

    private var LessonLimitMetaIds: List[Long] = Collections.newBuffer[Any](LessonLimitMeta.Adminclass.getMetaId, 
      LessonLimitMeta.Direction.getMetaId, LessonLimitMeta.Program.getMetaId, LessonLimitMeta.Major.getMetaId, 
      LessonLimitMeta.Department.getMetaId)

    def compare(o1: LessonLimitGroup, o2: LessonLimitGroup): Int = {
      val priorty1 = getPriority(o1)
      val priorty2 = getPriority(o2)
      if (priorty1 == priorty2) {
        (o2.getMaxCount - o2.getCurCount) - (o1.getMaxCount - o1.getCurCount)
      } else {
        priorty2 - priorty1
      }
    }

    private def getPriority(group: LessonLimitGroup): Int = {
      var priority = 0
      val hasMax = false
      for (lessonLimitItem <- group.getItems) {
        if (Operator.NOT_IN == lessonLimitItem.getOperator || Operator.NOT_EQUAL == lessonLimitItem.getOperator) {
          if (LessonLimitMeta.Program.getMetaId == lessonLimitItem.getMeta.id || 
            LessonLimitMeta.Adminclass.getMetaId == lessonLimitItem.getMeta.id) {
            if (!hasMax) {
              priority += MAXPRIORITY / 2
            }
          } else if (LessonLimitMeta.Direction.getMetaId == lessonLimitItem.getMeta.id) {
            priority += HIGHPRIORITY / 2
          } else if (LessonLimitMeta.Major.getMetaId == lessonLimitItem.getMeta.id) {
            priority += NORMALPRIORITY / 2
          } else if (LessonLimitMeta.Department.getMetaId == lessonLimitItem.getMeta.id) {
            priority += LOWPRIORITY / 2
          } else if (!LessonLimitMetaIds.contains(lessonLimitItem.getMeta.id)) {
            priority += ZEROPRIORITY / 2
          }
        } else {
          if (LessonLimitMeta.Program.getMetaId == lessonLimitItem.getMeta.id || 
            LessonLimitMeta.Adminclass.getMetaId == lessonLimitItem.getMeta.id) {
            if (!hasMax) {
              priority += MAXPRIORITY
            }
          } else if (LessonLimitMeta.Direction.getMetaId == lessonLimitItem.getMeta.id) {
            priority += HIGHPRIORITY
          } else if (LessonLimitMeta.Major.getMetaId == lessonLimitItem.getMeta.id) {
            priority += NORMALPRIORITY
          } else if (LessonLimitMeta.Department.getMetaId == lessonLimitItem.getMeta.id) {
            priority += LOWPRIORITY
          } else if (!LessonLimitMetaIds.contains(lessonLimitItem.getMeta.id)) {
            priority += ZEROPRIORITY
          }
        }
      }
      priority
    }
  }
}
