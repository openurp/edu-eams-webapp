package org.openurp.edu.eams.teach.lesson.task.service

import java.util.Collection
import java.util.List
import java.util.Map
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.teach.lesson.task.model.PlanTask
import org.openurp.edu.eams.teach.program.CourseGroup
import org.openurp.edu.eams.teach.program.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan

import scala.collection.JavaConversions._

trait PlanTaskService {

  def checkIsAppropriateClose(planCourseList: List[PlanCourse], planTaskList: List[PlanTask], semester: Semester): List[Array[Any]]

  def checkIsAppropriateOpen(planCourseList: List[PlanCourse], planTaskList: List[PlanTask], semester: Semester): List[Array[Any]]

  def extractInappropriateTeachPlan(teachPlans: Collection[MajorPlan], semester: Semester): Map[MajorPlan, Map[CourseGroup, Array[Double]]]
}
