package org.openurp.edu.eams.teach.lesson.service



import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.base.Program



trait CourseLimitExtractorService {

  def xtractEducationLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Education]]

  def xtractAdminclassLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Adminclass]]

  def xtractGradeLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[String]]

  def xtractStdTypeLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[StdType]]

  def xtractAttendDepartLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Department]]

  def xtractMajorLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Major]]

  def xtractDirectionLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Direction]]

  def xtractProgramLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Program]]

  def extractEducations(courseLimitGroup: CourseLimitGroup): List[Education]

  def extractAdminclasses(courseLimitGroup: CourseLimitGroup): List[Adminclass]

  def extractGrade(courseLimitGroup: CourseLimitGroup): String

  def extractStdTypes(courseLimitGroup: CourseLimitGroup): List[StdType]

  def extractMajors(courseLimitGroup: CourseLimitGroup): List[Major]

  def extractDirections(courseLimitGroup: CourseLimitGroup): List[Direction]

  def extractAttendDeparts(courseLimitGroup: CourseLimitGroup): List[Department]

  def extractPrograms(courseLimitGroup: CourseLimitGroup): List[Program]

  def extractGender(courseLimitGroup: CourseLimitGroup): Gender
}
