package org.openurp.edu.eams.teach.lesson.service



import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operator
import org.openurp.edu.base.Program



trait LessonLimitExtractorService {

  def xtractEducationLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[Education]]

  def xtractAdminclassLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[Adminclass]]

  def xtractGradeLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[String]]

  def xtractStdTypeLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[StdType]]

  def xtractAttendDepartLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[Department]]

  def xtractMajorLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[Major]]

  def xtractDirectionLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[Direction]]

  def xtractProgramLimit(courseLimitGroup: LessonLimitGroup): Pair[Operator, List[Program]]

  def extractEducations(courseLimitGroup: LessonLimitGroup): List[Education]

  def extractAdminclasses(courseLimitGroup: LessonLimitGroup): List[Adminclass]

  def extractGrade(courseLimitGroup: LessonLimitGroup): String

  def extractStdTypes(courseLimitGroup: LessonLimitGroup): List[StdType]

  def extractMajors(courseLimitGroup: LessonLimitGroup): List[Major]

  def extractDirections(courseLimitGroup: LessonLimitGroup): List[Direction]

  def extractAttendDeparts(courseLimitGroup: LessonLimitGroup): List[Department]

  def extractPrograms(courseLimitGroup: LessonLimitGroup): List[Program]

  def extractGender(courseLimitGroup: LessonLimitGroup): Gender
}
