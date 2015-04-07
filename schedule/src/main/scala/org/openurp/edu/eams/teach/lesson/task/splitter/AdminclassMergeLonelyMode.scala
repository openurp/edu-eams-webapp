package org.openurp.edu.eams.teach.lesson.task.splitter





import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.beangle.commons.collection.Collections



class AdminclassMergeLonelyMode extends AbstractTeachClassSplitter {

  this.name = "adminclass_split_merge_lonely"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val adminclasses = util.extractAdminclasses(target)
    if (num > adminclasses.size) {
      return ADMINCLASS_SPLIT_SPLIT_LONELY.splitClass(target, num)
    }
    val num = adminclasses.size
    val originalStdCount = target.stdCount
    val originalLimitCount = target.limitCount
    val adminClasses = splitAdminStds(target)
    val lonelyTakes = Collections.newSet[CourseTake]
    lonelyTakes ++= (util.extractLonelyTakes(target))
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
