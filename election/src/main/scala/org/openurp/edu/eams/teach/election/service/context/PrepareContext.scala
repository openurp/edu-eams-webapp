package org.openurp.edu.eams.teach.election.service.context

import java.io.Serializable


import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.plan.CoursePlan
import PrepareContext._




object PrepareContext {

  object PreparedDataName extends Enumeration {

    val CURRENT_TERM = new PreparedDataName()

    val COURSE_PLAN = new PreparedDataName()

    val ASSIGNED_LESSON_IDS = new PreparedDataName()

    val COURSE_ABILITY_RATE = new PreparedDataName()

    val CHECK_MAX_LIMIT_COUNT = new PreparedDataName()

    val CHECK_MIN_LIMIT_COUNT = new PreparedDataName()

    val CHECTED_COURSES = new PreparedDataName()

    val ERROR_WITHDRAW_TIME = new PreparedDataName()

    val CHECK_TIME_CONFLICT = new PreparedDataName()

    val RETAKE_COURSES = new PreparedDataName()

    val UNPASSED_RETAKE_COURSES = new PreparedDataName()

    val ELECT_COURSE_CREDITS = new PreparedDataName()

    val PLAN_CREDITS_LIMIT = new PreparedDataName()

    val RETAKE_PAYMENT_STATE = new PreparedDataName()

    val CONSTRAINT_MAX_CREDIT = new PreparedDataName()

    val CONSTRAINT_TOTAL_MAX_CREDIT = new PreparedDataName()

    val CONSTRAINT_COURSE_COUNT = new PreparedDataName()

    val ELECTION_TEACHCLASS_FILTER_PREPARE = new PreparedDataName()

    class PreparedDataName extends Val

    implicit def convertValue(v: Value): PreparedDataName = v.asInstanceOf[PreparedDataName]
  }
}

@SerialVersionUID(55623470436456155L)
class PrepareContext( val profile: ElectionProfile, 
     val state: ElectState, 
     val student: Student, 
     val takes: List[CourseTake], 
     val plan: CoursePlan) extends Serializable() {

  private var preparedDataNames: Set[PreparedDataName] = CollectUtils.newHashSet()

  def isPreparedData(preparedDataName: PreparedDataName): Boolean = {
    preparedDataNames.contains(preparedDataName)
  }

  def addPreparedDataName(preparedDataName: PreparedDataName): Boolean = preparedDataNames.add(preparedDataName)
}
