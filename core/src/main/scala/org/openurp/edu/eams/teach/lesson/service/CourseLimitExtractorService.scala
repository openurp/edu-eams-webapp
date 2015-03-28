package org.openurp.edu.eams.teach.lesson.service

import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators._
import org.openurp.edu.base.Program

trait LessonLimitExtractorService {

  def xtractEducationLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Education]]

  def xtractAdminclassLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Adminclass]]

  def xtractGradeLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[String]]

  def xtractStdTypeLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[StdType]]

  def xtractAttendDepartLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Department]]

  def xtractMajorLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Major]]

  def xtractDirectionLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Direction]]

  def xtractProgramLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Program]]

  def extractEducations(lessonLimitGroup: LessonLimitGroup): Seq[Education]

  def extractAdminclasses(lessonLimitGroup: LessonLimitGroup): Seq[Adminclass]

  def extractGrade(lessonLimitGroup: LessonLimitGroup): String

  def extractStdTypes(lessonLimitGroup: LessonLimitGroup): Seq[StdType]

  def extractMajors(lessonLimitGroup: LessonLimitGroup): Seq[Major]

  def extractDirections(lessonLimitGroup: LessonLimitGroup): Seq[Direction]

  def extractAttendDeparts(lessonLimitGroup: LessonLimitGroup): Seq[Department]

  def extractPrograms(lessonLimitGroup: LessonLimitGroup): Seq[Program]

  def extractGender(lessonLimitGroup: LessonLimitGroup): Gender
}
