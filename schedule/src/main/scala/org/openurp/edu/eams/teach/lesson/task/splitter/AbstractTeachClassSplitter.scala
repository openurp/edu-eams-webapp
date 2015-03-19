package org.openurp.edu.eams.teach.lesson.task.splitter



import java.util.Comparator






import java.util.TreeSet
import org.apache.commons.collections.CollectionUtils
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.task.service.helper.CourseTakeOfClassPredicate
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import AbstractTeachClassSplitter._



object AbstractTeachClassSplitter {

  private val splitModes = new HashMap[String, AbstractTeachClassSplitter]()

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

  def getMode(modeName: String, util: CourseLimitService, teachClassNameStrategy: TeachClassNameStrategy): AbstractTeachClassSplitter = {
    var mode: AbstractTeachClassSplitter = null
    mode = if ("adminclass_split" == modeName) new AdminclassGroupMode() else splitModes.get(modeName).asInstanceOf[AbstractTeachClassSplitter]
    if (mode == null) {
      return null
    }
    mode.setLessonCourseLimitUtil(util)
    mode.setTeachClassNameStrategy(teachClassNameStrategy)
    mode
  }
}

abstract class AbstractTeachClassSplitter {

  protected var name: String = _

  protected var util: CourseLimitService = _

  protected var teachClassNameStrategy: TeachClassNameStrategy = _

  protected var splitStdNums: Array[Integer] = null

  def splitClass(target: TeachClass, num: Int): Array[TeachClass]

  protected def splitTakes(takes: Iterable[CourseTake], num: Int): Array[Set[CourseTake]] = {
    val results = Array.ofDim[TreeSet](num)
    val totalCount = CollectionUtils.size(takes)
    val avgLimitCount = Math.ceil(totalCount.toDouble / num.toDouble).toInt
    val modLimitCount = totalCount - avgLimitCount * (num - 1)
    val courseTakeIter = takes.iterator()
    for (i <- 0 until num - 1) {
      results(i) = extractTakes(avgLimitCount, courseTakeIter)
    }
    results(num - 1) = extractTakes(modLimitCount, courseTakeIter)
    results
  }

  protected def extractTakes(count: Int, courseTakeIt: Iterator[CourseTake]): Set[CourseTake] = {
    val results = new ArrayList[CourseTake]()
    var j = 0
    while (j < count && courseTakeIt.hasNext) {
      results.add(courseTakeIt.next())
      j += 1
    }
    val treeSet = new TreeSet[CourseTake](new Comparator[CourseTake]() {

      def compare(o1: CourseTake, o2: CourseTake): Int = {
        return o1.getStd.getCode.compareTo(o2.getStd.getCode)
      }
    })
    treeSet.addAll(results)
    treeSet
  }

  protected def setLimitCountByScale(originalStdCount: Int, 
      originalLimitCount: Int, 
      splitNum: Int, 
      teachClass: TeachClass) {
    val stdCount = teachClass.getStdCount
    if (originalStdCount == 0) {
      teachClass.setLimitCount((originalLimitCount / splitNum.toDouble).toInt)
    } else {
      teachClass.setLimitCount((stdCount.toDouble / originalStdCount * originalLimitCount).toInt)
    }
  }

  protected def setLimitCountByScale(originalStdCount: Int, originalLimitCount: Int, teachClass: TeachClass) {
    val stdCount = teachClass.getStdCount
    val adminclasses = util.extractAdminclasses(teachClass)
    if (originalStdCount == 0) {
      if (stdCount == 0) {
        var planCount = 0
        for (adminclass <- adminclasses) {
          planCount += adminclass.getPlanCount
        }
        teachClass.setLimitCount(planCount)
      } else {
        teachClass.setLimitCount(stdCount)
      }
    } else {
      teachClass.setLimitCount((stdCount.toDouble / originalStdCount * originalLimitCount).toInt)
    }
  }

  protected def splitAdminStds(target: TeachClass): Array[TeachClass] = {
    val adminclasses = new HashSet[Adminclass](util.extractAdminclasses(target))
    val teachClasses = Array.ofDim[TeachClass](adminclasses.size)
    val originalStdCount = target.getStdCount
    val originalLimitCount = target.getLimitCount
    var i = 0
    var iter = adminclasses.iterator()
    while (iter.hasNext) {
      val one = iter.next().asInstanceOf[Adminclass]
      teachClasses(i) = target.clone().asInstanceOf[TeachClass]
      teachClasses(i).setName(one.getName)
      teachClasses(i).setCourseTakes(new HashSet[CourseTake]())
      teachClasses(i).setExamTakes(new HashSet[ExamTake]())
      util.limitTeachClass(Operator.IN, teachClasses(i), one)
      LessonElectionUtil.addCourseTakes(teachClasses(i), CollectionUtils.select(target.getCourseTakes, 
        new CourseTakeOfClassPredicate(one)))
      setLimitCountByScale(originalStdCount, originalLimitCount, teachClasses(i))
      i += 1
    }
    teachClasses
  }

  override def toString(): String = name

  def setSplitStdNums(splitStdNums: Array[Integer]) {
    this.splitStdNums = splitStdNums
  }

  def setLessonCourseLimitUtil(util: CourseLimitService) {
    this.util = util
  }

  def getName(): String = name

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }
}
