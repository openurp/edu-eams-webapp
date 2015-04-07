package org.openurp.edu.eams.teach.lesson.task.splitter






import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators.Operator
import org.beangle.commons.collection.Collections




class AdminclassGroupMode extends AbstractTeachClassSplitter() {

  
  var adminclassGroups: collection.mutable.Buffer[Array[Long]] = Collections.newBuffer[Array[Long]]()

  this.name = "adminclass_split"

  override def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val teachclasses = Array.ofDim[TeachClass](num)
    val courseTakes = target.courseTakes
    val problemTakes = Collections.newSet[CourseTake]
    val assignedCourseTakes = Collections.newSet[CourseTake]
    for (i <- 0 until adminclassGroups.size) {
      teachclasses(i) = target.clone().asInstanceOf[TeachClass]
      teachclasses(i).courseTakes = Collections.newBuffer[CourseTake]
      teachclasses(i).examTakes = Collections.newSet[ExamTake]
      val adminclassSet = Collections.newSet[Adminclass]
      var planCount = 0
      val adminclassIds = adminclassGroups(i)
      if (adminclassIds != null) {
        for (j <- 0 until adminclassIds.length) {
          val adminclass = selectAdminClass(adminclassIds(j), util.extractAdminclasses(target))
          adminclassSet.add(adminclass)
          planCount += adminclass.planCount
          for (take <- target.courseTakes) {
            if (take.std.adminclass == null) {
              problemTakes.add(take)
              //continue
            }
            if (adminclass.id == take.std.adminclass.id) {
              LessonElectionUtil.addCourseTake(teachclasses(i), take)
              assignedCourseTakes.add(take)
            }
          }
        }
      }
      util.limitTeachClass(Operator.IN, teachclasses(i), adminclassSet.toArray(Array.ofDim[Adminclass](0)))
      setLimitCountByScale(target.stdCount, target.limitCount, teachclasses(i))
      teachClassNameStrategy.autoName(teachclasses(i))
    }
    problemTakes ++= CollectionUtils.subtract(courseTakes, assignedCourseTakes)
    LessonElectionUtil.addCourseTakes(teachclasses(0), problemTakes)
    teachclasses
  }

  private def selectAdminClass(adminClassId: java.lang.Long, adminclasses: Iterable[Adminclass]): Adminclass = {
    adminclasses.find(_.id == adminClassId).getOrElse(null)
  }
}
