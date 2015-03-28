package org.openurp.edu.eams.teach.lesson.task.web.action




import org.beangle.commons.collection.Collections
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operator
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonOperateViolation
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.task.web.action.parent.LessonManagerCoreAction



class TeachTaskAction extends LessonManagerCoreAction {

  def batchCalPeopleLimit(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val provider = lessonLimitItemContentProviderFactory.getProvider(LessonLimitMeta.Adminclass)
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    if (!lessons.isEmpty) {
      val lessonLimitGroups = Collections.newMap[Any]
      val groups = entityDao.get(classOf[LessonLimitGroup], "lesson", lessons)
      for (lessonLimitGroup <- groups) {
        var oneLessonLimitGroups = lessonLimitGroups.get(lessonLimitGroup.getLesson)
        if (null == oneLessonLimitGroups) {
          oneLessonLimitGroups = Collections.newBuffer[Any]
          lessonLimitGroups.put(lessonLimitGroup.getLesson, oneLessonLimitGroups)
        }
        oneLessonLimitGroups.add(lessonLimitGroup)
      }
      val groupItems = Collections.newMap[Any]
      val items = entityDao.get(classOf[LessonLimitItem], "group", groups)
      for (lessonLimitItem <- items) {
        var oneGroupItems = groupItems.get(lessonLimitItem.getGroup)
        if (null == oneGroupItems) {
          oneGroupItems = Collections.newBuffer[Any]
          groupItems.put(lessonLimitItem.getGroup, oneGroupItems)
        }
        oneGroupItems.add(lessonLimitItem)
      }
      val saveEntities = Collections.newMap[Any]
      for (lesson <- lessons) {
        val lessonLimitGroups = lessonLimitGroups.get(lesson)
        if (null != lessonLimitGroups) {
          for (lessonLimitGroup <- lessonLimitGroups) {
            val maxCount = lessonLimitGroup.getMaxCount
            var adminclassCounts = 0
            val lessonLimitItems = groupItems.get(lessonLimitGroup)
            for (lessonLimitItem <- lessonLimitItems) {
              val meta = lessonLimitItem.getMeta
              if (LessonLimitMeta.Adminclass.getMetaId == meta.id) {
                if (lessonLimitItem.getOperator == Operator.IN) {
                  val adminclasses = provider.getContents(lessonLimitItem.getContent).values.asInstanceOf[Iterable[Adminclass]]
                  for (adminclass <- adminclasses) {
                    var count = adminclass.getStdCount
                    if (count == 0) {
                      count = adminclass.getPlanCount
                    }
                    adminclassCounts += count
                  }
                }
              }
            }
            if (adminclassCounts > 0 && adminclassCounts != maxCount) {
              lessonLimitGroup.setMaxCount(adminclassCounts)
              var saveGroups = saveEntities.get(lesson)
              if (null == saveGroups) {
                saveGroups = Collections.newSet[Any]
                saveEntities.put(lesson, saveGroups)
              }
              saveGroups.add(lessonLimitGroup)
            }
          }
        }
      }
      val entities = Collections.newBuffer[Any]
      for ((key, value) <- saveEntities) {
        val lesson = key
        val saveGroups = value
        var limitCount = 0
        for (group <- lessonLimitGroups.get(lesson) if !saveGroups.contains(group)) {
          limitCount += group.getMaxCount
        }
        for (group <- saveGroups) {
          limitCount += group.getMaxCount
        }
        lesson.getTeachClass.setLimitCount(limitCount)
        entities.add(lesson)
        entities.addAll(saveGroups)
      }
      try {
        entityDao.saveOrUpdate(entities)
        return redirect("search", "info.save.success", get("params"))
      } catch {
        case e: Exception => logger.info("info.save.failure", e)
      }
    }
    redirect("search", "info.save.failure", get("params"))
  }

  def allowCollegeOperation(): String = {
    val semesterId = getIntId("semester")
    val projectId = getSession.get("projectId").asInstanceOf[java.lang.Integer]
    lessonCollegeSwitchService.allow(semesterId, projectId)
    put("message", "从现在起院系允许对教学任务进行:\n添加、删除、修改、复制（到本学期）、拆分以及合并操作")
    "collegeSwitchMessage"
  }

  def disallowCollegeOperation(): String = {
    val semesterId = getIntId("semester")
    val projectId = getSession.get("projectId").asInstanceOf[java.lang.Integer]
    lessonCollegeSwitchService.disallow(semesterId, projectId)
    put("message", "从现在起院系禁止对教学任务进行:\n添加、删除、修改、复制（到本学期）、拆分以及合并操作")
    "collegeSwitchMessage"
  }

  def auditLessons(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val pass = get("pass")
    var res: CommonAuditState = null
    if ("1" == pass) {
      res = CommonAuditState.ACCEPTED
    } else if ("0" == pass) {
      res = CommonAuditState.REJECTED
    } else if ("-1" == pass) {
      res = CommonAuditState.UNSUBMITTED
    }
    for (lesson <- lessons) {
      lesson.setAuditStatus(res)
    }
    entityDao.saveOrUpdate(lessons)
    redirect("search", "info.save.success", get("params"))
  }

  override def copyViolationCheck(lesson: Lesson, semester: Semester): LessonOperateViolation = LessonOperateViolation.NO_VIOLATION

  override def operateViolationCheck(lesson: Lesson): LessonOperateViolation = LessonOperateViolation.NO_VIOLATION

  override def operateViolationCheck(lessons: List[Lesson]): LessonOperateViolation = LessonOperateViolation.NO_VIOLATION
}
