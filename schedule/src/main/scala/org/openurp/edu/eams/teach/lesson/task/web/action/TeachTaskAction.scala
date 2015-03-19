package org.openurp.edu.eams.teach.lesson.task.web.action




.Entry

import org.beangle.commons.collection.CollectUtils
import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.core.CommonAuditState
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonOperateViolation
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.task.web.action.parent.LessonManagerCoreAction



class TeachTaskAction extends LessonManagerCoreAction {

  def batchCalPeopleLimit(): String = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    val provider = courseLimitItemContentProviderFactory.getProvider(CourseLimitMetaEnum.ADMINCLASS)
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    if (!lessons.isEmpty) {
      val lessonLimitGroups = CollectUtils.newHashMap()
      val groups = entityDao.get(classOf[CourseLimitGroup], "lesson", lessons)
      for (courseLimitGroup <- groups) {
        var oneLessonLimitGroups = lessonLimitGroups.get(courseLimitGroup.getLesson)
        if (null == oneLessonLimitGroups) {
          oneLessonLimitGroups = CollectUtils.newArrayList()
          lessonLimitGroups.put(courseLimitGroup.getLesson, oneLessonLimitGroups)
        }
        oneLessonLimitGroups.add(courseLimitGroup)
      }
      val groupItems = CollectUtils.newHashMap()
      val items = entityDao.get(classOf[CourseLimitItem], "group", groups)
      for (courseLimitItem <- items) {
        var oneGroupItems = groupItems.get(courseLimitItem.getGroup)
        if (null == oneGroupItems) {
          oneGroupItems = CollectUtils.newArrayList()
          groupItems.put(courseLimitItem.getGroup, oneGroupItems)
        }
        oneGroupItems.add(courseLimitItem)
      }
      val saveEntities = CollectUtils.newHashMap()
      for (lesson <- lessons) {
        val courseLimitGroups = lessonLimitGroups.get(lesson)
        if (null != courseLimitGroups) {
          for (courseLimitGroup <- courseLimitGroups) {
            val maxCount = courseLimitGroup.getMaxCount
            var adminclassCounts = 0
            val courseLimitItems = groupItems.get(courseLimitGroup)
            for (courseLimitItem <- courseLimitItems) {
              val meta = courseLimitItem.getMeta
              if (CourseLimitMetaEnum.ADMINCLASS.getMetaId == meta.id) {
                if (courseLimitItem.getOperator == Operator.IN) {
                  val adminclasses = provider.getContents(courseLimitItem.getContent).values.asInstanceOf[Iterable[Adminclass]]
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
              courseLimitGroup.setMaxCount(adminclassCounts)
              var saveGroups = saveEntities.get(lesson)
              if (null == saveGroups) {
                saveGroups = CollectUtils.newHashSet()
                saveEntities.put(lesson, saveGroups)
              }
              saveGroups.add(courseLimitGroup)
            }
          }
        }
      }
      val entities = CollectUtils.newArrayList()
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
