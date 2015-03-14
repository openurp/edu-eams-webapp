package org.openurp.edu.eams.teach.lesson.service.internal

import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator
import java.util.HashSet
import java.util.List
import java.util.Set
import java.util.TreeSet
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.edu.base.Student
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.model.CourseLimitGroupPair
import org.openurp.edu.eams.teach.lesson.model.CourseLimitMetaBean
import org.openurp.edu.eams.teach.lesson.model.NormalClassBean
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.eams.teach.lesson.service.CourseLimitGroupBuilder
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.base.Program

import scala.collection.JavaConversions._

class CourseLimitServiceImpl extends BaseServiceImpl with CourseLimitService {

  var teachClassNameStrategy: TeachClassNameStrategy = _

  @Deprecated
  def mergeAll(target: TeachClass, source: TeachClass) {
  }

  @Deprecated
  def merge(mergeType: java.lang.Long, target: TeachClass, source: TeachClass) {
    if (CourseLimitMetaEnum.ADMINCLASS.getMetaId == mergeType) {
      val tmp_collection = new HashSet[Adminclass]()
      val targetCollection = extractAdminclasses(target)
      if (CollectUtils.isNotEmpty(targetCollection)) {
        tmp_collection.addAll(targetCollection)
      }
      val sourceCollection = extractAdminclasses(source)
      if (CollectUtils.isNotEmpty(sourceCollection)) {
        tmp_collection.addAll(sourceCollection)
      }
      limitTeachClass(Operator.IN, target, tmp_collection.toArray(Array.ofDim[Adminclass](0)))
    } else if (CourseLimitMetaEnum.DEPARTMENT.getMetaId == mergeType) {
      val tmp_collection = new HashSet[Department]()
      val targetCollection = extractAttendDeparts(target)
      if (CollectUtils.isNotEmpty(targetCollection)) {
        tmp_collection.addAll(targetCollection)
      }
      val sourceCollection = extractAttendDeparts(source)
      if (CollectUtils.isNotEmpty(sourceCollection)) {
        tmp_collection.addAll(sourceCollection)
      }
      limitTeachClass(Operator.IN, target, tmp_collection.toArray(Array.ofDim[Department](0)))
    } else if (CourseLimitMetaEnum.STDTYPE.getMetaId == mergeType) {
      val tmp_collection = new HashSet[StdType]()
      val targetCollection = extractStdTypes(target)
      if (CollectUtils.isNotEmpty(targetCollection)) {
        tmp_collection.addAll(targetCollection)
      }
      val sourceCollection = extractStdTypes(source)
      if (CollectUtils.isNotEmpty(sourceCollection)) {
        tmp_collection.addAll(sourceCollection)
      }
      limitTeachClass(Operator.IN, target, tmp_collection.toArray(Array.ofDim[StdType](0)))
    } else if (CourseLimitMetaEnum.DIRECTION.getMetaId == mergeType) {
      val tmp_collection = new HashSet[Direction]()
      val targetCollection = extractDirections(target)
      if (CollectUtils.isNotEmpty(targetCollection)) {
        tmp_collection.addAll(targetCollection)
      }
      val sourceCollection = extractDirections(source)
      if (CollectUtils.isNotEmpty(sourceCollection)) {
        tmp_collection.addAll(sourceCollection)
      }
      limitTeachClass(Operator.IN, target, tmp_collection.toArray(Array.ofDim[Direction](0)))
    } else if (CourseLimitMetaEnum.GENDER.getMetaId == mergeType) {
      val targetGender = extractGender(target)
      val sourceGender = extractGender(source)
      var tmp_gender: Gender = null
      if (targetGender != null && sourceGender != null) {
        if (targetGender == sourceGender) {
          tmp_gender = targetGender
        }
      }
      if (targetGender != null && sourceGender == null) {
        tmp_gender = targetGender
      }
      if (targetGender == null && sourceGender != null) {
        tmp_gender = sourceGender
      }
      if (tmp_gender != null) {
        limitTeachClass(Operator.IN, target, tmp_gender)
      }
    } else if (CourseLimitMetaEnum.GRADE.getMetaId == mergeType) {
      var tmp_grade = ""
      val targetGrade = extractGrade(target)
      if (Strings.isNotBlank(targetGrade)) {
        tmp_grade = targetGrade
      }
      val sourceGrade = extractGrade(source)
      if (Strings.isNotBlank(sourceGrade)) {
        tmp_grade += "," + sourceGrade
      }
      val grades = Strings.split(tmp_grade)
      if (grades.length > 0) {
        limitTeachClass(Operator.IN, target, grades)
      }
    } else if (CourseLimitMetaEnum.MAJOR.getMetaId == mergeType) {
      val tmp_collection = new HashSet[Major]()
      val targetCollection = extractMajors(target)
      if (CollectUtils.isNotEmpty(targetCollection)) {
        tmp_collection.addAll(targetCollection)
      }
      val sourceCollection = extractMajors(source)
      if (CollectUtils.isNotEmpty(sourceCollection)) {
        tmp_collection.addAll(sourceCollection)
      }
      limitTeachClass(Operator.IN, target, tmp_collection.toArray(Array.ofDim[Major](0)))
    } else if (CourseLimitMetaEnum.EDUCATION.getMetaId == mergeType) {
      val tmp_collection = new HashSet[Education]()
      val targetCollection = extractEducations(target)
      if (CollectUtils.isNotEmpty(targetCollection)) {
        tmp_collection.addAll(targetCollection)
      }
      val sourceCollection = extractEducations(source)
      if (CollectUtils.isNotEmpty(sourceCollection)) {
        tmp_collection.addAll(sourceCollection)
      }
      limitTeachClass(Operator.IN, target, tmp_collection.toArray(Array.ofDim[Education](0)))
    } else if (CourseLimitMetaEnum.PROGRAM.getMetaId == mergeType) {
      val tmp_collection = new HashSet[Program]()
      val targetCollection = extractPrograms(target)
      if (CollectUtils.isNotEmpty(targetCollection)) {
        tmp_collection.addAll(targetCollection)
      }
      val sourceCollection = extractPrograms(source)
      if (CollectUtils.isNotEmpty(sourceCollection)) {
        tmp_collection.addAll(sourceCollection)
      }
      limitTeachClass(Operator.IN, target, tmp_collection.toArray(Array.ofDim[Program](0)))
    } else {
      throw new RuntimeException("unsupported limit meta merge")
    }
  }

  def extractEducations(teachClass: TeachClass): List[Education] = {
    val res = xtractEducationLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractEducations(group: CourseLimitGroup): List[Education] = {
    val res = xtractEducationLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractPrograms(teachClass: TeachClass): List[Program] = {
    val res = xtractProgramLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractPrograms(group: CourseLimitGroup): List[Program] = {
    val res = xtractProgramLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractAdminclasses(teachClass: TeachClass): List[Adminclass] = {
    val res = xtractAdminclassLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractAdminclasses(group: CourseLimitGroup): List[Adminclass] = {
    val res = xtractAdminclassLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractGrade(teachClass: TeachClass): String = {
    val res = xtractGradeLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      if (CollectUtils.isNotEmpty(res._2)) {
        return res._2.get(0)
      }
    }
    null
  }

  def extractGrade(group: CourseLimitGroup): String = {
    val res = xtractGradeLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      if (CollectUtils.isNotEmpty(res._2)) {
        return res._2.get(0)
      }
    }
    null
  }

  def extractStdTypes(teachClass: TeachClass): List[StdType] = {
    val res = xtractStdTypeLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractStdTypes(group: CourseLimitGroup): List[StdType] = {
    val res = xtractStdTypeLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractMajors(teachClass: TeachClass): List[Major] = {
    val res = xtractMajorLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractMajors(group: CourseLimitGroup): List[Major] = {
    val res = xtractMajorLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractDirections(teachClass: TeachClass): List[Direction] = {
    val res = xtractDirectionLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractDirections(group: CourseLimitGroup): List[Direction] = {
    val res = xtractDirectionLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractAttendDeparts(teachClass: TeachClass): List[Department] = {
    val res = xtractAttendDepartLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractAttendDeparts(group: CourseLimitGroup): List[Department] = {
    val res = xtractAttendDepartLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractGender(teachClass: TeachClass): Gender = {
    val groups = teachClass.getLimitGroups
    for (group <- groups) {
      if (!group.isForClass) {
        //continue
      }
      for (item <- group.getItems if CourseLimitMetaEnum.GENDER.getMetaId == item.getMeta.getId && 
        (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator)) {
        return entityDao.get(classOf[Gender], Strings.splitToInt(item.getContent))
          .get(0)
      }
    }
    null
  }

  def extractGender(group: CourseLimitGroup): Gender = {
    if (group == null) {
      return null
    }
    for (item <- group.getItems if CourseLimitMetaEnum.GENDER.getMetaId == item.getMeta.getId && 
      (Operator.IN == item.getOperator || Operator.EQUAL == item.getOperator)) {
      return entityDao.get(classOf[Gender], Strings.splitToInt(item.getContent))
        .get(0)
    }
    null
  }

  def extractNormalclasses(teachClass: TeachClass): List[NormalClassBean] = {
    val res = xtractNormalclassLimit(teachClass)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def extractNormalclasses(group: CourseLimitGroup): List[NormalClassBean] = {
    val res = xtractNormalclassLimit(group)
    if (Operator.IN == res._1 || Operator.EQUAL == res._1) {
      return res._2
    }
    CollectUtils.newArrayList()
  }

  def builder(): CourseLimitGroupBuilder = new DefaultCourseLimitGroupBuilder()

  private def builder(group: CourseLimitGroup): CourseLimitGroupBuilder = {
    new DefaultCourseLimitGroupBuilder(group)
  }

  def builder(teachClass: TeachClass): CourseLimitGroupBuilder = {
    val clb = teachClass.asInstanceOf[TeachClassBean]
    val group = clb.getOrCreateDefaultLimitGroup
    new DefaultCourseLimitGroupBuilder(group)
  }

  def extractLonelyTakes(teachClass: TeachClass): Set[CourseTake] = {
    val takes = teachClass.getCourseTakes
    val adminclasses = extractAdminclasses(teachClass)
    val lonelyTakes = new HashSet[CourseTake]()
    for (take <- takes) {
      var lonely = true
      for (adminclass <- adminclasses if adminclass == take.getStd.getAdminclass) {
        lonely = false
        //break
      }
      if (lonely) {
        lonelyTakes.add(take)
      }
    }
    lonelyTakes
  }

  def extractPossibleCourseTakes(teachClass: TeachClass): Set[CourseTake] = {
    val possibleTakes = new TreeSet[CourseTake](new Comparator[CourseTake]() {

      def compare(o1: CourseTake, o2: CourseTake): Int = {
        return o1.getStd.getCode.compareTo(o2.getStd.getCode)
      }
    })
    if (CollectUtils.isNotEmpty(teachClass.getCourseTakes)) {
      possibleTakes.addAll(teachClass.getCourseTakes)
      return possibleTakes
    }
    val adminclasses = extractAdminclasses(teachClass)
    val eleMode = new ElectionMode()
    eleMode.setId(ElectionMode.ASSIGEND)
    for (adminclass <- adminclasses; std <- adminclass.getStudents) {
      val take = Model.newInstance(classOf[CourseTake])
      take.setLesson(teachClass.getLesson)
      take.setStd(std)
      take.setCourseTakeType(new CourseTakeType(CourseTakeType.NORMAL))
      take.setElectionMode(eleMode)
      possibleTakes.add(take)
    }
    possibleTakes
  }

  def limitTeachClass(operator: Operator, teachClass: TeachClass, grades: String*) {
    if (grades.length > 0) {
      val clb = teachClass.asInstanceOf[TeachClassBean]
      val group = clb.getOrCreateDefaultLimitGroup
      val builder = builder(group)
      builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GRADE.getMetaId))
      operator match {
        case IN => builder.inGrades(grades)
        case EQUAL => builder.inGrades(grades)
        case NOT_IN => builder.notInGrades(grades)
        case _ => throw new RuntimeException("not supported CourseLimitItem operator")
      }
    }
  }

  def limitTeachClass[T <: Entity[_]](operator: Operator, teachClass: TeachClass, entities: T*) {
    if (entities.length > 0) {
      val clb = teachClass.asInstanceOf[TeachClassBean]
      val group = clb.getOrCreateDefaultLimitGroup
      val builder = builder(group)
      val first = entities(0)
      if (first.isInstanceOf[Adminclass]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.ADMINCLASS.getMetaId))
      } else if (first.isInstanceOf[StdType]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.STDTYPE.getMetaId))
      } else if (first.isInstanceOf[Major]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.MAJOR.getMetaId))
      } else if (first.isInstanceOf[Direction]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DIRECTION.getMetaId))
      } else if (first.isInstanceOf[Department]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DEPARTMENT.getMetaId))
      } else if (first.isInstanceOf[Education]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.EDUCATION.getMetaId))
      } else if (first.isInstanceOf[Program]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.PROGRAM.getMetaId))
      } else if (first.isInstanceOf[Gender]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GENDER.getMetaId))
      } else {
        throw new RuntimeException("not supported limit meta class " + first.getClass.getName)
      }
      operator match {
        case IN => builder.in(entities)
        case EQUAL => builder.in(entities)
        case NOT_IN => builder.notIn(entities)
        case _ => throw new RuntimeException("not supported CourseLimitItem operator")
      }
    }
  }

  private def xtractLimitDirtyWork(group: CourseLimitGroup, limitMetaId: java.lang.Long): Pair[CourseLimitMeta.Operator, List[_]] = {
    if (group == null) {
      return new Pair[Operator, List[_]](null, CollectUtils.newArrayList())
    }
    for (item <- group.getItems) {
      if (CourseLimitMetaEnum.EDUCATION.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.EDUCATION.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[Education], 
            Strings.splitToInt(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.ADMINCLASS.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.ADMINCLASS.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[Adminclass], 
            Strings.splitToInt(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.DEPARTMENT.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.DEPARTMENT.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[Department], 
            Strings.splitToInt(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.MAJOR.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.MAJOR.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[Major], 
            Strings.splitToInt(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.DIRECTION.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.DIRECTION.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[Direction], 
            Strings.splitToInt(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.PROGRAM.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.PROGRAM.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[Program], 
            Strings.splitToLong(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.STDTYPE.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.STDTYPE.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[StdType], 
            Strings.splitToInt(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.NORMALCLASS.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.NORMALCLASS.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[NormalClassBean], 
            Strings.splitToLong(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.STDLABEL.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.STDLABEL.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[StdLabel], 
            Strings.splitToInt(item.getContent)))
        }
      } else if (CourseLimitMetaEnum.GRADE.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.GRADE.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, Arrays.asList(Strings.split(item.getContent):_*))
        }
      } else if (CourseLimitMetaEnum.GENDER.getMetaId == limitMetaId) {
        if (CourseLimitMetaEnum.GENDER.getMetaId == item.getMeta.getId) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.getOperator, entityDao.get(classOf[Gender], 
            Strings.splitToInt(item.getContent)))
        }
      }
    }
    new Pair[CourseLimitMeta.Operator, List[_]](null, CollectUtils.newArrayList())
  }

  private def xtractLimitDirtyWork(teachClass: TeachClass, limitMetaId: java.lang.Long): Pair[CourseLimitMeta.Operator, List[_]] = {
    val groups = teachClass.getLimitGroups
    for (group <- groups) {
      if (!group.isForClass) {
        //continue
      }
      return xtractLimitDirtyWork(group, limitMetaId)
    }
    new Pair[CourseLimitMeta.Operator, List[_]](null, CollectUtils.newArrayList())
  }

  def xtractEducationLimit(teachClass: TeachClass): Pair[CourseLimitMeta.Operator, List[Education]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.EDUCATION.getMetaId)
    new Pair[Operator, List[Education]](tmpRes._1, tmpRes._2.asInstanceOf[List[Education]])
  }

  def xtractEducationLimit(group: CourseLimitGroup): Pair[CourseLimitMeta.Operator, List[Education]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.EDUCATION.getMetaId)
    new Pair[Operator, List[Education]](tmpRes._1, tmpRes._2.asInstanceOf[List[Education]])
  }

  def xtractAdminclassLimit(teachClass: TeachClass): Pair[Operator, List[Adminclass]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.ADMINCLASS.getMetaId)
    new Pair[Operator, List[Adminclass]](tmpRes._1, tmpRes._2.asInstanceOf[List[Adminclass]])
  }

  def xtractAdminclassLimit(group: CourseLimitGroup): Pair[Operator, List[Adminclass]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.ADMINCLASS.getMetaId)
    new Pair[Operator, List[Adminclass]](tmpRes._1, tmpRes._2.asInstanceOf[List[Adminclass]])
  }

  def xtractAttendDepartLimit(teachClass: TeachClass): Pair[Operator, List[Department]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.DEPARTMENT.getMetaId)
    new Pair[Operator, List[Department]](tmpRes._1, tmpRes._2.asInstanceOf[List[Department]])
  }

  def xtractAttendDepartLimit(group: CourseLimitGroup): Pair[Operator, List[Department]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DEPARTMENT.getMetaId)
    new Pair[Operator, List[Department]](tmpRes._1, tmpRes._2.asInstanceOf[List[Department]])
  }

  def xtractDirectionLimit(teachClass: TeachClass): Pair[Operator, List[Direction]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.DIRECTION.getMetaId)
    new Pair[Operator, List[Direction]](tmpRes._1, tmpRes._2.asInstanceOf[List[Direction]])
  }

  def xtractDirectionLimit(group: CourseLimitGroup): Pair[Operator, List[Direction]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DIRECTION.getMetaId)
    new Pair[Operator, List[Direction]](tmpRes._1, tmpRes._2.asInstanceOf[List[Direction]])
  }

  def xtractProgramLimit(teachClass: TeachClass): Pair[Operator, List[Program]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.PROGRAM.getMetaId)
    new Pair[Operator, List[Program]](tmpRes._1, tmpRes._2.asInstanceOf[List[Program]])
  }

  def xtractProgramLimit(group: CourseLimitGroup): Pair[Operator, List[Program]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.PROGRAM.getMetaId)
    new Pair[Operator, List[Program]](tmpRes._1, tmpRes._2.asInstanceOf[List[Program]])
  }

  def xtractNormalclassLimit(teachClass: TeachClass): Pair[Operator, List[NormalClassBean]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.NORMALCLASS.getMetaId)
    new Pair[Operator, List[NormalClassBean]](tmpRes._1, tmpRes._2.asInstanceOf[List[NormalClassBean]])
  }

  def xtractNormalclassLimit(group: CourseLimitGroup): Pair[Operator, List[NormalClassBean]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.NORMALCLASS.getMetaId)
    new Pair[Operator, List[NormalClassBean]](tmpRes._1, tmpRes._2.asInstanceOf[List[NormalClassBean]])
  }

  def xtractGradeLimit(teachClass: TeachClass): Pair[Operator, List[String]] = {
    val groups = teachClass.getLimitGroups
    for (group <- groups) {
      if (!group.isForClass) {
        //continue
      }
      return xtractGradeLimit(group)
    }
    new Pair[Operator, List[String]](null, new ArrayList[String]())
  }

  def xtractGradeLimit(group: CourseLimitGroup): Pair[Operator, List[String]] = {
    group.getItems.find(CourseLimitMetaEnum.GRADE.getMetaId == _.getMeta.getId)
      .map(item => new Pair[Operator, List[String]](item.getOperator, CollectUtils.newArrayList(item.getContent)))
      .getOrElse(new Pair[Operator, List[String]](null, new ArrayList[String]()))
  }

  def xtractMajorLimit(teachClass: TeachClass): Pair[Operator, List[Major]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.MAJOR.getMetaId)
    new Pair[Operator, List[Major]](tmpRes._1, tmpRes._2.asInstanceOf[List[Major]])
  }

  def xtractMajorLimit(group: CourseLimitGroup): Pair[Operator, List[Major]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.MAJOR.getMetaId)
    new Pair[Operator, List[Major]](tmpRes._1, tmpRes._2.asInstanceOf[List[Major]])
  }

  def xtractStdTypeLimit(teachClass: TeachClass): Pair[Operator, List[StdType]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.STDTYPE.getMetaId)
    new Pair[Operator, List[StdType]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdType]])
  }

  def xtractStdTypeLimit(group: CourseLimitGroup): Pair[Operator, List[StdType]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDTYPE.getMetaId)
    new Pair[Operator, List[StdType]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdType]])
  }

  def isAutoName(lesson: Lesson): Boolean = {
    var isAutoName = true
    if (lesson.getId != null) {
      val teachClassName = lesson.getTeachClass.getName
      val autoName = teachClassNameStrategy.genFullname(lesson.getTeachClass)
      if (teachClassName != null && teachClassName != autoName) {
        isAutoName = false
      }
    }
    isAutoName
  }

  def xtractStdLabelLimit(teachClass: TeachClass): Pair[Operator, List[StdLabel]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.STDLABEL.getMetaId)
    new Pair[Operator, List[StdLabel]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdLabel]])
  }

  def xtractStdLabelLimit(group: CourseLimitGroup): Pair[Operator, List[StdLabel]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDLABEL.getMetaId)
    new Pair[Operator, List[StdLabel]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdLabel]])
  }

  def xtractLimitGroup(group: CourseLimitGroup): CourseLimitGroupPair = {
    val pair = new CourseLimitGroupPair(group)
    val gradeLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.GRADE.getMetaId)
    if (gradeLimit._1 != null) {
      pair.setGradeLimit(gradeLimit)
    }
    val stdTypeLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDTYPE.getMetaId)
    if (stdTypeLimit._1 != null) {
      pair.setStdTypeLimit(stdTypeLimit)
    }
    val genderLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.GENDER.getMetaId)
    if (genderLimit._1 != null) {
      pair.setGenderLimit(genderLimit)
    }
    val departLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DEPARTMENT.getMetaId)
    if (departLimit._1 != null) {
      pair.setDepartmentLimit(departLimit)
    }
    val majorLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.MAJOR.getMetaId)
    if (majorLimit._1 != null) {
      pair.setMajorLimit(majorLimit)
    }
    val directionLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DIRECTION.getMetaId)
    if (directionLimit._1 != null) {
      pair.setDirectionLimit(directionLimit)
    }
    val adminclassLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.ADMINCLASS.getMetaId)
    if (adminclassLimit._1 != null) {
      pair.setAdminclassLimit(adminclassLimit)
    }
    val educationLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.EDUCATION.getMetaId)
    if (educationLimit._1 != null) {
      pair.setEducationLimit(educationLimit)
    }
    val programLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.PROGRAM.getMetaId)
    if (programLimit._1 != null) {
      pair.setProgramLimit(programLimit)
    }
    val normalClassLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.NORMALCLASS.getMetaId)
    if (normalClassLimit._1 != null) {
      pair.setNormalClassLimit(normalClassLimit)
    }
    val stdlabelLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDLABEL.getMetaId)
    if (stdlabelLimit._1 != null) {
      pair.setStdLabelLimit(stdlabelLimit)
    }
    pair
  }

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }
}
