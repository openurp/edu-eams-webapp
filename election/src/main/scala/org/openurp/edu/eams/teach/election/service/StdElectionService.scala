package org.openurp.edu.eams.teach.election.service

import java.util.Collection
import java.util.List
import java.util.Map
import org.beangle.commons.text.i18n.Message
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.eams.teach.election.service.context.ElectState
import org.openurp.edu.eams.teach.election.service.context.ElectionCourseContext
import org.openurp.edu.eams.teach.election.service.context.PrepareContext
import org.openurp.edu.teach.lesson.Lesson
import StdElectionService._

import scala.collection.JavaConversions._

object StdElectionService {

  var selectSuccess: String = "prompt.elect.success"

  var notEvaluateComplete: String = "error.elect.notEvaluateComplete"

  var overCeilCreditConstraint: String = "error.elect.overCeilCreditConstraint"

  var overMaxStdCount: String = "error.elect.overMaxStdCount"

  var notExistsCreditConstraint: String = "error.elect.notExistsCreditConstraint"

  var notInElectScope: String = "error.elect.notInElectScope"

  var reStudiedNotAllowed: String = "error.elect.reStudiedNotAllowed"

  var reStudyPassedCourseNotAllowed: String = "error.elect.reStudyPassedCourseNotAllowed"

  var elected: String = "error.elect.elected"

  var electClosed: String = "error.elect.electClosed"

  var noAverageGrade: String = "error.elect.noAverageGrade"

  var timeCollision: String = "error.elect.timeCollision"

  var needSelectOnTimeCollsion: String = "error.elect.needSelectOnTimeCollsion"

  var noDateSuitable: String = "error.elect.noSuitableDate"

  var noTimeSuitable: String = "error.elect.noSuitableTime"

  var noParamsNotExists: String = "error.electParams.notExists"

  var courseIsNotCancelable: String = "error.cancelElect.courseIsNotCancelable"

  var cancelCourseOfPreviousTurn: String = "error.cancelElect.cancelCourseOfPreviousTurn"

  var underMinStdCount: String = "error.cancelElect.underMinStdCount"

  var cancelUnElected: String = "error.cancelElect.cancelUnElected"

  var cancelSuccess: String = "prompt.cancelElect.success"

  var HSKNotSatisfied: String = "error.elect.HSKNotSatisfied"

  var languageAbilityNotSatisfied: String = "error.elect.languageAbilityNotSatisfied"

  var prerequisteCoursesNotMeeted: String = "error.elect.prerequisteCoursesNotMeeted"

  var notInSchoolDistrict: String = "error.elect.notInSchoolDistrict"

  var notGenderDistrict: String = "error.elect.noGenderDistrict"
}

trait StdElectionService {

  def getProfiles(std: Student): List[ElectionProfile]

  def isElectable(lesson: Lesson, state: ElectState): Boolean

  def batchOperator(contexts: Map[ElectRuleType, List[ElectionCourseContext]]): Collection[List[Message]]

  def generalCheck(profile: ElectionProfile, context: ElectionCourseContext): List[Message]

  def prepare(profile: ElectionProfile, context: PrepareContext): Unit

  def test(context: ElectionCourseContext): ElectionCourseContext
}
