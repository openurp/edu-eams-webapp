package org.openurp.edu.eams.teach.lesson.task.splitter



import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.beangle.commons.collection.Collections



class AverageMode extends AbstractTeachClassSplitter() {

  this.name = "average_split"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val originalLimitCount = target.limitCount
    val courseTakes = util.extractPossibleCourseTakes(target)
    val takesArr = splitTakes(courseTakes, num)
    val teachClasses = Array.ofDim[TeachClass](takesArr.length)
    for (i <- 0 until takesArr.length) {
      teachClasses(i) = target.clone()
      teachClasses(i).courseTakes = Collections.newBuffer[CourseTake]
      teachClasses(i).examTakes = Collections.newSet[ExamTake]
      teachClasses(i).name = target.name + (i + 1)
      teachClasses(i).limitCount = 0
      LessonElectionUtil.addCourseTakes(teachClasses(i), takesArr(i))
    }
    var mod = originalLimitCount % teachClasses.length
    for (teachClass <- teachClasses) {
      var newLimitCount = (originalLimitCount / teachClasses.length)
      if (mod > 0) {
        newLimitCount += 1
        mod -= 1
      }
      teachClass.limitCount = newLimitCount
    }
    teachClasses
  }
}
