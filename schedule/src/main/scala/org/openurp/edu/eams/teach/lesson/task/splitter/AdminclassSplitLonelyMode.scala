package org.openurp.edu.eams.teach.lesson.task.splitter

import java.util.Collection
import java.util.HashSet
import java.util.Set
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.teach.lesson.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil

import scala.collection.JavaConversions._

class AdminclassSplitLonelyMode extends AbstractTeachClassSplitter() {

  this.name = "adminclass_split_split_lonely"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    if (num <= util.extractAdminclasses(target).size) {
      return ADMINCLASS_SPLIT_MERGE_LONELY.splitClass(target, num)
    }
    val classes = Array.ofDim[TeachClass](num)
    val originalStdCount = target.getStdCount
    val originalLimitCount = target.getLimitCount
    val classesHasAdminclass = splitAdminStds(target)
    val classesHasLonelyTakes = Array.ofDim[TeachClass](num - classesHasAdminclass.length)
    val lonelyTakes = new HashSet[CourseTake](util.extractLonelyTakes(target))
    if (!lonelyTakes.isEmpty) {
      val splittedLonleyTakesArr = splitTakes(lonelyTakes, num - classesHasAdminclass.length)
      var i = 0
      for (splittedLonelyTake <- splittedLonleyTakesArr) {
        classesHasLonelyTakes(i) = target.clone()
        classesHasLonelyTakes(i).setCourseTakes(new HashSet[CourseTake]())
        classesHasLonelyTakes(i).setExamTakes(new HashSet[ExamTake]())
        classesHasLonelyTakes(i).setName(target.getName + (classesHasAdminclass.length + i + 1))
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
