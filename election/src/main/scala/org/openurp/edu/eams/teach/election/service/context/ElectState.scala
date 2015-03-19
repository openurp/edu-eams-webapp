package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Semester
import 
import org.openurp.edu.base.Student
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.constraint.AbstractCreditConstraint
import org.openurp.edu.eams.teach.election.model.constraint.StdCourseCountConstraint
import org.openurp.edu.eams.teach.election.model.constraint.StdCreditConstraint
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.program.StudentProgram
import ElectState._




object ElectState {

  def createState(std: Student, profile: ElectionProfile, entityDao: EntityDao): ElectState = {
    val it2 = entityDao.get(classOf[StdCreditConstraint], Array("semester", "std"), profile.getSemester, 
      std)
      .iterator()
    val constraint = if (it2.hasNext) it2.next() else null
    val query = OqlBuilder.from(classOf[StudentProgram], "sp")
    query.where("sp.std=:std", std)
    val stdProgram = entityDao.uniqueResult(query)
    val state = new ElectState(std, stdProgram, profile, constraint)
    state
  }
}

@SerialVersionUID(1L)
class ElectState extends Serializable() {

  
  var std: SimpleStd = _

  
  var coursePlan: ElectCoursePlan = _

  
  var profileId: java.lang.Long = _

  
  var semesterId: java.lang.Integer = _

  
  var courseSubstitutions: List[ElectCourseSubstitution] = CollectUtils.newArrayList()

  
  var hisCourses: Map[Long, Boolean] = CollectUtils.newHashMap()

  
  var electedCredit: Float = _

  
  val electedCourseIds = CollectUtils.newHashMap()

  
  val electableLessonIds = CollectUtils.newArrayList()

  
  var table: YearWeekTime = _

  
  var compulsoryCourseIds: Set[Long] = CollectUtils.newHashSet()

  
  var checkTimeConflict: Boolean = _

  
  var checkMaxLimitCount: Boolean = _

  
  var checkTeachClass: Boolean = _

  
  var checkMinLimitCount: Boolean = _

  
  val params = CollectUtils.newHashMap()

  
  var unElectableLessonIds: Set[Long] = CollectUtils.newHashSet()

  
  var unWithdrawableLessonIds: Map[Long, String] = CollectUtils.newHashMap()

  
  var creditConstraint: ElectConstraintWrapper[Float] = _

  
  var totalCreditConstraint: ElectConstraintWrapper[Float] = _

  
  var courseCountConstraint: ElectConstraintWrapper[Integer] = _

  private var courseTypeCourseCountConstraints: Map[CourseType, ElectConstraintWrapper[Integer]] = CollectUtils.newHashMap()

  def electSuccess(lesson: Lesson) {
    if (coursePlan != null) {
      val group = coursePlan.getOrCreateGroup(lesson.getCourse, lesson.getCourseType)
      group.addElectCourse(lesson.getCourse)
    }
    electedCourseIds.put(lesson.getCourse.id, lesson.id)
    if (null != creditConstraint) {
      creditConstraint.addElectedItem(lesson.getCourse.getCredits)
    }
    if (null != totalCreditConstraint) {
      totalCreditConstraint.addElectedItem(lesson.getCourse.getCredits)
    }
    if (null != courseCountConstraint) {
      courseCountConstraint.addElectedItem(1)
    }
    if (null != 
      courseTypeCourseCountConstraints.get(lesson.getCourse.getCourseType)) {
      courseTypeCourseCountConstraints.get(lesson.getCourse.getCourseType)
        .addElectedItem(1)
    } else if (null != 
      courseTypeCourseCountConstraints.get(lesson.getCourseType)) {
      courseTypeCourseCountConstraints.get(lesson.getCourseType)
        .addElectedItem(1)
    }
  }

  def withdrawSuccess(lesson: Lesson) {
    if (coursePlan != null) {
      val group = coursePlan.getGroup(lesson.getCourse, lesson.getCourseType)
      group.removeElectCourse(lesson.getCourse)
    }
    electedCourseIds.remove(lesson.getCourse.id)
    if (null != creditConstraint) {
      creditConstraint.subElectedItem(lesson.getCourse.getCredits)
    }
    if (null != totalCreditConstraint) {
      totalCreditConstraint.subElectedItem(lesson.getCourse.getCredits)
    }
    if (null != courseCountConstraint) {
      courseCountConstraint.subElectedItem(1)
    }
    if (null != 
      courseTypeCourseCountConstraints.get(lesson.getCourse.getCourseType)) {
      courseTypeCourseCountConstraints.get(lesson.getCourse.getCourseType)
        .subElectedItem(1)
    } else if (null != 
      courseTypeCourseCountConstraints.get(lesson.getCourseType)) {
      courseTypeCourseCountConstraints.get(lesson.getCourseType)
        .subElectedItem(1)
    }
  }

  def this(student: Student, 
      stdProgram: StudentProgram, 
      profile: ElectionProfile, 
      constraint: StdCreditConstraint) {
    this()
    std = new SimpleStd(student, stdProgram)
    this.profileId = profile.id
    this.semesterId = profile.getSemester.id
  }

  def getUnPassedCourseIds(): Set[Long] = {
    if (null == hisCourses) {
      Collections.EMPTY_SET
    } else {
      val unPassedCourseIds = CollectUtils.newHashSet()
      val courseSet = hisCourses.keySet
      for (courseId <- courseSet) {
        val rs = hisCourses.get(courseId)
        if (false == rs) {
          unPassedCourseIds.add(courseId)
        }
      }
      unPassedCourseIds
    }
  }

  def isRetakeCourse(courseId: Long): Boolean = {
    if (hisCourses.containsKey(courseId)) {
      return true
    }
    for (courseSubstitution <- courseSubstitutions if courseSubstitution.getSubstitutes.contains(courseId); 
         originCourseId <- courseSubstitution.getOrigins if hisCourses.containsKey(originCourseId)) {
      return true
    }
    false
  }

  def getOriginCourseId(courseId: Long): java.lang.Long = {
    for (courseSubstitution <- courseSubstitutions if courseSubstitution.getSubstitutes.contains(courseId); 
         originCourseId <- courseSubstitution.getOrigins if hisCourses.containsKey(originCourseId)) {
      return originCourseId
    }
    courseId
  }

  def isCoursePass(courseId: java.lang.Long): Boolean = true == hisCourses.get(courseId)

  def getSemester(entityDao: EntityDao): Semester = {
    entityDao.get(classOf[Semester], this.semesterId)
  }

  def getProfile(entityDao: EntityDao): ElectionProfile = {
    entityDao.get(classOf[ElectionProfile], profileId)
  }

  def setCreditConstraint(creditConstraint: AbstractCreditConstraint, electedCredits: java.lang.Float) {
    this.creditConstraint = new CreditConstraintWrapper(creditConstraint, electedCredits)
  }

  def setTotalCreditConstraint(totalCreditConstraint: AbstractCreditConstraint, electedCredits: java.lang.Float) {
    this.totalCreditConstraint = new CreditConstraintWrapper(totalCreditConstraint, electedCredits)
  }

  def setCourseCountConstraint(stdCourseCountConstraint: StdCourseCountConstraint, electedCount: java.lang.Integer, courseTypeCourseCountConstraints: Map[CourseType, ElectConstraintWrapper[Integer]]) {
    this.courseCountConstraint = new CourseCountConstraintWrapper(stdCourseCountConstraint, electedCount)
    this.courseTypeCourseCountConstraints = courseTypeCourseCountConstraints
  }

  def getCourseCountConstraint(courseType: CourseType): ElectConstraintWrapper[Integer] = {
    courseTypeCourseCountConstraints.get(courseType)
  }

  def addCourseSubsititution(courseSubstitution: ElectCourseSubstitution) {
    courseSubstitutions.add(courseSubstitution)
  }
}
