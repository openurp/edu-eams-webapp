package org.openurp.edu.eams.teach.lesson.service.internal



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings

import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.service.CourseLimitExtractorService
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.base.Program



class CourseLimitExtractorServiceImpl extends BaseServiceImpl with CourseLimitExtractorService {

  def extractEducations(courseLimitGroup: CourseLimitGroup): List[Education] = {
    val res = xtractEducationLimit(courseLimitGroup)
    if (Operator.IN == res.left || Operator.EQUAL == res.left) {
      return res.right
    }
    CollectUtils.newArrayList()
  }

  def extractPrograms(courseLimitGroup: CourseLimitGroup): List[Program] = {
    val res = xtractProgramLimit(courseLimitGroup)
    if (Operator.IN == res.left || Operator.EQUAL == res.left) {
      return res.right
    }
    CollectUtils.newArrayList()
  }

  def extractAdminclasses(courseLimitGroup: CourseLimitGroup): List[Adminclass] = {
    courseLimitGroup.items.find(item => CourseLimitMetaEnum.ADMINCLASS.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator))
      .map(entityDao.get(classOf[Adminclass], Strings.splitToInt(_.content)))
      .orElse(CollectUtils.newArrayList())
  }

  def extractGrade(courseLimitGroup: CourseLimitGroup): String = {
    courseLimitGroup.items.find(item => CourseLimitMetaEnum.GRADE.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator))
      .map(_.content)
      .orElse(null)
  }

  def extractStdTypes(courseLimitGroup: CourseLimitGroup): List[StdType] = {
    courseLimitGroup.items.find(item => CourseLimitMetaEnum.STDTYPE.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator))
      .map(entityDao.get(classOf[StdType], Strings.splitToInt(_.content)))
      .orElse(CollectUtils.newArrayList())
  }

  def extractMajors(courseLimitGroup: CourseLimitGroup): List[Major] = {
    courseLimitGroup.items.find(item => CourseLimitMetaEnum.MAJOR.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator))
      .map(entityDao.get(classOf[Major], Strings.splitToInt(_.content)))
      .orElse(CollectUtils.newArrayList())
  }

  def extractDirections(courseLimitGroup: CourseLimitGroup): List[Direction] = {
    courseLimitGroup.items.find(item => CourseLimitMetaEnum.DIRECTION.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator))
      .map(entityDao.get(classOf[Direction], Strings.splitToInt(_.content)))
      .orElse(CollectUtils.newArrayList())
  }

  def extractAttendDeparts(courseLimitGroup: CourseLimitGroup): List[Department] = {
    courseLimitGroup.items.find(item => CourseLimitMetaEnum.DEPARTMENT.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator))
      .map(entityDao.get(classOf[Department], Strings.splitToInt(_.content)))
      .orElse(CollectUtils.newArrayList())
  }

  def extractGender(courseLimitGroup: CourseLimitGroup): Gender = {
    courseLimitGroup.items.find(item => CourseLimitMetaEnum.GENDER.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator))
      .map(entityDao.get(classOf[Gender], Strings.splitToInt(_.content))
      .get(0))
      .orElse(null)
  }

  def xtractEducationLimit(courseLimitGroup: CourseLimitGroup): Pair[CourseLimitMeta.Operator, List[Education]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.EDUCATION.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[Education]](item.operator, entityDao.get(classOf[Education], 
      Strings.splitToInt(item.content))))
      .orElse(new Pair[Operator, List[Education]](null, new ArrayList[Education]()))
  }

  def xtractAdminclassLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Adminclass]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.ADMINCLASS.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[Adminclass]](item.operator, entityDao.get(classOf[Adminclass], 
      Strings.splitToInt(item.content))))
      .orElse(new Pair[Operator, List[Adminclass]](null, new ArrayList[Adminclass]()))
  }

  def xtractAttendDepartLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Department]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.DEPARTMENT.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[Department]](item.operator, entityDao.get(classOf[Department], 
      Strings.splitToInt(item.content))))
      .orElse(new Pair[Operator, List[Department]](null, new ArrayList[Department]()))
  }

  def xtractDirectionLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Direction]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.DIRECTION.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[Direction]](item.operator, entityDao.get(classOf[Direction], 
      Strings.splitToInt(item.content))))
      .orElse(new Pair[Operator, List[Direction]](null, new ArrayList[Direction]()))
  }

  def xtractProgramLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Program]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.PROGRAM.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[Program]](item.operator, entityDao.get(classOf[Program], 
      Strings.splitToLong(item.content))))
      .orElse(new Pair[Operator, List[Program]](null, new ArrayList[Program]()))
  }

  def xtractGradeLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[String]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.GRADE.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[String]](item.operator, CollectUtils.newArrayList(item.content)))
      .orElse(new Pair[Operator, List[String]](null, new ArrayList[String]()))
  }

  def xtractMajorLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Major]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.MAJOR.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[Major]](item.operator, entityDao.get(classOf[Major], Strings.splitToInt(item.content))))
      .orElse(new Pair[Operator, List[Major]](null, new ArrayList[Major]()))
  }

  def xtractStdTypeLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[StdType]] = {
    courseLimitGroup.items.find(CourseLimitMetaEnum.STDTYPE.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[StdType]](item.operator, entityDao.get(classOf[StdType], 
      Strings.splitToInt(item.content))))
      .orElse(new Pair[Operator, List[StdType]](null, new ArrayList[StdType]()))
  }
}
