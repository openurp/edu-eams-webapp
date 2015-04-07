package org.openurp.edu.eams.teach.lesson.task.splitter




import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.beangle.commons.collection.Collections



class AdminclassSplitLonelyMode extends AbstractTeachClassSplitter() {

  this.name = "adminclass_split_split_lonely"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    if (num <= util.extractAdminclasses(target).size) {
      return ADMINCLASS_SPLIT_MERGE_LONELY.splitClass(target, num)
    }
    val classes = Array.ofDim[TeachClass](num)
    val originalStdCount = target.stdCount
    val originalLimitCount = target.limitCount
    val classesHasAdminclass = splitAdminStds(target)
    val classesHasLonelyTakes = Array.ofDim[TeachClass](num - classesHasAdminclass.length)
    val lonelyTakes = Collections.newSet[CourseTake]
    lonelyTakes ++= (util.extractLonelyTakes(target))
    if (!lonelyTakes.isEmpty) {
      val splittedLonleyTakesArr = splitTakes(lonelyTakes, num - classesHasAdminclass.length)
      var i = 0
      for (splittedLonelyTake <- splittedLonleyTakesArr) {
        classesHasLonelyTakes(i) = target.clone()
        classesHasLonelyTakes(i).courseTakes = Collections.newBuffer[CourseTake]
        classesHasLonelyTakes(i).examTakes = Collections.newSet[ExamTake]
        classesHasLonelyTakes(i).name = target.name + (classesHasAdminclass.length + i + 1)
        LessonElectionUtil.addCourseTakes(classesHasLonelyTakes(i), splittedLonelyTake)
        setLimitCountByScale(originalStdCount, originalLimitCount, classesHasLonelyTakes(i))
        i += 1
      }
    }
    for (i <- 0 until num) {
      classes(i) = if (i < classesHasAdminclass.length) classesHasAdminclass(i) else classesHasLonelyTakes(num - i - 1)
    }
    classes
  }
}
