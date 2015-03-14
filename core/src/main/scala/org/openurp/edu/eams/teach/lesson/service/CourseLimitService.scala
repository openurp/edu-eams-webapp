package org.openurp.edu.eams.teach.lesson.service

import java.util.List
import java.util.Set
import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.tuple.Pair
import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.model.CourseLimitGroupPair
import org.openurp.edu.eams.teach.lesson.model.NormalClassBean
import org.openurp.edu.base.Program

import scala.collection.JavaConversions._

trait CourseLimitService {

  @Deprecated
  def mergeAll(target: TeachClass, source: TeachClass): Unit

  @Deprecated
  def merge(mergeType: java.lang.Long, target: TeachClass, source: TeachClass): Unit

  def xtractEducationLimit(teachClass: TeachClass): Pair[CourseLimitMeta.Operator, List[Education]]

  def xtractEducationLimit(group: CourseLimitGroup): Pair[Operator, List[Education]]

  def xtractAdminclassLimit(teachClass: TeachClass): Pair[Operator, List[Adminclass]]

  def xtractAdminclassLimit(group: CourseLimitGroup): Pair[Operator, List[Adminclass]]

  def xtractNormalclassLimit(teachClass: TeachClass): Pair[Operator, List[NormalClassBean]]

  def xtractNormalclassLimit(group: CourseLimitGroup): Pair[Operator, List[NormalClassBean]]

  def xtractGradeLimit(teachClass: TeachClass): Pair[Operator, List[String]]

  def xtractGradeLimit(group: CourseLimitGroup): Pair[Operator, List[String]]

  def xtractStdTypeLimit(teachClass: TeachClass): Pair[Operator, List[StdType]]

  def xtractStdTypeLimit(group: CourseLimitGroup): Pair[Operator, List[StdType]]

  def xtractAttendDepartLimit(teachClass: TeachClass): Pair[Operator, List[Department]]

  def xtractAttendDepartLimit(group: CourseLimitGroup): Pair[Operator, List[Department]]

  def xtractMajorLimit(teachClass: TeachClass): Pair[Operator, List[Major]]

  def xtractMajorLimit(group: CourseLimitGroup): Pair[Operator, List[Major]]

  def xtractDirectionLimit(teachClass: TeachClass): Pair[Operator, List[Direction]]

  def xtractDirectionLimit(group: CourseLimitGroup): Pair[Operator, List[Direction]]

  def xtractProgramLimit(teachClass: TeachClass): Pair[Operator, List[Program]]

  def xtractProgramLimit(group: CourseLimitGroup): Pair[Operator, List[Program]]

  def limitTeachClass(operator: Operator, teachClass: TeachClass, grades: String*): Unit

  def limitTeachClass[T <: Entity[_]](operator: Operator, teachClass: TeachClass, entities: T*): Unit

  def extractEducations(teachClass: TeachClass): List[Education]

  def extractEducations(group: CourseLimitGroup): List[Education]

  def extractAdminclasses(teachClass: TeachClass): List[Adminclass]

  def extractAdminclasses(group: CourseLimitGroup): List[Adminclass]

  def extractGrade(teachClass: TeachClass): String

  def extractGrade(group: CourseLimitGroup): String

  def extractStdTypes(teachClass: TeachClass): List[StdType]

  def extractStdTypes(group: CourseLimitGroup): List[StdType]

  def extractMajors(teachClass: TeachClass): List[Major]

  def extractMajors(group: CourseLimitGroup): List[Major]

  def extractDirections(teachClass: TeachClass): List[Direction]

  def extractDirections(group: CourseLimitGroup): List[Direction]

  def extractAttendDeparts(teachClass: TeachClass): List[Department]

  def extractAttendDeparts(group: CourseLimitGroup): List[Department]

  def extractPrograms(teachClass: TeachClass): List[Program]

  def extractPrograms(group: CourseLimitGroup): List[Program]

  def extractGender(teachClass: TeachClass): Gender

  def extractGender(group: CourseLimitGroup): Gender

  def extractNormalclasses(teachClass: TeachClass): List[NormalClassBean]

  def extractNormalclasses(group: CourseLimitGroup): List[NormalClassBean]

  def builder(): CourseLimitGroupBuilder

  def builder(teachClass: TeachClass): CourseLimitGroupBuilder

  def extractLonelyTakes(teachClass: TeachClass): Set[CourseTake]

  def extractPossibleCourseTakes(teachClass: TeachClass): Set[CourseTake]

  def isAutoName(lesson: Lesson): Boolean

  def xtractStdLabelLimit(group: CourseLimitGroup): Pair[Operator, List[StdLabel]]

  def xtractStdLabelLimit(teachClass: TeachClass): Pair[Operator, List[StdLabel]]

  def xtractLimitGroup(group: CourseLimitGroup): CourseLimitGroupPair
}
