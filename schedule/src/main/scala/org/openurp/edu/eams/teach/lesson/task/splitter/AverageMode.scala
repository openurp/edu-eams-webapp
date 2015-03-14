package org.openurp.edu.eams.teach.lesson.task.splitter

import java.util.Collection
import java.util.HashSet
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil

import scala.collection.JavaConversions._

class AverageMode extends AbstractTeachClassSplitter() {

  this.name = "average_split"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val originalLimitCount = target.getLimitCount
    val courseTakes = util.extractPossibleCourseTakes(target)
    val takesArr = splitTakes(courseTakes, num)
    val teachClasses = Array.ofDim[TeachClass](takesArr.length)
    for (i <- 0 until takesArr.length) {
      teachClasses(i) = target.clone()
      teachClasses(i).setCourseTakes(new HashSet[CourseTake]())
      teachClasses(i).setExamTakes(new HashSet[ExamTake]())
      teachClasses(i).setName(target.getName + (i + 1))
      teachClasses(i).setLimitCount(0)
      LessonElectionUtil.addCourseTakes(teachClasses(i), takesArr(i))
    }
    var mod = originalLimitCount % teachClasses.length
    for (teachClass <- teachClasses) {
      var newLimitCount = (originalLimitCount / teachClasses.length)
      if (mod > 0) {
        newLimitCount += 1
        mod -= 1
      }
      teachClass.setLimitCount(newLimitCount)
    }
    teachClasses
  }
}
