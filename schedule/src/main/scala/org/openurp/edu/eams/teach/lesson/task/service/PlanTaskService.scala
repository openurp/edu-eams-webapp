package org.openurp.edu.eams.teach.lesson.task.service




import org.openurp.base.Semester
import org.openurp.edu.eams.teach.lesson.task.model.PlanTask
import org.openurp.edu.teach.plan.CourseGroup
import org.openurp.edu.teach.plan.PlanCourse
import org.openurp.edu.teach.plan.MajorPlan



trait PlanTaskService {

  def checkIsAppropriateClose(planCourseSeq: Seq[PlanCourse], planTaskSeq: Seq[PlanTask], semester: Semester): Seq[Array[Any]]

  def checkIsAppropriateOpen(planCourseSeq: Seq[PlanCourse], planTaskSeq: Seq[PlanTask], semester: Semester): Seq[Array[Any]]

  def extractInappropriateTeachPlan(teachPlans: Iterable[MajorPlan], semester: Semester): Map[MajorPlan, Map[CourseGroup, Array[Double]]]
}
