package org.openurp.edu.eams.teach.lesson.task.splitter





import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Student
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.exam.ExamTake
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.model.LessonLimitMetaBean
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.task.service.helper.CourseTakeOfGenderPredicate
import org.openurp.edu.eams.teach.lesson.util.LessonElectionUtil



class GenderMode extends AbstractTeachClassSplitter() {

  this.name = "gender_split"

  def splitClass(target: TeachClass, num: Int): Array[TeachClass] = {
    val courseTakes = target.getCourseTakes
    val totalStdCount = courseTakes.size
    val totalLimitCount = target.getLimitCount
    if (Collections.isNotEmpty(courseTakes)) {
      val maleStds = CollectionUtils.select(courseTakes, new CourseTakeOfGenderPredicate("男"))
      val maleTakesArr = splitTakes(maleStds, splitStdNums(0))
      val maleClasses = Array.ofDim[TeachClass](maleTakesArr.length)
      for (i <- 0 until maleTakesArr.length) {
        maleClasses(i) = target.clone()
        maleClasses(i).setName(target.getName + "男" + (i + 1))
        maleClasses(i).setCourseTakes(new HashSet[CourseTake]())
        util.builder(maleClasses(i)).clear(new LessonLimitMetaBean(LessonLimitMeta.Gender.getMetaId))
          .in(new Gender(1, "男"))
        LessonElectionUtil.addCourseTakes(maleClasses(i), maleTakesArr(i))
      }
      val femaleStds = CollectionUtils.select(courseTakes, new CourseTakeOfGenderPredicate("女"))
      val femaleTakesArr = splitTakes(femaleStds, splitStdNums(1))
      val femaleClasses = Array.ofDim[TeachClass](femaleTakesArr.length)
      for (i <- 0 until femaleTakesArr.length) {
        femaleClasses(i) = target.clone()
        femaleClasses(i).setCourseTakes(new HashSet[CourseTake]())
        femaleClasses(i).setExamTakes(new HashSet[ExamTake]())
        femaleClasses(i).setName(target.getName + "女" + (i + 1))
        util.builder(femaleClasses(i)).clear(new LessonLimitMetaBean(LessonLimitMeta.Gender.getMetaId))
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
    for (aclass <- adminclasses; std <- aclass.getStudents) {
      if ("男" == std.getGender.getName) {
        maleCount += 1
      } else if ("女" == std.getGender.getName) {
        femailCount += 1
      }
    }
    val maleClasses = Array.ofDim[TeachClass](splitStdNums(0))
    for (i <- 0 until maleClasses.length) {
      maleClasses(i) = target.clone()
      maleClasses(i).setCourseTakes(new HashSet[CourseTake]())
      maleClasses(i).setExamTakes(new HashSet[ExamTake]())
      maleClasses(i).setName(target.getName + "男" + (i + 1))
      util.builder(maleClasses(i)).clear(new LessonLimitMetaBean(LessonLimitMeta.Gender.getMetaId))
        .in(new Gender(1, "男"))
      maleClasses(i).setLimitCount(maleCount / splitStdNums(0))
    }
    val femaleClasses = Array.ofDim[TeachClass](splitStdNums(1))
    for (i <- 0 until femaleClasses.length) {
      femaleClasses(i) = target.clone()
      femaleClasses(i).setName(target.getName + "女" + (i + 1))
      femaleClasses(i).setCourseTakes(new HashSet[CourseTake]())
      util.builder(femaleClasses(i)).clear(new LessonLimitMetaBean(LessonLimitMeta.Gender.getMetaId))
        .in(new Gender(2, "女"))
      femaleClasses(i).setLimitCount(femailCount / splitStdNums(1))
    }
    val classes = ArrayUtils.addAll(maleClasses, femaleClasses).asInstanceOf[Array[TeachClass]]
    classes
  }
}
