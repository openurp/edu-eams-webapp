package org.openurp.edu.eams.teach.lesson.task.splitter





import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil



class AdminclassMergeLonelyMode extends AbstractTeachClassSplitter {

  this.name = "adminclass_split_merge_lonely"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val adminclasses = util.extractAdminclasses(target)
    if (num > adminclasses.size) {
      return ADMINCLASS_SPLIT_SPLIT_LONELY.splitClass(target, num)
    }
    num = adminclasses.size
    val originalStdCount = target.getStdCount
    val originalLimitCount = target.getLimitCount
    val adminClasses = splitAdminStds(target)
    val lonelyTakes = new HashSet[CourseTake](util.extractLonelyTakes(target))
    if (!lonelyTakes.isEmpty) {
      val splittedLonleyTakesArr = splitTakes(lonelyTakes, num)
      var i = 0
      for (splittedLonelyTake <- splittedLonleyTakesArr) {
        LessonElectionUtil.addCourseTakes(adminClasses(i), splittedLonelyTake)
        setLimitCountByScale(originalStdCount, originalLimitCount, adminClasses(i))
        i += 1
      }
    }
    adminClasses
  }
}
