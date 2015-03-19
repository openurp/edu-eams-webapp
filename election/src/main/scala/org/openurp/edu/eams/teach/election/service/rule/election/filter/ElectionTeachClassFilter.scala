package org.openurp.edu.eams.teach.election.service.rule.election.filter

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.beangle.ems.rule.model.RuleConfig
import org.beangle.ems.rule.model.RuleConfigParam
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectMessage
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext.PreparedDataName
import org.openurp.edu.eams.teach.election.service.helper.CourseLimitGroupHelper
import org.openurp.edu.eams.teach.election.service.rule.ElectRulePrepare
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.Lesson
import ElectionTeachClassFilter._




object ElectionTeachClassFilter {

  private val CHECK_LESSON_LIMIT = "checkLessonLimit"

  private val CHECK_GROUP_LIMIT = "checkGroupLimit"

  private val CHECK_TEACH_CLASS = "checkTeachClass"
}

class ElectionTeachClassFilter extends AbstractElectableLessonFilter() with ElectRulePrepare {

  
  var checkLessonLimit: Boolean = false

  
  var checkGroupLimit: Boolean = false

  
  var checkTeachClass: Boolean = false

  order = java.lang.Integer.MAX_VALUE

  def isElectable(lesson: Lesson, state: ElectState): Boolean = {
    if (!this.checkTeachClass) {
      return true
    }
    if (retakeService.isRetakeCourse(state, lesson.getCourse.id) && 
      !retakeService.isCheckTeachClass(state.getProfile(entityDao).getElectConfigs)) {
      return true
    }
    if (CollectUtils.isEmpty(lesson.getTeachClass.getLimitGroups)) {
      return true
    }
    CourseLimitGroupHelper.isElectable(lesson, state)
  }

  protected override def onExecuteRuleReturn(result: Boolean, context: ElectionCourseContext): Boolean = {
    if (!result) {
      context.addMessage(new ElectMessage("只开放给:" + context.getLesson.getTeachClass.getName + "的学生", 
        ElectRuleType.ELECTION, false, context.getLesson))
      return result
    }
    var limitGroup: CourseLimitGroup = null
    limitGroup = if (checkGroupLimit) CourseLimitGroupHelper.getMatchCountCourseLimitGroup(context.getLesson, 
      context.getState) else CourseLimitGroupHelper.getMatchCourseLimitGroup(context.getLesson, context.getState)
    if (checkTeachClass && limitGroup == null) {
      context.addMessage(new ElectMessage("匹配不到合适的授课对象组", ElectRuleType.ELECTION, false, context.getLesson))
      return false
    }
    var sb: StringBuilder = null
    if (checkLessonLimit) {
      sb = new StringBuilder("update t_lessons set std_count=std_count+1 where std_count<(limit_count-reserved_count) and id=?")
      val update = electionDao.updateStdCount(sb.toString, context.getLesson.id)
      if (update == 0) {
        context.addMessage(new ElectMessage("人数已满", ElectRuleType.ELECTION, false, context.getLesson))
        return false
      }
    } else {
      sb = new StringBuilder("update t_lessons set std_count=std_count+1 where id=?")
      electionDao.updateStdCount(sb.toString, context.getLesson.id)
    }
    if (limitGroup != null) {
      sb = new StringBuilder("update t_course_limit_groups set cur_count = cur_count+1 where id=?")
      electionDao.updateStdCount(sb.toString, limitGroup.id)
      entityDao.refresh(limitGroup)
      context.getCourseTake.setLimitGroup(limitGroup)
    }
    entityDao.refresh(context.getLesson)
    true
  }

  def prepare(context: PrepareContext) {
    if (!context.isPreparedData(PreparedDataName.ELECTION_TEACHCLASS_FILTER_PREPARE)) {
      for (config <- context.getState.getProfile(entityDao).getElectConfigs if config.getRule.getServiceName.toUpperCase() == this.getClass.getSimpleName.toUpperCase(); 
           param <- config.getParams) {
        if (param.getParam.getName.trim() == CHECK_LESSON_LIMIT) {
          setCheckLessonLimit(java.lang.Boolean.valueOf(Strings.trim(param.getValue)))
        } else if (param.getParam.getName.trim() == CHECK_GROUP_LIMIT) {
          setCheckGroupLimit(java.lang.Boolean.valueOf(Strings.trim(param.getValue)))
        } else if (param.getParam.getName.trim() == CHECK_TEACH_CLASS) {
          setCheckTeachClass(java.lang.Boolean.valueOf(Strings.trim(param.getValue)))
        }
      }
      context.getState.setCheckMaxLimitCount(this.checkLessonLimit)
      context.getState.setCheckTeachClass(this.checkTeachClass)
      context.addPreparedDataName(PreparedDataName.ELECTION_TEACHCLASS_FILTER_PREPARE)
    }
  }
}
