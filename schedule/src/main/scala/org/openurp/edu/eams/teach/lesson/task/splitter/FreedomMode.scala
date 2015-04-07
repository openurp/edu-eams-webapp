package org.openurp.edu.eams.teach.lesson.task.splitter





import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.beangle.commons.collection.Collections



class FreedomMode extends AbstractTeachClassSplitter {

  this.name = "freedom_split"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val courseTakes = Collections.newBuffer[CourseTake]
    courseTakes ++= util.extractPossibleCourseTakes(target)
    Collections.sort(courseTakes)
    val totalStdCount = courseTakes.size
    val totalLimitCount = target.limitCount
    val stdIt = courseTakes.iterator
    val teachClasses = Array.ofDim[TeachClass](num)
    for (i <- 0 until num) {
      val takes = extractTakes(splitStdNums(i), stdIt)
      teachClasses(i) = target.clone()
      teachClasses(i).courseTakes = Collections.newBuffer[CourseTake]
      teachClasses(i).examTakes = Collections.newSet[ExamTake]
      teachClasses(i).name = target.name + (i + 1)
      LessonElectionUtil.addCourseTakes(teachClasses(i), takes)
      val stdCount = teachClasses(i).stdCount
      if (totalStdCount == 0) {
        if (stdCount == 0) {
          teachClasses(i).limitCount = splitStdNums(i)
        } else {
          teachClasses(i).limitCount = stdCount
        }
      } else {
        teachClasses(i).limitCount = (stdCount.toDouble / totalStdCount * totalLimitCount).toInt
      }
    }
    teachClasses
  }
}
