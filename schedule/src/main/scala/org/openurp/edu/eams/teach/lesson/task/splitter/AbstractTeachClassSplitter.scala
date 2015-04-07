package org.openurp.edu.eams.teach.lesson.task.splitter



import java.util.Comparator
import java.util.TreeSet
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.task.service.helper.CourseTakeOfClassPredicate
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import AbstractTeachClassSplitter._
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators.Operator
import scala.collection.mutable.HashSet
import org.mockito.cglib.core.CollectionUtils
import org.beangle.commons.collection.Collections
import java.util.ArrayList



object AbstractTeachClassSplitter {

  private val splitModes = Collections.newMap[String, AbstractTeachClassSplitter]

  val AVERAGE_SPLIT = new AverageMode()

  val FREEDOM_SPLIT = new FreedomMode()

  val ADMINCLASS_SPLIT_SPLIT_LONELY = new AdminclassSplitLonelyMode()

  val ADMINCLASS_SPLIT_MERGE_LONELY = new AdminclassMergeLonelyMode()

  val GENDER_SPLIT = new GenderMode()

  val NUMBER_SPLIT = new NumberMode()

  splitModes.put(AVERAGE_SPLIT.name, AVERAGE_SPLIT)

  splitModes.put(ADMINCLASS_SPLIT_SPLIT_LONELY.name, ADMINCLASS_SPLIT_SPLIT_LONELY)

  splitModes.put(ADMINCLASS_SPLIT_MERGE_LONELY.name, ADMINCLASS_SPLIT_MERGE_LONELY)

  splitModes.put(FREEDOM_SPLIT.name, FREEDOM_SPLIT)

  splitModes.put(GENDER_SPLIT.name, GENDER_SPLIT)

  splitModes.put(NUMBER_SPLIT.name, NUMBER_SPLIT)

  def getMode(modeName: String, util: LessonLimitService, teachClassNameStrategy: TeachClassNameStrategy): AbstractTeachClassSplitter = {
    var mode: AbstractTeachClassSplitter = null
    mode = if ("adminclass_split" == modeName) new AdminclassGroupMode() else splitModes.get(modeName).asInstanceOf[AbstractTeachClassSplitter]
    if (mode == null) {
      return null
    }
//    mode.lessonLessonLimitUtil = util
    mode.teachClassNameStrategy = teachClassNameStrategy
    mode
  }
}

abstract class AbstractTeachClassSplitter {

  var name: String = _

  var util: LessonLimitService = _

  var teachClassNameStrategy: TeachClassNameStrategy = _

  var splitStdNums: Array[Integer] = null

  def splitClass(target: TeachClass, num: Int): Array[TeachClass]

  protected def splitTakes(takes: Iterable[CourseTake], num: Int): Array[Set[CourseTake]] = {
    val results = Array.ofDim[TreeSet](num)
    val totalCount = CollectionUtils.size(takes)
    val avgLimitCount = Math.ceil(totalCount.toDouble / num.toDouble).toInt
    val modLimitCount = totalCount - avgLimitCount * (num - 1)
    val courseTakeIter = takes.iterator
    for (i <- 0 until num - 1) {
      results(i) = extractTakes(avgLimitCount, courseTakeIter)
    }
    results(num - 1) = extractTakes(modLimitCount, courseTakeIter)
    results
  }

  protected def extractTakes(count: Int, courseTakeIt: Iterator[CourseTake]): Set[CourseTake] = {
    val results = Collections.newBuffer[CourseTake]
    var j = 0
    while (j < count && courseTakeIt.hasNext) {
      results += courseTakeIt.next()
      j += 1
    }
    val treeSet = Collections.newSet[CourseTake](new Comparator[CourseTake]() {

      def compare(o1: CourseTake, o2: CourseTake): Int = {
        return o1.std.code.compareTo(o2.std.code)
      }
    })
    treeSet.addAll(results)
    treeSet
  }

  protected def setLimitCountByScale(originalStdCount: Int, 
      originalLimitCount: Int, 
      splitNum: Int, 
      teachClass: TeachClass) {
    val stdCount = teachClass.stdCount
    teachClass.limitCount = if (originalStdCount == 0) (originalLimitCount / splitNum.toDouble).toInt else (stdCount.toDouble / originalStdCount * originalLimitCount).toInt
//    if (originalStdCount == 0) {
//      teachClass.limitCount = (originalLimitCount / splitNum.toDouble).toInt
//    } else {
//      teachClass.limitCount = (stdCount.toDouble / originalStdCount * originalLimitCount).toInt
//    }
  }

  protected def setLimitCountByScale(originalStdCount: Int, originalLimitCount: Int, teachClass: TeachClass) {
    val stdCount = teachClass.stdCount
    val adminclasses = util.extractAdminclasses(teachClass)
    if (originalStdCount == 0) {
      if (stdCount == 0) {
        var planCount = 0
        for (adminclass <- adminclasses) {
          planCount += adminclass.planCount
        }
        teachClass.limitCount = planCount
      } else {
        teachClass.limitCount = stdCount
      }
    } else {
      teachClass.limitCount = (stdCount.toDouble / originalStdCount * originalLimitCount).toInt
    }
  }

  protected def splitAdminStds(target: TeachClass): Array[TeachClass] = {
    val adminclasses = Collections.newSet[Adminclass]
    adminclasses ++= (util.extractAdminclasses(target))
    val teachClasses = Array.ofDim[TeachClass](adminclasses.size)
    val originalStdCount = target.stdCount
    val originalLimitCount = target.limitCount
    var i = 0
    var iter = adminclasses.iterator
    while (iter.hasNext) {
      val one = iter.next().asInstanceOf[Adminclass]
      teachClasses(i) = target.clone().asInstanceOf[TeachClass]
      teachClasses(i).name = one.name
      teachClasses(i).courseTakes = Collections.newBuffer[CourseTake]
      teachClasses(i).examTakes = Collections.newSet[ExamTake]
      util.limitTeachClass(Operator.IN, teachClasses(i), one)
      LessonElectionUtil.addCourseTakes(teachClasses(i), CollectionUtils.select(target.courseTakes, 
        new CourseTakeOfClassPredicate(one)))
      setLimitCountByScale(originalStdCount, originalLimitCount, teachClasses(i))
      i += 1
    }
    teachClasses
  }


}
