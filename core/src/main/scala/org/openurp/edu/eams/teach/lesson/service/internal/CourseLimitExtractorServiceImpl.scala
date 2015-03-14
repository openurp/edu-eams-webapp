package org.openurp.edu.eams.teach.lesson.service.internal

import java.util.ArrayList
import java.util.List
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
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

import scala.collection.JavaConversions._

class CourseLimitExtractorServiceImpl extends BaseServiceImpl with CourseLimitExtractorService {

  def extractEducations(courseLimitGroup: CourseLimitGroup): List[Education] = {
    val res = xtractEducationLimit(courseLimitGroup)
    if (Operator.IN == res.getLeft || Operator.EQUAL == res.getLeft) {
      return res.getRight
    }
    CollectUtils.newArrayList()
  }

  def extractPrograms(courseLimitGroup: CourseLimitGroup): List[Program] = {
    val res = xtractProgramLimit(courseLimitGroup)
    if (Operator.IN == res.getLeft || Operator.EQUAL == res.getLeft) {
      return res.getRight
    }
    CollectUtils.newArrayList()
  }

  def extractAdminclasses(courseLimitGroup: CourseLimitGroup): List[Adminclass] = {
    courseLimitGroup.getItems.find(item => CourseLimitMetaEnum.ADMINCLASS.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator))
      .map(entityDao.get(classOf[Adminclass], Strings.splitToInt(_.getContent)))
      .getOrElse(CollectUtils.newArrayList())
  }

  def extractGrade(courseLimitGroup: CourseLimitGroup): String = {
    courseLimitGroup.getItems.find(item => CourseLimitMetaEnum.GRADE.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator))
      .map(_.getContent)
      .getOrElse(null)
  }

  def extractStdTypes(courseLimitGroup: CourseLimitGroup): List[StdType] = {
    courseLimitGroup.getItems.find(item => CourseLimitMetaEnum.STDTYPE.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator))
      .map(entityDao.get(classOf[StdType], Strings.splitToInt(_.getContent)))
      .getOrElse(CollectUtils.newArrayList())
  }

  def extractMajors(courseLimitGroup: CourseLimitGroup): List[Major] = {
    courseLimitGroup.getItems.find(item => CourseLimitMetaEnum.MAJOR.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator))
      .map(entityDao.get(classOf[Major], Strings.splitToInt(_.getContent)))
      .getOrElse(CollectUtils.newArrayList())
  }

  def extractDirections(courseLimitGroup: CourseLimitGroup): List[Direction] = {
    courseLimitGroup.getItems.find(item => CourseLimitMetaEnum.DIRECTION.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator))
      .map(entityDao.get(classOf[Direction], Strings.splitToInt(_.getContent)))
      .getOrElse(CollectUtils.newArrayList())
  }

  def extractAttendDeparts(courseLimitGroup: CourseLimitGroup): List[Department] = {
    courseLimitGroup.getItems.find(item => CourseLimitMetaEnum.DEPARTMENT.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator))
      .map(entityDao.get(classOf[Department], Strings.splitToInt(_.getContent)))
      .getOrElse(CollectUtils.newArrayList())
  }

  def extractGender(courseLimitGroup: CourseLimitGroup): Gender = {
    courseLimitGroup.getItems.find(item => CourseLimitMetaEnum.GENDER.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator))
      .map(entityDao.get(classOf[Gender], Strings.splitToInt(_.getContent))
      .get(0))
      .getOrElse(null)
  }

  def xtractEducationLimit(courseLimitGroup: CourseLimitGroup): Pair[CourseLimitMeta.Operator, List[Education]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.EDUCATION.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[Education]](item.getOperator, entityDao.get(classOf[Education], 
      Strings.splitToInt(item.getContent))))
      .getOrElse(new Pair[Operator, List[Education]](null, new ArrayList[Education]()))
  }

  def xtractAdminclassLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Adminclass]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.ADMINCLASS.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[Adminclass]](item.getOperator, entityDao.get(classOf[Adminclass], 
      Strings.splitToInt(item.getContent))))
      .getOrElse(new Pair[Operator, List[Adminclass]](null, new ArrayList[Adminclass]()))
  }

  def xtractAttendDepartLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Department]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.DEPARTMENT.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[Department]](item.getOperator, entityDao.get(classOf[Department], 
      Strings.splitToInt(item.getContent))))
      .getOrElse(new Pair[Operator, List[Department]](null, new ArrayList[Department]()))
  }

  def xtractDirectionLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Direction]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.DIRECTION.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[Direction]](item.getOperator, entityDao.get(classOf[Direction], 
      Strings.splitToInt(item.getContent))))
      .getOrElse(new Pair[Operator, List[Direction]](null, new ArrayList[Direction]()))
  }

  def xtractProgramLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Program]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.PROGRAM.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[Program]](item.getOperator, entityDao.get(classOf[Program], 
      Strings.splitToLong(item.getContent))))
      .getOrElse(new Pair[Operator, List[Program]](null, new ArrayList[Program]()))
  }

  def xtractGradeLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[String]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.GRADE.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[String]](item.getOperator, CollectUtils.newArrayList(item.getContent)))
      .getOrElse(new Pair[Operator, List[String]](null, new ArrayList[String]()))
  }

  def xtractMajorLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[Major]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.MAJOR.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[Major]](item.getOperator, entityDao.get(classOf[Major], Strings.splitToInt(item.getContent))))
      .getOrElse(new Pair[Operator, List[Major]](null, new ArrayList[Major]()))
  }

  def xtractStdTypeLimit(courseLimitGroup: CourseLimitGroup): Pair[Operator, List[StdType]] = {
    courseLimitGroup.getItems.find(CourseLimitMetaEnum.STDTYPE.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[StdType]](item.getOperator, entityDao.get(classOf[StdType], 
      Strings.splitToInt(item.getContent))))
      .getOrElse(new Pair[Operator, List[StdType]](null, new ArrayList[StdType]()))
  }
}
