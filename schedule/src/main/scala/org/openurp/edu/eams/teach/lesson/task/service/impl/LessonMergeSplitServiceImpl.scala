package org.openurp.edu.eams.teach.lesson.task.service.impl




import org.apache.commons.collections.CollectionUtils
import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.task.service.LessonMergeSplitService
import org.openurp.edu.eams.teach.lesson.task.splitter.AbstractTeachClassSplitter
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil



class LessonMergeSplitServiceImpl extends BaseServiceImpl with LessonMergeSplitService {

  private var lessonDao: LessonDao = _

  private var lessonLimitService: LessonLimitService = _

  private var lessonLogHelper: LessonLogHelper = _

  private var teachClassNameStrategy: TeachClassNameStrategy = _

  def merge(taskIds: Array[Long]): Lesson = merge(taskIds, 0)

  def merge(lessonIds: Array[Long], reservedId: java.lang.Long): Lesson = {
    if (null == lessonIds || lessonIds.length < 1 || null == reservedId) {
      return null
    }
    for (i <- 0 until lessonIds.length if reservedId == lessonIds(i)) {
      return merge(lessonIds, i)
    }
    null
  }

  private def merge(taskIds: Array[Long], target: Int): Lesson = {
    if (null == taskIds || taskIds.length == 0 || target >= taskIds.length || 
      target < 0) {
      return null
    }
    val taskList = entityDao.get(classOf[Lesson], taskIds)
    if (taskList.isEmpty || taskList.size != taskIds.length) {
      return null
    }
    val tasks = Array.ofDim[Lesson](taskList.size)
    taskList.toArray(tasks)
    merge(tasks, target)
  }

  private def merge(tasks: Array[Lesson], target: Int): Lesson = {
    for (i <- 0 until tasks.length) {
      if (i == target) {
        //continue
      }
      dirtywork(tasks(target), tasks(i))
    }
    lessonDao.saveMergeResult(tasks, target)
    logMergeResult(tasks, target)
    tasks(target)
  }

  private def logMergeResult(tasks: Array[Lesson], target: Int) {
    for (i <- 0 until tasks.length) {
      if (i == target) {
        lessonLogHelper.log(LessonLogBuilder.update(tasks(i), "合并任务,更新任务"))
      } else {
        lessonLogHelper.log(LessonLogBuilder.delete(tasks(i), "合并任务,删除任务"))
      }
    }
  }

  def split(lesson: Lesson, 
      num: Int, 
      mode: AbstractTeachClassSplitter, 
      splitUnitNums: Array[Integer]): Array[Lesson] = {
    if (num <= 1) {
      return Array(lesson)
    }
    val lessons = Array.ofDim[Lesson](num)
    lessons(0) = lesson
    for (i <- 1 until lessons.length) {
      lessons(i) = lesson.clone().asInstanceOf[Lesson]
    }
    if (splitUnitNums != null) {
      mode.setSplitStdNums(splitUnitNums)
    }
    val splitClasses = mode.splitClass(lesson.getTeachClass, num)
    LessonElectionUtil.normalizeTeachClass(lesson)
    for (j <- 1 until lessons.length) {
      lessons(j).setTeachClass(splitClasses(j))
      for (take <- lessons(j).getTeachClass.getCourseTakes) {
        take.setCourseTakeType(new CourseTakeType(CourseTakeType.NORMAL))
      }
      LessonElectionUtil.normalizeTeachClass(lessons(j))
      if (Collections.isNotEmpty(lessons(j).getTeachClass.getCourseTakes) && 
        lessons(j).getTeachClass.getCourseTakes.iterator().next()
        .isPersisted) {
        val persistedTakes = new HashSet[CourseTake](lessons(j).getTeachClass.getCourseTakes)
        lessons(j).getTeachClass.getCourseTakes.clear()
        for (persistedTake <- persistedTakes) {
          persistedTake.setLesson(lesson)
        }
        lessonDao.saveOrUpdate(lessons(j))
        lessons(j).getTeachClass.getCourseTakes.addAll(persistedTakes)
        for (persistedTake <- persistedTakes) {
          persistedTake.setLesson(lessons(j))
        }
        lessonDao.saveOrUpdate(lessons(j))
      } else {
        lessonDao.saveOrUpdate(lessons(j))
      }
      lessonLogHelper.log(LessonLogBuilder.create(lessons(j), "拆分任务,新建任务"))
    }
    lesson.getTeachClass.getLimitGroups.clear()
    lessonDao.saveOrUpdate(lesson)
    lesson.getTeachClass.getLimitGroups.addAll(splitClasses(0).getLimitGroups)
    lesson.getTeachClass.setName(splitClasses(0).getName)
    lesson.getTeachClass.setCourseTakes(new HashSet[CourseTake]())
    LessonElectionUtil.addCourseTakes(lesson.getTeachClass, splitClasses(0).getCourseTakes)
    lesson.getTeachClass.setStdCount(splitClasses(0).getStdCount)
    lesson.getTeachClass.setLimitCount(splitClasses(0).getLimitCount)
    LessonElectionUtil.normalizeTeachClass(lesson)
    lessonDao.saveOrUpdate(lesson)
    lessonLogHelper.log(LessonLogBuilder.update(lesson, "拆分任务,更新任务"))
    lessons
  }

  private def dirtywork(target: Lesson, source: Lesson): Lesson = {
    entityDao.executeUpdate("update " + classOf[CourseTake].getName + " take set take.lesson=?1 where take.lesson=?2", 
      target, source)
    entityDao.executeUpdate("update " + classOf[LessonLimitGroup].getName + " clg set clg.lesson=?1 where clg.lesson=?2", 
      target, source)
    teachClassNameStrategy.autoName(target.getTeachClass)
    var limitCount = 0
    limitCount += target.getTeachClass.getLimitCount
    limitCount += source.getTeachClass.getLimitCount
    target.getTeachClass.setLimitCount(limitCount)
    var stdCount = 0
    stdCount += target.getTeachClass.getStdCount
    stdCount += source.getTeachClass.getStdCount
    target.getTeachClass.setStdCount(stdCount)
    target
  }

  def setLessonDao(lessonDao: LessonDao) {
    this.lessonDao = lessonDao
  }

  def setLessonLimitService(lessonLimitService: LessonLimitService) {
    this.lessonLimitService = lessonLimitService
  }

  def setLessonLogHelper(lessonLogHelper: LessonLogHelper) {
    this.lessonLogHelper = lessonLogHelper
  }

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }
}
