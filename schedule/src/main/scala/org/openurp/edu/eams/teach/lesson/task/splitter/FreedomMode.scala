package org.openurp.edu.eams.teach.lesson.task.splitter

import java.util.ArrayList
import java.util.Collection
import java.util.Collections
import java.util.HashSet
import java.util.Iterator
import java.util.List
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil

import scala.collection.JavaConversions._

class FreedomMode extends AbstractTeachClassSplitter {

  this.name = "freedom_split"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val courseTakes = new ArrayList[CourseTake]()
    courseTakes.addAll(util.extractPossibleCourseTakes(target))
    Collections.sort(courseTakes)
    val totalStdCount = courseTakes.size
    val totalLimitCount = target.getLimitCount
    val stdIt = courseTakes.iterator()
    val teachClasses = Array.ofDim[TeachClass](num)
    for (i <- 0 until num) {
      val takes = extractTakes(splitStdNums(i), stdIt)
      teachClasses(i) = target.clone()
      teachClasses(i).setCourseTakes(new HashSet[CourseTake]())
      teachClasses(i).setExamTakes(new HashSet[ExamTake]())
      teachClasses(i).setName(target.getName + (i + 1))
      LessonElectionUtil.addCourseTakes(teachClasses(i), takes)
      val stdCount = teachClasses(i).getStdCount
      if (totalStdCount == 0) {
        if (stdCount == 0) {
          teachClasses(i).setLimitCount(splitStdNums(i))
        } else {
          teachClasses(i).setLimitCount(stdCount)
        }
      } else {
        teachClasses(i).setLimitCount((stdCount.toDouble / totalStdCount * totalLimitCount).toInt)
      }
    }
    teachClasses
  }
}
