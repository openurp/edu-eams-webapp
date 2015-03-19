package org.openurp.edu.eams.teach.lesson.task.splitter




import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.ArrayUtils
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.task.service.helper.CourseTakeOfStdNoPredicate
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil



class NumberMode extends AbstractTeachClassSplitter() {

  this.name = "number_split"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val courseTakes = new HashSet[CourseTake](util.extractPossibleCourseTakes(target))
    val totalStdCount = courseTakes.size
    val totalLimitCount = target.getLimitCount
    val oddNoStds = CollectionUtils.select(courseTakes, new CourseTakeOfStdNoPredicate(true))
    val oddTakesArr = splitTakes(oddNoStds, splitStdNums(0))
    val oddNoClasses = Array.ofDim[TeachClass](oddTakesArr.length)
    for (i <- 0 until oddTakesArr.length) {
      oddNoClasses(i) = target.clone()
      oddNoClasses(i).setCourseTakes(new HashSet[CourseTake]())
      oddNoClasses(i).setExamTakes(new HashSet[ExamTake]())
      oddNoClasses(i).setName(target.getName + "单" + (i + 1))
      LessonElectionUtil.addCourseTakes(oddNoClasses(i), oddTakesArr(i))
    }
    val evenNoStds = CollectionUtils.select(courseTakes, new CourseTakeOfStdNoPredicate(false))
    val evenTakesArr = splitTakes(evenNoStds, splitStdNums(1))
    val evenNoClasses = Array.ofDim[TeachClass](evenTakesArr.length)
    for (i <- 0 until evenTakesArr.length) {
      evenNoClasses(i) = target.clone()
      evenNoClasses(i).setCourseTakes(new HashSet[CourseTake]())
      evenNoClasses(i).setExamTakes(new HashSet[ExamTake]())
      evenNoClasses(i).setName(target.getName + "双" + (i + 1))
      LessonElectionUtil.addCourseTakes(evenNoClasses(i), evenTakesArr(i))
    }
    val classes = ArrayUtils.addAll(oddNoClasses, evenNoClasses).asInstanceOf[Array[TeachClass]]
    for (i <- 0 until classes.length) {
      setLimitCountByScale(totalStdCount, totalLimitCount, classes(i))
    }
    classes
  }
}
