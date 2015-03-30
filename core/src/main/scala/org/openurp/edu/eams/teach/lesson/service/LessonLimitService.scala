package org.openurp.edu.eams.teach.lesson.service

import org.beangle.data.model.Entity
import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators._
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.model.LessonLimitGroupPair
import org.openurp.edu.base.Program

trait LessonLimitService {

  @Deprecated
  def mergeAll(target: TeachClass, source: TeachClass): Unit

  @Deprecated
  def merge(mergeType: java.lang.Long, target: TeachClass, source: TeachClass): Unit

  def xtractEducationLimit(teachClass: TeachClass): Pair[Operator, Seq[Education]]

  def xtractEducationLimit(group: LessonLimitGroup): Pair[Operator, Seq[Education]]

  def xtractAdminclassLimit(teachClass: TeachClass): Pair[Operator, Seq[Adminclass]]

  def xtractAdminclassLimit(group: LessonLimitGroup): Pair[Operator, Seq[Adminclass]]

  def xtractGradeLimit(teachClass: TeachClass): Pair[Operator, Seq[String]]

  def xtractGradeLimit(group: LessonLimitGroup): Pair[Operator, Seq[String]]

  def xtractStdTypeLimit(teachClass: TeachClass): Pair[Operator, Seq[StdType]]

  def xtractStdTypeLimit(group: LessonLimitGroup): Pair[Operator, Seq[StdType]]

  def xtractAttendDepartLimit(teachClass: TeachClass): Pair[Operator, Seq[Department]]

  def xtractAttendDepartLimit(group: LessonLimitGroup): Pair[Operator, Seq[Department]]

  def xtractMajorLimit(teachClass: TeachClass): Pair[Operator, Seq[Major]]

  def xtractMajorLimit(group: LessonLimitGroup): Pair[Operator, Seq[Major]]

  def xtractDirectionLimit(teachClass: TeachClass): Pair[Operator, Seq[Direction]]

  def xtractDirectionLimit(group: LessonLimitGroup): Pair[Operator, Seq[Direction]]

  def xtractProgramLimit(teachClass: TeachClass): Pair[Operator, Seq[Program]]

  def xtractProgramLimit(group: LessonLimitGroup): Pair[Operator, Seq[Program]]

  def limitTeachClass(operator: Operator, teachClass: TeachClass, grades: Array[String]): Unit

  def limitTeachClass[T <: Entity[_]](operator: Operator, teachClass: TeachClass, entities: T*): Unit

  def extractEducations(teachClass: TeachClass): Seq[Education]

  def extractEducations(group: LessonLimitGroup): Seq[Education]

  def extractAdminclasses(teachClass: TeachClass): Seq[Adminclass]

  def extractAdminclasses(group: LessonLimitGroup): Seq[Adminclass]

  def extractGrade(teachClass: TeachClass): String

  def extractGrade(group: LessonLimitGroup): String

  def extractStdTypes(teachClass: TeachClass): Seq[StdType]

  def extractStdTypes(group: LessonLimitGroup): Seq[StdType]

  def extractMajors(teachClass: TeachClass): Seq[Major]

  def extractMajors(group: LessonLimitGroup): Seq[Major]

  def extractDirections(teachClass: TeachClass): Seq[Direction]

  def extractDirections(group: LessonLimitGroup): Seq[Direction]

  def extractAttendDeparts(teachClass: TeachClass): Seq[Department]

  def extractAttendDeparts(group: LessonLimitGroup): Seq[Department]

  def extractPrograms(teachClass: TeachClass): Seq[Program]

  def extractPrograms(group: LessonLimitGroup): Seq[Program]

  def extractGender(teachClass: TeachClass): Gender

  def extractGender(group: LessonLimitGroup): Gender

  def builder(group: LessonLimitGroup): LessonLimitGroupBuilder

  def builder(teachClass: TeachClass): LessonLimitGroupBuilder

  def extractLonelyTakes(teachClass: TeachClass): collection.Set[CourseTake]

  def extractPossibleCourseTakes(teachClass: TeachClass): collection.Set[CourseTake]

  def isAutoName(lesson: Lesson): Boolean

  def xtractStdLabelLimit(group: LessonLimitGroup): Pair[Operator, Seq[StdLabel]]

  def xtractStdLabelLimit(teachClass: TeachClass): Pair[Operator, Seq[StdLabel]]

  def xtractLimitGroup(group: LessonLimitGroup): LessonLimitGroupPair
}
