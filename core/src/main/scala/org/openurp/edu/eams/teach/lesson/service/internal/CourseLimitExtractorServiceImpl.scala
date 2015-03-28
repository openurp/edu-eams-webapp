package org.openurp.edu.eams.teach.lesson.service.internal

import org.beangle.commons.collection.Collections
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings

import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators._
import org.openurp.edu.eams.teach.lesson.service.LessonLimitExtractorService
import org.openurp.edu.base.Program

class LessonLimitExtractorServiceImpl extends BaseServiceImpl with LessonLimitExtractorService {

  def extractEducations(lessonLimitGroup: LessonLimitGroup): Seq[Education] = {
    val res = xtractEducationLimit(lessonLimitGroup)
    if (Operators.IN == res._1 || Operators.Equals == res._2) {
      return res._2
    }
    Collections.newBuffer[Education]
  }

  def extractPrograms(lessonLimitGroup: LessonLimitGroup): Seq[Program] = {
    val res = xtractProgramLimit(lessonLimitGroup)
    if (Operators.IN == res._1 || Operators.Equals == res._2) {
      return res._2
    }
    Collections.newBuffer[Program]
  }

  def extractAdminclasses(lessonLimitGroup: LessonLimitGroup): Seq[Adminclass] = {
    lessonLimitGroup.items.find(item => LessonLimitMeta.Adminclass.id == item.meta.id &&
      (Operators.IN == item.operator || Operators.Equals == item.operator))
      .map(x => entityDao.find(classOf[Adminclass], Strings.splitToLong(x.content)))
      .getOrElse(Collections.newBuffer[Adminclass])
  }

  def extractGrade(lessonLimitGroup: LessonLimitGroup): String = {
    lessonLimitGroup.items.find(item => LessonLimitMeta.Grade.id == item.meta.id &&
      (Operators.IN == item.operator || Operators.Equals == item.operator))
      .map(_.content)
      .getOrElse(null)
  }

  def extractStdTypes(lessonLimitGroup: LessonLimitGroup): Seq[StdType] = {
    lessonLimitGroup.items.find(item => LessonLimitMeta.StdType.id == item.meta.id &&
      (Operators.IN == item.operator || Operators.Equals == item.operator))
      .map(x => entityDao.find(classOf[StdType], Strings.splitToInteger(x.content)))
      .getOrElse(Collections.newBuffer[StdType])
  }

  def extractMajors(lessonLimitGroup: LessonLimitGroup): Seq[Major] = {
    lessonLimitGroup.items.find(item => LessonLimitMeta.Major.id == item.meta.id &&
      (Operators.IN == item.operator || Operators.Equals == item.operator))
      .map(x => entityDao.find(classOf[Major], Strings.splitToInteger(x.content)))
      .getOrElse(Collections.newBuffer[Major])
  }

  def extractDirections(lessonLimitGroup: LessonLimitGroup): Seq[Direction] = {
    lessonLimitGroup.items.find(item => LessonLimitMeta.Direction.id == item.meta.id &&
      (Operators.IN == item.operator || Operators.Equals == item.operator))
      .map(x => entityDao.find(classOf[Direction], Strings.splitToInteger(x.content)))
      .getOrElse(Collections.newBuffer[Direction])
  }

  def extractAttendDeparts(lessonLimitGroup: LessonLimitGroup): Seq[Department] = {
    lessonLimitGroup.items.find(item => LessonLimitMeta.Department.id == item.meta.id &&
      (Operators.IN == item.operator || Operators.Equals == item.operator))
      .map(x => entityDao.find(classOf[Department], Strings.splitToInteger(x.content)))
      .getOrElse(Collections.newBuffer[Department])
  }

  def extractGender(lessonLimitGroup: LessonLimitGroup): Gender = {
    lessonLimitGroup.items.find(item => LessonLimitMeta.Gender.id == item.meta.id &&
      (Operators.IN == item.operator || Operators.Equals == item.operator))
      .map(x => entityDao.find(classOf[Gender], Strings.splitToInteger(x.content))(0))
      .getOrElse(null)
  }

  def xtractEducationLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Education]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.Education.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[Education]](item.operator, entityDao.find(classOf[Education],
        Strings.splitToInteger(item.content))))
      .getOrElse(new Pair[Operator, Seq[Education]](null, Collections.newBuffer[Education]))
  }

  def xtractAdminclassLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Adminclass]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.Adminclass.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[Adminclass]](item.operator, entityDao.find(classOf[Adminclass],
        Strings.splitToLong(item.content))))
      .getOrElse(new Pair[Operator, Seq[Adminclass]](null, Collections.newBuffer[Adminclass]))
  }

  def xtractAttendDepartLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Department]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.Department.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[Department]](item.operator, entityDao.find(classOf[Department],
        Strings.splitToInteger(item.content))))
      .getOrElse(new Pair[Operator, Seq[Department]](null, Collections.newBuffer[Department]))
  }

  def xtractDirectionLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Direction]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.Direction.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[Direction]](item.operator, entityDao.find(classOf[Direction],
        Strings.splitToInteger(item.content))))
      .getOrElse(new Pair[Operator, Seq[Direction]](null, Collections.newBuffer[Direction]))
  }

  def xtractProgramLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Program]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.Program.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[Program]](item.operator, entityDao.find(classOf[Program],
        Strings.splitToLong(item.content))))
      .getOrElse(new Pair[Operator, Seq[Program]](null, Collections.newBuffer[Program]))
  }

  def xtractGradeLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[String]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.Grade.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[String]](item.operator, Collections.newBuffer[String](item.content)))
      .getOrElse(new Pair[Operator, Seq[String]](null, Collections.newBuffer[String]))
  }

  def xtractMajorLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[Major]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.Major.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[Major]](item.operator, entityDao.find(classOf[Major], Strings.splitToInteger(item.content))))
      .getOrElse(new Pair[Operator, Seq[Major]](null, Collections.newBuffer[Major]))
  }

  def xtractStdTypeLimit(lessonLimitGroup: LessonLimitGroup): Pair[Operator, Seq[StdType]] = {
    lessonLimitGroup.items.find(LessonLimitMeta.StdType.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[StdType]](item.operator, entityDao.find(classOf[StdType],
        Strings.splitToInteger(item.content))))
      .getOrElse(new Pair[Operator, Seq[StdType]](null, Collections.newBuffer[StdType]))
  }
}
