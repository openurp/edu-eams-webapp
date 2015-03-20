package org.openurp.edu.eams.teach.lesson.service.internal


import java.util.Arrays
import java.util.Comparator



import java.util.TreeSet
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings

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



class CourseLimitServiceImpl extends BaseServiceImpl with CourseLimitService {

  var teachClassNameStrategy: TeachClassNameStrategy = _

  @Deprecated
  def mergeAll(target: TeachClass, source: TeachClass) {
  }

  @Deprecated
  def merge(mergeType: java.lang.Long, target: TeachClass, source: TeachClass) {
    if (CourseLimitMetaEnum.ADMINCLASS.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.DEPARTMENT.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.STDTYPE.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.DIRECTION.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.GENDER.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.GRADE.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.MAJOR.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.EDUCATION.metaId == mergeType) {
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
    } else if (CourseLimitMetaEnum.PROGRAM.metaId == mergeType) {
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
    val groups = teachClass.limitGroups
    for (group <- groups) {
      if (!group.isForClass) {
        //continue
      }
      for (item <- group.items if CourseLimitMetaEnum.GENDER.metaId == item.meta.id && 
        (Operator.IN == item.operator || Operator.EQUAL == item.operator)) {
        return entityDao.get(classOf[Gender], Strings.splitToInt(item.content))
          .get(0)
      }
    }
    null
  }

  def extractGender(group: CourseLimitGroup): Gender = {
    if (group == null) {
      return null
    }
    for (item <- group.items if CourseLimitMetaEnum.GENDER.metaId == item.meta.id && 
      (Operator.IN == item.operator || Operator.EQUAL == item.operator)) {
      return entityDao.get(classOf[Gender], Strings.splitToInt(item.content))
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
    val group = clb.orCreateDefaultLimitGroup
    new DefaultCourseLimitGroupBuilder(group)
  }

  def extractLonelyTakes(teachClass: TeachClass): Set[CourseTake] = {
    val takes = teachClass.courseTakes
    val adminclasses = extractAdminclasses(teachClass)
    val lonelyTakes = new HashSet[CourseTake]()
    for (take <- takes) {
      var lonely = true
      for (adminclass <- adminclasses if adminclass == take.std.adminclass) {
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
        return o1.std.code.compareTo(o2.std.code)
      }
    })
    if (CollectUtils.isNotEmpty(teachClass.courseTakes)) {
      possibleTakes.addAll(teachClass.courseTakes)
      return possibleTakes
    }
    val adminclasses = extractAdminclasses(teachClass)
    val eleMode = new ElectionMode()
    eleMode.id=ElectionMode.ASSIGEND
    for (adminclass <- adminclasses; std <- adminclass.students) {
      val take = Model.newInstance(classOf[CourseTake])
      take.lesson=teachClass.lesson
      take.std=std
      take.courseTakeType=new CourseTakeType(CourseTakeType.NORMAL)
      take.electionMode=eleMode
      possibleTakes.add(take)
    }
    possibleTakes
  }

  def limitTeachClass(operator: Operator, teachClass: TeachClass, grades: String*) {
    if (grades.length > 0) {
      val clb = teachClass.asInstanceOf[TeachClassBean]
      val group = clb.orCreateDefaultLimitGroup
      val builder = builder(group)
      builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GRADE.metaId))
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
      val group = clb.orCreateDefaultLimitGroup
      val builder = builder(group)
      val first = entities(0)
      if (first.isInstanceOf[Adminclass]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.ADMINCLASS.metaId))
      } else if (first.isInstanceOf[StdType]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.STDTYPE.metaId))
      } else if (first.isInstanceOf[Major]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.MAJOR.metaId))
      } else if (first.isInstanceOf[Direction]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DIRECTION.metaId))
      } else if (first.isInstanceOf[Department]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DEPARTMENT.metaId))
      } else if (first.isInstanceOf[Education]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.EDUCATION.metaId))
      } else if (first.isInstanceOf[Program]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.PROGRAM.metaId))
      } else if (first.isInstanceOf[Gender]) {
        builder.clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GENDER.metaId))
      } else {
        throw new RuntimeException("not supported limit meta class " + first.getClass.name)
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
    for (item <- group.items) {
      if (CourseLimitMetaEnum.EDUCATION.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.EDUCATION.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[Education], 
            Strings.splitToInt(item.content)))
        }
      } else if (CourseLimitMetaEnum.ADMINCLASS.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.ADMINCLASS.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[Adminclass], 
            Strings.splitToInt(item.content)))
        }
      } else if (CourseLimitMetaEnum.DEPARTMENT.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.DEPARTMENT.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[Department], 
            Strings.splitToInt(item.content)))
        }
      } else if (CourseLimitMetaEnum.MAJOR.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.MAJOR.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[Major], 
            Strings.splitToInt(item.content)))
        }
      } else if (CourseLimitMetaEnum.DIRECTION.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.DIRECTION.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[Direction], 
            Strings.splitToInt(item.content)))
        }
      } else if (CourseLimitMetaEnum.PROGRAM.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.PROGRAM.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[Program], 
            Strings.splitToLong(item.content)))
        }
      } else if (CourseLimitMetaEnum.STDTYPE.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.STDTYPE.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[StdType], 
            Strings.splitToInt(item.content)))
        }
      } else if (CourseLimitMetaEnum.NORMALCLASS.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.NORMALCLASS.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[NormalClassBean], 
            Strings.splitToLong(item.content)))
        }
      } else if (CourseLimitMetaEnum.STDLABEL.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.STDLABEL.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[StdLabel], 
            Strings.splitToInt(item.content)))
        }
      } else if (CourseLimitMetaEnum.GRADE.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.GRADE.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, Arrays.asList(Strings.split(item.content):_*))
        }
      } else if (CourseLimitMetaEnum.GENDER.metaId == limitMetaId) {
        if (CourseLimitMetaEnum.GENDER.metaId == item.meta.id) {
          return new Pair[CourseLimitMeta.Operator, List[_]](item.operator, entityDao.get(classOf[Gender], 
            Strings.splitToInt(item.content)))
        }
      }
    }
    new Pair[CourseLimitMeta.Operator, List[_]](null, CollectUtils.newArrayList())
  }

  private def xtractLimitDirtyWork(teachClass: TeachClass, limitMetaId: java.lang.Long): Pair[CourseLimitMeta.Operator, List[_]] = {
    val groups = teachClass.limitGroups
    for (group <- groups) {
      if (!group.isForClass) {
        //continue
      }
      return xtractLimitDirtyWork(group, limitMetaId)
    }
    new Pair[CourseLimitMeta.Operator, List[_]](null, CollectUtils.newArrayList())
  }

  def xtractEducationLimit(teachClass: TeachClass): Pair[CourseLimitMeta.Operator, List[Education]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.EDUCATION.metaId)
    new Pair[Operator, List[Education]](tmpRes._1, tmpRes._2.asInstanceOf[List[Education]])
  }

  def xtractEducationLimit(group: CourseLimitGroup): Pair[CourseLimitMeta.Operator, List[Education]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.EDUCATION.metaId)
    new Pair[Operator, List[Education]](tmpRes._1, tmpRes._2.asInstanceOf[List[Education]])
  }

  def xtractAdminclassLimit(teachClass: TeachClass): Pair[Operator, List[Adminclass]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.ADMINCLASS.metaId)
    new Pair[Operator, List[Adminclass]](tmpRes._1, tmpRes._2.asInstanceOf[List[Adminclass]])
  }

  def xtractAdminclassLimit(group: CourseLimitGroup): Pair[Operator, List[Adminclass]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.ADMINCLASS.metaId)
    new Pair[Operator, List[Adminclass]](tmpRes._1, tmpRes._2.asInstanceOf[List[Adminclass]])
  }

  def xtractAttendDepartLimit(teachClass: TeachClass): Pair[Operator, List[Department]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.DEPARTMENT.metaId)
    new Pair[Operator, List[Department]](tmpRes._1, tmpRes._2.asInstanceOf[List[Department]])
  }

  def xtractAttendDepartLimit(group: CourseLimitGroup): Pair[Operator, List[Department]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DEPARTMENT.metaId)
    new Pair[Operator, List[Department]](tmpRes._1, tmpRes._2.asInstanceOf[List[Department]])
  }

  def xtractDirectionLimit(teachClass: TeachClass): Pair[Operator, List[Direction]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.DIRECTION.metaId)
    new Pair[Operator, List[Direction]](tmpRes._1, tmpRes._2.asInstanceOf[List[Direction]])
  }

  def xtractDirectionLimit(group: CourseLimitGroup): Pair[Operator, List[Direction]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DIRECTION.metaId)
    new Pair[Operator, List[Direction]](tmpRes._1, tmpRes._2.asInstanceOf[List[Direction]])
  }

  def xtractProgramLimit(teachClass: TeachClass): Pair[Operator, List[Program]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.PROGRAM.metaId)
    new Pair[Operator, List[Program]](tmpRes._1, tmpRes._2.asInstanceOf[List[Program]])
  }

  def xtractProgramLimit(group: CourseLimitGroup): Pair[Operator, List[Program]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.PROGRAM.metaId)
    new Pair[Operator, List[Program]](tmpRes._1, tmpRes._2.asInstanceOf[List[Program]])
  }

  def xtractNormalclassLimit(teachClass: TeachClass): Pair[Operator, List[NormalClassBean]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.NORMALCLASS.metaId)
    new Pair[Operator, List[NormalClassBean]](tmpRes._1, tmpRes._2.asInstanceOf[List[NormalClassBean]])
  }

  def xtractNormalclassLimit(group: CourseLimitGroup): Pair[Operator, List[NormalClassBean]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.NORMALCLASS.metaId)
    new Pair[Operator, List[NormalClassBean]](tmpRes._1, tmpRes._2.asInstanceOf[List[NormalClassBean]])
  }

  def xtractGradeLimit(teachClass: TeachClass): Pair[Operator, List[String]] = {
    val groups = teachClass.limitGroups
    for (group <- groups) {
      if (!group.isForClass) {
        //continue
      }
      return xtractGradeLimit(group)
    }
    new Pair[Operator, List[String]](null, new ArrayList[String]())
  }

  def xtractGradeLimit(group: CourseLimitGroup): Pair[Operator, List[String]] = {
    group.items.find(CourseLimitMetaEnum.GRADE.metaId == _.meta.id)
      .map(item => new Pair[Operator, List[String]](item.operator, CollectUtils.newArrayList(item.content)))
      .orElse(new Pair[Operator, List[String]](null, new ArrayList[String]()))
  }

  def xtractMajorLimit(teachClass: TeachClass): Pair[Operator, List[Major]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.MAJOR.metaId)
    new Pair[Operator, List[Major]](tmpRes._1, tmpRes._2.asInstanceOf[List[Major]])
  }

  def xtractMajorLimit(group: CourseLimitGroup): Pair[Operator, List[Major]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.MAJOR.metaId)
    new Pair[Operator, List[Major]](tmpRes._1, tmpRes._2.asInstanceOf[List[Major]])
  }

  def xtractStdTypeLimit(teachClass: TeachClass): Pair[Operator, List[StdType]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.STDTYPE.metaId)
    new Pair[Operator, List[StdType]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdType]])
  }

  def xtractStdTypeLimit(group: CourseLimitGroup): Pair[Operator, List[StdType]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDTYPE.metaId)
    new Pair[Operator, List[StdType]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdType]])
  }

  def isAutoName(lesson: Lesson): Boolean = {
    var isAutoName = true
    if (lesson.id != null) {
      val teachClassName = lesson.teachClass.name
      val autoName = teachClassNameStrategy.genFullname(lesson.teachClass)
      if (teachClassName != null && teachClassName != autoName) {
        isAutoName = false
      }
    }
    isAutoName
  }

  def xtractStdLabelLimit(teachClass: TeachClass): Pair[Operator, List[StdLabel]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, CourseLimitMetaEnum.STDLABEL.metaId)
    new Pair[Operator, List[StdLabel]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdLabel]])
  }

  def xtractStdLabelLimit(group: CourseLimitGroup): Pair[Operator, List[StdLabel]] = {
    val tmpRes = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDLABEL.metaId)
    new Pair[Operator, List[StdLabel]](tmpRes._1, tmpRes._2.asInstanceOf[List[StdLabel]])
  }

  def xtractLimitGroup(group: CourseLimitGroup): CourseLimitGroupPair = {
    val pair = new CourseLimitGroupPair(group)
    val gradeLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.GRADE.metaId)
    if (gradeLimit._1 != null) {
      pair.gradeLimit=gradeLimit
    }
    val stdTypeLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDTYPE.metaId)
    if (stdTypeLimit._1 != null) {
      pair.stdTypeLimit=stdTypeLimit
    }
    val genderLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.GENDER.metaId)
    if (genderLimit._1 != null) {
      pair.genderLimit=genderLimit
    }
    val departLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DEPARTMENT.metaId)
    if (departLimit._1 != null) {
      pair.departmentLimit=departLimit
    }
    val majorLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.MAJOR.metaId)
    if (majorLimit._1 != null) {
      pair.majorLimit=majorLimit
    }
    val directionLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.DIRECTION.metaId)
    if (directionLimit._1 != null) {
      pair.directionLimit=directionLimit
    }
    val adminclassLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.ADMINCLASS.metaId)
    if (adminclassLimit._1 != null) {
      pair.adminclassLimit=adminclassLimit
    }
    val educationLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.EDUCATION.metaId)
    if (educationLimit._1 != null) {
      pair.educationLimit=educationLimit
    }
    val programLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.PROGRAM.metaId)
    if (programLimit._1 != null) {
      pair.programLimit=programLimit
    }
    val normalClassLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.NORMALCLASS.metaId)
    if (normalClassLimit._1 != null) {
      pair.normalClassLimit=normalClassLimit
    }
    val stdlabelLimit = xtractLimitDirtyWork(group, CourseLimitMetaEnum.STDLABEL.metaId)
    if (stdlabelLimit._1 != null) {
      pair.stdLabelLimit=stdlabelLimit
    }
    pair
  }

  def setTeachClassNameStrategy(teachClassNameStrategy: TeachClassNameStrategy) {
    this.teachClassNameStrategy = teachClassNameStrategy
  }
}
