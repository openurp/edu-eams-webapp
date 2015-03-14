package org.openurp.edu.eams.teach.program.common.dao

import java.util.List
import com.ekingstar.eams.teach.Course
import com.ekingstar.eams.teach.code.school.CourseType
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.CoursePlan
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.major.MajorPlan
//remove if not needed
import scala.collection.JavaConversions._

trait PlanCommonDao {

  def removePlan(plan: CoursePlan): Unit

  def saveOrUpdatePlan(plan: CoursePlan): Unit

  def statPlanCredits(plan: CoursePlan): Float

  def hasCourse(cgroup: CourseGroup, course: Course): Boolean

  def getUsedCourseTypes(plan: CoursePlan): List[CourseType]

  def getUnusedCourseTypes(plan: CoursePlan): List[CourseType]

  def getDuplicatePrograms(program: Program): List[Program]

  def isDuplicate(program: Program): Boolean

  def getCreditByTerm(plan: MajorPlan, term: Int): java.lang.Float

  def hasCourse(cgroup: CourseGroup, course: Course, planCourse: PlanCourse): Boolean
}
