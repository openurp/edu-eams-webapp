package org.openurp.edu.eams.teach.lesson.task.splitter






import org.apache.commons.collections.CollectionUtils
import org.openurp.edu.base.Adminclass
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil




class AdminclassGroupMode extends AbstractTeachClassSplitter() {

  
  var adminclassGroups: List[Array[Long]] = new ArrayList[Array[Long]]()

  this.name = "adminclass_split"

  override def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val teachclasses = Array.ofDim[TeachClass](num)
    val courseTakes = target.getCourseTakes
    val problemTakes = new HashSet[CourseTake]()
    val assignedCourseTakes = new HashSet[CourseTake]()
    for (i <- 0 until adminclassGroups.size) {
      teachclasses(i) = target.clone().asInstanceOf[TeachClass]
      teachclasses(i).setCourseTakes(new HashSet[CourseTake]())
      teachclasses(i).setExamTakes(new HashSet[ExamTake]())
      val adminclassSet = new HashSet[Adminclass]()
      var planCount = 0
      val adminclassIds = adminclassGroups.get(i)
      if (adminclassIds != null) {
        for (j <- 0 until adminclassIds.length) {
          val adminclass = selectAdminClass(adminclassIds(j), util.extractAdminclasses(target))
          adminclassSet.add(adminclass)
          planCount += adminclass.getPlanCount
          for (take <- target.getCourseTakes) {
            if (take.getStd.getAdminclass == null) {
              problemTakes.add(take)
              //continue
            }
            if (adminclass.id == take.getStd.getAdminclass.id) {
              LessonElectionUtil.addCourseTake(teachclasses(i), take)
              assignedCourseTakes.add(take)
            }
          }
        }
      }
      util.limitTeachClass(Operator.IN, teachclasses(i), adminclassSet.toArray(Array.ofDim[Adminclass](0)))
      setLimitCountByScale(target.getStdCount, target.getLimitCount, teachclasses(i))
      teachClassNameStrategy.autoName(teachclasses(i))
    }
    problemTakes.addAll(CollectionUtils.subtract(courseTakes, assignedCourseTakes))
    LessonElectionUtil.addCourseTakes(teachclasses(0), problemTakes)
    teachclasses
  }

  private def selectAdminClass(adminClassId: java.lang.Long, adminclasses: Iterable[Adminclass]): Adminclass = {
    adminclasses.find(_.id == adminClassId).getOrElse(null)
  }
}
