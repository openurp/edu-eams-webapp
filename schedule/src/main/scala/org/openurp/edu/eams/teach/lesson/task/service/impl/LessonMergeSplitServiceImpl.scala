package org.openurp.edu.eams.teach.lesson.task.service.impl




import org.beangle.commons.collection.Collections
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.task.service.LessonMergeSplitService
import org.openurp.edu.eams.teach.lesson.task.splitter.AbstractTeachClassSplitter
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.openurp.edu.eams.teach.lesson.dao.LessonDao
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.openurp.edu.eams.teach.lesson.service.LessonLogHelper
import scala.collection.mutable.HashSet
import org.openurp.edu.eams.teach.lesson.service.LessonLogBuilder
import org.openurp.edu.teach.code.model.CourseTakeTypeBean



class LessonMergeSplitServiceImpl extends BaseServiceImpl with LessonMergeSplitService {

  var lessonDao: LessonDao = _

  var lessonLimitService: LessonLimitService = _

  var lessonLogHelper: LessonLogHelper = _

  var teachClassNameStrategy: TeachClassNameStrategy = _

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

  private def merge(taskIds: Array[java.lang.Long], target: Int): Lesson = {
    if (null == taskIds || taskIds.length == 0 || target >= taskIds.length || 
      target < 0) {
      return null
    }
    val taskList = entityDao.find(classOf[Lesson], taskIds)
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
    val splitClasses = mode.splitClass(lesson.teachClass, num)
    LessonElectionUtil.normalizeTeachClass(lesson)
    for (j <- 1 until lessons.length) {
      lessons(j).teachClass = splitClasses(j)
      for (take <- lessons(j).teachClass.courseTakes) {
//        take.courseTakeType = new CourseTakeType(CourseTakeType.NORMAL )
        take.courseTakeType = new CourseTakeTypeBean
      }
      LessonElectionUtil.normalizeTeachClass(lessons(j))
      if (Collections.isNotEmpty(lessons(j).teachClass.courseTakes) && 
        lessons(j).teachClass.courseTakes.iterator.next.persisted) {
//        val persistedTakes = new HashSet[CourseTake](lessons(j).teachClass.getCourseTakes)
        val persistedTakes = Collections.newSet[CourseTake]
        lessons(j).teachClass.courseTakes.clear()
        for (persistedTake <- persistedTakes) {
          persistedTake.lesson = lesson
        }
        lessonDao.saveOrUpdate(lessons(j))
        lessons(j).teachClass.courseTakes ++= persistedTakes
        for (persistedTake <- persistedTakes) {
          persistedTake.lesson = lessons(j)
        }
        lessonDao.saveOrUpdate(lessons(j))
      } else {
        lessonDao.saveOrUpdate(lessons(j))
      }
      lessonLogHelper.log(LessonLogBuilder.create(lessons(j), "拆分任务,新建任务"))
    }
    lesson.teachClass.limitGroups.clear()
    lessonDao.saveOrUpdate(lesson)
    lesson.teachClass.limitGroups ++= splitClasses(0).limitGroups
    lesson.teachClass.name = splitClasses(0).name
    lesson.teachClass.courseTakes = Collections.newBuffer[CourseTake]
    LessonElectionUtil.addCourseTakes(lesson.teachClass, splitClasses(0).courseTakes)
    lesson.teachClass.stdCount = splitClasses(0).stdCount
    lesson.teachClass.limitCount = splitClasses(0).limitCount
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
    teachClassNameStrategy.autoName(target.teachClass)
    var limitCount = 0
    limitCount += target.teachClass.limitCount
    limitCount += source.teachClass.limitCount
    target.teachClass.limitCount = limitCount
    var stdCount = 0
    stdCount += target.teachClass.stdCount
    stdCount += source.teachClass.stdCount
    target.teachClass.stdCount = stdCount
    target
  }
}
