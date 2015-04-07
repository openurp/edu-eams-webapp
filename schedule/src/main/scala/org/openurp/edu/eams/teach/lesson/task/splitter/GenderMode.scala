package org.openurp.edu.eams.teach.lesson.task.splitter





import org.beangle.commons.collection.Collections
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.task.service.helper.CourseTakeOfGenderPredicate
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil
import org.mockito.internal.util.collections.ArrayUtils
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.mockito.cglib.core.CollectionUtils
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.code.person.model.GenderBean
import org.openurp.edu.teach.lesson.LessonLimitMeta



class GenderMode extends AbstractTeachClassSplitter() {

  this.name = "gender_split"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val courseTakes = target.courseTakes
    val totalStdCount = courseTakes.size
    val totalLimitCount = target.limitCount
    if (Collections.isNotEmpty(courseTakes)) {
      val maleStds = CollectionUtils.select(courseTakes, new CourseTakeOfGenderPredicate("男"))
      val maleTakesArr = splitTakes(maleStds, splitStdNums(0))
      val maleClasses = Array.ofDim[TeachClass](maleTakesArr.length)
      for (i <- 0 until maleTakesArr.length) {
        maleClasses(i) = target.clone()
        maleClasses(i).name = target.name + "男" + (i + 1)
        maleClasses(i).courseTakes = Collections.newBuffer[CourseTake]
        util.builder(maleClasses(i)).clear(new LessonLimitMeta(LessonLimitMeta.Gender.getMetaId))
          .in(new Gender(1, "男"))
        LessonElectionUtil.addCourseTakes(maleClasses(i), maleTakesArr(i))
      }
      val femaleStds = CollectionUtils.select(courseTakes, new CourseTakeOfGenderPredicate("女"))
      val femaleTakesArr = splitTakes(femaleStds, splitStdNums(1))
      val femaleClasses = Array.ofDim[TeachClass](femaleTakesArr.length)
      for (i <- 0 until femaleTakesArr.length) {
        femaleClasses(i) = target.clone()
        femaleClasses(i).courseTakes = Collections.newBuffer[CourseTake]
        femaleClasses(i).examTakes = Collections.newSet[ExamTake]
        femaleClasses(i).name = target.name + "女" + (i + 1)
        util.builder(femaleClasses(i)).clear(new LessonLimitMeta(LessonLimitMeta.Gender.getMetaId))
          .in(new Gender(2, "女"))
        LessonElectionUtil.addCourseTakes(femaleClasses(i), femaleTakesArr(i))
      }
      val classes = ArrayUtils.addAll(maleClasses, femaleClasses).asInstanceOf[Array[TeachClass]]
      for (i <- 0 until classes.length) {
        setLimitCountByScale(totalStdCount, totalLimitCount, num, classes(i))
      }
      return classes
    }
    val adminclasses = util.extractAdminclasses(target)
    var maleCount = 0
    var femailCount = 0
    for (aclass <- adminclasses; std <- aclass.students) {
      if ("男" == std.person.gender.name) {
        maleCount += 1
      } else if ("女" == std.person.gender.name) {
        femailCount += 1
      }
    }
    val maleClasses = Array.ofDim[TeachClass](splitStdNums(0))
    for (i <- 0 until maleClasses.length) {
      maleClasses(i) = target.clone()
      maleClasses(i).courseTakes = Collections.newBuffer[CourseTake]
      maleClasses(i).examTakes = Collections.newSet[ExamTake]
      maleClasses(i).name = target.name + "男" + (i + 1)
      util.builder(maleClasses(i)).clear(new LessonLimitMeta(LessonLimitMeta.Gender.getMetaId))
        .in(new Gender(1, "男"))
      maleClasses(i).limitCount = maleCount / splitStdNums(0)
    }
    val femaleClasses = Array.ofDim[TeachClass](splitStdNums(1))
    for (i <- 0 until femaleClasses.length) {
      femaleClasses(i) = target.clone()
      femaleClasses(i).name = target.name + "女" + (i + 1)
      femaleClasses(i).courseTakes = Collections.newBuffer[CourseTake]
      util.builder(femaleClasses(i)).clear(new LessonLimitMeta(LessonLimitMeta.Gender.getMetaId))
        .in(new Gender(2, "女"))
      femaleClasses(i).limitCount = femailCount / splitStdNums(1)
    }
    val classes = ArrayUtils.addAll(maleClasses, femaleClasses).asInstanceOf[Array[TeachClass]]
    classes
  }
}
