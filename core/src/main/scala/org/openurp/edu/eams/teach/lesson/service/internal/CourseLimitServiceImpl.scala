package org.openurp.edu.eams.teach.lesson.service.internal

import java.util.Arrays
import java.util.Comparator
import java.util.TreeSet
import org.beangle.commons.collection.Collections
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
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators._
import org.openurp.edu.teach.lesson.LessonLimitMeta.Operators
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.TeachClass
import org.openurp.edu.eams.teach.lesson.model.LessonLimitGroupPair
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.eams.teach.lesson.service.LessonLimitGroupBuilder
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.teach.lesson.service.TeachClassNameStrategy
import org.openurp.edu.base.Program
import org.openurp.edu.teach.code.model.ElectionModeBean
import org.openurp.edu.teach.code.model.CourseTakeTypeBean
import org.openurp.edu.teach.lesson.model.LessonLimitGroupBean

class LessonLimitServiceImpl extends BaseServiceImpl with LessonLimitService {

  var teachClassNameStrategy: TeachClassNameStrategy = _

  @Deprecated
  def mergeAll(target: TeachClass, source: TeachClass) {
  }

  @Deprecated
  def merge(mergeType: java.lang.Long, target: TeachClass, source: TeachClass) {
    if (LessonLimitMeta.Adminclass.id == mergeType) {
      val tmp_collection = Collections.newSet[Adminclass]
      val targetCollection = extractAdminclasses(target)
      if (Collections.isNotEmpty(targetCollection)) {
        tmp_collection ++= targetCollection
      }
      val sourceCollection = extractAdminclasses(source)
      if (Collections.isNotEmpty(sourceCollection)) {
        tmp_collection ++= sourceCollection
      }
      limitTeachClass(Operators.IN, target, tmp_collection.toArray: _*)
    } else if (LessonLimitMeta.Department.id == mergeType) {
      val tmp_collection = Collections.newSet[Department]
      val targetCollection = extractAttendDeparts(target)
      if (Collections.isNotEmpty(targetCollection)) {
        tmp_collection ++= targetCollection
      }
      val sourceCollection = extractAttendDeparts(source)
      if (Collections.isNotEmpty(sourceCollection)) {
        tmp_collection ++= sourceCollection
      }
      limitTeachClass(Operators.IN, target, tmp_collection.toArray: _*)
    } else if (LessonLimitMeta.StdType.id == mergeType) {
      val tmp_collection = Collections.newSet[StdType]
      val targetCollection = extractStdTypes(target)
      if (Collections.isNotEmpty(targetCollection)) {
        tmp_collection ++= targetCollection
      }
      val sourceCollection = extractStdTypes(source)
      if (Collections.isNotEmpty(sourceCollection)) {
        tmp_collection ++= sourceCollection
      }
      limitTeachClass(Operators.IN, target, tmp_collection.toArray: _*)
    } else if (LessonLimitMeta.Direction.id == mergeType) {
      val tmp_collection = Collections.newSet[Direction]
      val targetCollection = extractDirections(target)
      if (Collections.isNotEmpty(targetCollection)) {
        tmp_collection ++= targetCollection
      }
      val sourceCollection = extractDirections(source)
      if (Collections.isNotEmpty(sourceCollection)) {
        tmp_collection ++= sourceCollection
      }
      limitTeachClass(Operators.IN, target, tmp_collection.toArray: _*)
    } else if (LessonLimitMeta.Gender.id == mergeType) {
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
        limitTeachClass(Operators.IN, target, tmp_gender)
      }
    } else if (LessonLimitMeta.Grade.id == mergeType) {
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
        limitTeachClass(Operators.IN, target, grades: _*)
      }
    } else if (LessonLimitMeta.Major.id == mergeType) {
      val tmp_collection = Collections.newSet[Major]
      val targetCollection = extractMajors(target)
      if (Collections.isNotEmpty(targetCollection)) {
        tmp_collection ++= targetCollection
      }
      val sourceCollection = extractMajors(source)
      if (Collections.isNotEmpty(sourceCollection)) {
        tmp_collection ++= sourceCollection
      }
      limitTeachClass(Operators.IN, target, tmp_collection.toArray: _*)
    } else if (LessonLimitMeta.Education.id == mergeType) {
      val tmp_collection = Collections.newSet[Education]
      val targetCollection = extractEducations(target)
      if (Collections.isNotEmpty(targetCollection)) {
        tmp_collection ++= targetCollection
      }
      val sourceCollection = extractEducations(source)
      if (Collections.isNotEmpty(sourceCollection)) {
        tmp_collection ++= sourceCollection
      }
      limitTeachClass(Operators.IN, target, tmp_collection.toArray: _*)
    } else if (LessonLimitMeta.Program.id == mergeType) {
      val tmp_collection = Collections.newSet[Program]
      val targetCollection = extractPrograms(target)
      if (Collections.isNotEmpty(targetCollection)) {
        tmp_collection ++= targetCollection
      }
      val sourceCollection = extractPrograms(source)
      if (Collections.isNotEmpty(sourceCollection)) {
        tmp_collection ++= sourceCollection
      }
      limitTeachClass(Operators.IN, target, tmp_collection.toArray: _*)
    } else {
      throw new RuntimeException("unsupported limit meta merge")
    }
  }

  def extractEducations(teachClass: TeachClass): Seq[Education] = {
    val res = xtractEducationLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Education]
  }

  def extractEducations(group: LessonLimitGroup): Seq[Education] = {
    val res = xtractEducationLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Education]
  }

  def extractPrograms(teachClass: TeachClass): Seq[Program] = {
    val res = xtractProgramLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Program]
  }

  def extractPrograms(group: LessonLimitGroup): Seq[Program] = {
    val res = xtractProgramLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Program]
  }

  def extractAdminclasses(teachClass: TeachClass): Seq[Adminclass] = {
    val res = xtractAdminclassLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Adminclass]
  }

  def extractAdminclasses(group: LessonLimitGroup): Seq[Adminclass] = {
    val res = xtractAdminclassLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Adminclass]
  }

  def extractGrade(teachClass: TeachClass): String = {
    val res = xtractGradeLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      if (Collections.isNotEmpty(res._2)) {
        return res._2(0)
      }
    }
    null
  }

  def extractGrade(group: LessonLimitGroup): String = {
    val res = xtractGradeLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      if (Collections.isNotEmpty(res._2)) {
        return res._2(0)
      }
    }
    null
  }

  def extractStdTypes(teachClass: TeachClass): Seq[StdType] = {
    val res = xtractStdTypeLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[StdType]
  }

  def extractStdTypes(group: LessonLimitGroup): Seq[StdType] = {
    val res = xtractStdTypeLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[StdType]
  }

  def extractMajors(teachClass: TeachClass): Seq[Major] = {
    val res = xtractMajorLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Major]
  }

  def extractMajors(group: LessonLimitGroup): Seq[Major] = {
    val res = xtractMajorLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Major]
  }

  def extractDirections(teachClass: TeachClass): Seq[Direction] = {
    val res = xtractDirectionLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Direction]
  }

  def extractDirections(group: LessonLimitGroup): Seq[Direction] = {
    val res = xtractDirectionLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Direction]
  }

  def extractAttendDeparts(teachClass: TeachClass): Seq[Department] = {
    val res = xtractAttendDepartLimit(teachClass)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Department]
  }

  def extractAttendDeparts(group: LessonLimitGroup): Seq[Department] = {
    val res = xtractAttendDepartLimit(group)
    if (Operators.IN == res._1 || Operators.Equals == res._1) {
      return res._2
    }
    Collections.newBuffer[Department]
  }

  def extractGender(teachClass: TeachClass): Gender = {
    val groups = teachClass.limitGroups
    for (group <- groups) {
      if (group.forClass) {
        for (
          item <- group.items if LessonLimitMeta.Gender.id == item.meta.id &&
            (Operators.IN == item.operator || Operators.Equals == item.operator)
        ) {
          return entityDao.find(classOf[Gender], Strings.splitToInteger(item.content))(0)
        }
      }
    }
    null
  }

  def extractGender(group: LessonLimitGroup): Gender = {
    if (group == null) {
      return null
    }
    for (
      item <- group.items if LessonLimitMeta.Gender.id == item.meta.id &&
        (Operators.IN == item.operator || Operators.Equals == item.operator)
    ) {
      return entityDao.find(classOf[Gender], Strings.splitToInteger(item.content))(0)
    }
    null
  }

  override def builder(group: LessonLimitGroup): LessonLimitGroupBuilder = {
    new DefaultLessonLimitGroupBuilder(group)
  }

  private def getOrCreateDefaultLimitGroup(teachClass: TeachClass): LessonLimitGroup = {
    teachClass.limitGroups find (g => g.forClass) match {
      case Some(g) => g
      case None =>
        val g = new LessonLimitGroupBean
        g.lesson = teachClass.lesson
        g.forClass = true
        teachClass.limitGroups += g
        g
    }
  }
  override def builder(teachClass: TeachClass): LessonLimitGroupBuilder = {
    val clb = teachClass.asInstanceOf[TeachClassBean]
    val group = getOrCreateDefaultLimitGroup(clb)
    new DefaultLessonLimitGroupBuilder(group)
  }

  def extractLonelyTakes(teachClass: TeachClass): collection.Set[CourseTake] = {
    val takes = teachClass.courseTakes
    val adminclasses = extractAdminclasses(teachClass)
    val lonelyTakes = Collections.newSet[CourseTake]
    for (take <- takes) {
      val lonely = !adminclasses.exists { a => a == take.std.adminclass }
      if (lonely)
        lonelyTakes += take
    }
    lonelyTakes
  }

  def extractPossibleCourseTakes(teachClass: TeachClass): collection.Set[CourseTake] = {
    val possibleTakes = Collections.newSet[CourseTake]
    if (Collections.isNotEmpty(teachClass.courseTakes)) {
      possibleTakes ++= teachClass.courseTakes
      return possibleTakes
    }

    val adminclasses = extractAdminclasses(teachClass)
    val eleMode = new ElectionModeBean
    eleMode.id = ElectionMode.ASSIGEND
    for (adminclass <- adminclasses; std <- adminclass.students) {
      val take = Model.newInstance(classOf[CourseTake])
      take.lesson = teachClass.lesson
      take.std = std
      val takeType = new CourseTakeTypeBean
      takeType.id = CourseTakeType.NORMAL
      take.courseTakeType = takeType
      take.electionMode = eleMode
      possibleTakes.add(take)
    }
    possibleTakes
  }

  def limitTeachClass(operator: Operator, teachClass: TeachClass, grades: String*) {
    if (grades.length > 0) {
      val clb = teachClass.asInstanceOf[TeachClassBean]
      val group = getOrCreateDefaultLimitGroup(clb)
      val bdr = builder(group)
      bdr.clear(LessonLimitMeta.Grade)
      operator match {
        case IN => bdr.inGrades(grades: _*)
        case Equals => bdr.inGrades(grades: _*)
        case NOT_IN => bdr.notInGrades(grades: _*)
        case _ => throw new RuntimeException("not supported LessonLimitItem operator")
      }
    }
  }

  def limitTeachClass[T <: Entity[_]](operator: Operator, teachClass: TeachClass, entities: T*) {
    if (entities.length > 0) {
      val clb = teachClass.asInstanceOf[TeachClassBean]
      val group = getOrCreateDefaultLimitGroup(clb)
      val bdr = builder(group)
      val first = entities(0)
      if (first.isInstanceOf[Adminclass]) {
        bdr.clear(LessonLimitMeta.Adminclass)
      } else if (first.isInstanceOf[StdType]) {
        bdr.clear(LessonLimitMeta.StdType)
      } else if (first.isInstanceOf[Major]) {
        bdr.clear(LessonLimitMeta.Major)
      } else if (first.isInstanceOf[Direction]) {
        bdr.clear(LessonLimitMeta.Direction)
      } else if (first.isInstanceOf[Department]) {
        bdr.clear(LessonLimitMeta.Department)
      } else if (first.isInstanceOf[Education]) {
        bdr.clear(LessonLimitMeta.Education)
      } else if (first.isInstanceOf[Program]) {
        bdr.clear(LessonLimitMeta.Program)
      } else if (first.isInstanceOf[Gender]) {
        bdr.clear(LessonLimitMeta.Gender)
      } else {
        throw new RuntimeException("not supported limit meta class " + first.getClass.getName)
      }
      operator match {
        case IN => bdr.in(entities: _*)
        case Equals => bdr.in(entities: _*)
        case NOT_IN => bdr.notIn(entities: _*)
        case _ => throw new RuntimeException("not supported LessonLimitItem operator")
      }
    }
  }

  private def xtractLimitDirtyWork(group: LessonLimitGroup, limitMetaId: Int): Pair[Operator, Seq[_]] = {
    if (group == null) {
      return new Pair[Operator, Seq[_]](null.asInstanceOf[Operator], Collections.newBuffer[Any])
    }
    for (item <- group.items) {
      if (LessonLimitMeta.Education.id == limitMetaId) {
        if (LessonLimitMeta.Education.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[Education], Strings.splitToInteger(item.content)))
        }
      } else if (LessonLimitMeta.Adminclass.id == limitMetaId) {
        if (LessonLimitMeta.Adminclass.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[Adminclass],
            Strings.splitToLong(item.content)))
        }
      } else if (LessonLimitMeta.Department.id == limitMetaId) {
        if (LessonLimitMeta.Department.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[Department],
            Strings.splitToInteger(item.content)))
        }
      } else if (LessonLimitMeta.Major.id == limitMetaId) {
        if (LessonLimitMeta.Major.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[Major],
            Strings.splitToInteger(item.content)))
        }
      } else if (LessonLimitMeta.Direction.id == limitMetaId) {
        if (LessonLimitMeta.Direction.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[Direction], Strings.splitToInteger(item.content)))
        }
      } else if (LessonLimitMeta.Program.id == limitMetaId) {
        if (LessonLimitMeta.Program.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[Program], Strings.splitToLong(item.content)))
        }
      } else if (LessonLimitMeta.StdType.id == limitMetaId) {
        if (LessonLimitMeta.StdType.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[StdType], Strings.splitToInteger(item.content)))
        }
      } else if (LessonLimitMeta.StdLabel.id == limitMetaId) {
        if (LessonLimitMeta.StdLabel.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[StdLabel], Strings.splitToInteger(item.content)))
        }
      } else if (LessonLimitMeta.Grade.id == limitMetaId) {
        if (LessonLimitMeta.Grade.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, Collections.newBuffer(Strings.split(item.content): _*))
        }
      } else if (LessonLimitMeta.Gender.id == limitMetaId) {
        if (LessonLimitMeta.Gender.id == item.meta.id) {
          return new Pair[Operator, Seq[_]](item.operator, entityDao.find(classOf[Gender],
            Strings.splitToInteger(item.content)))
        }
      }
    }
    new Pair[Operator, Seq[_]](null, Collections.newBuffer[Any])
  }

  private def xtractLimitDirtyWork(teachClass: TeachClass, limitMetaId: Int): Pair[Operator, Seq[_]] = {
    val groups = teachClass.limitGroups
    for (group <- groups) {
      if (group.forClass) {
        return xtractLimitDirtyWork(group, limitMetaId)
      }
    }
    new Pair[Operator, Seq[_]](null, Collections.newBuffer[Any])
  }

  def xtractEducationLimit(teachClass: TeachClass): Pair[Operator, Seq[Education]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.Education.id)
    new Pair[Operator, Seq[Education]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Education]])
  }

  def xtractEducationLimit(group: LessonLimitGroup): Pair[Operator, Seq[Education]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.Education.id)
    new Pair[Operator, Seq[Education]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Education]])
  }

  def xtractAdminclassLimit(teachClass: TeachClass): Pair[Operator, Seq[Adminclass]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.Adminclass.id)
    new Pair[Operator, Seq[Adminclass]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Adminclass]])
  }

  def xtractAdminclassLimit(group: LessonLimitGroup): Pair[Operator, Seq[Adminclass]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.Adminclass.id)
    new Pair[Operator, Seq[Adminclass]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Adminclass]])
  }

  def xtractAttendDepartLimit(teachClass: TeachClass): Pair[Operator, Seq[Department]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.Department.id)
    new Pair[Operator, Seq[Department]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Department]])
  }

  def xtractAttendDepartLimit(group: LessonLimitGroup): Pair[Operator, Seq[Department]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.Department.id)
    new Pair[Operator, Seq[Department]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Department]])
  }

  def xtractDirectionLimit(teachClass: TeachClass): Pair[Operator, Seq[Direction]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.Direction.id)
    new Pair[Operator, Seq[Direction]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Direction]])
  }

  def xtractDirectionLimit(group: LessonLimitGroup): Pair[Operator, Seq[Direction]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.Direction.id)
    new Pair[Operator, Seq[Direction]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Direction]])
  }

  def xtractProgramLimit(teachClass: TeachClass): Pair[Operator, Seq[Program]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.Program.id)
    new Pair[Operator, Seq[Program]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Program]])
  }

  def xtractProgramLimit(group: LessonLimitGroup): Pair[Operator, Seq[Program]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.Program.id)
    new Pair[Operator, Seq[Program]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Program]])
  }

  def xtractGradeLimit(teachClass: TeachClass): Pair[Operator, Seq[String]] = {
    val groups = teachClass.limitGroups
    for (group <- groups) {
      if (group.forClass) {
        return xtractGradeLimit(group)
      }
    }
    new Pair[Operator, Seq[String]](null, Collections.newBuffer[String])
  }

  def xtractGradeLimit(group: LessonLimitGroup): Pair[Operator, Seq[String]] = {
    group.items.find(LessonLimitMeta.Grade.id == _.meta.id)
      .map(item => new Pair[Operator, Seq[String]](item.operator, Collections.newBuffer[String](item.content)))
      .getOrElse(new Pair[Operator, Seq[String]](null, Collections.newBuffer[String]))
  }

  def xtractMajorLimit(teachClass: TeachClass): Pair[Operator, Seq[Major]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.Major.id)
    new Pair[Operator, Seq[Major]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Major]])
  }

  def xtractMajorLimit(group: LessonLimitGroup): Pair[Operator, Seq[Major]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.Major.id)
    new Pair[Operator, Seq[Major]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[Major]])
  }

  def xtractStdTypeLimit(teachClass: TeachClass): Pair[Operator, Seq[StdType]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.StdType.id)
    new Pair[Operator, Seq[StdType]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[StdType]])
  }

  def xtractStdTypeLimit(group: LessonLimitGroup): Pair[Operator, Seq[StdType]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.StdType.id)
    new Pair[Operator, Seq[StdType]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[StdType]])
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

  def xtractStdLabelLimit(teachClass: TeachClass): Pair[Operator, Seq[StdLabel]] = {
    val tmpRes = xtractLimitDirtyWork(teachClass, LessonLimitMeta.StdLabel.id)
    new Pair[Operator, Seq[StdLabel]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[StdLabel]])
  }

  def xtractStdLabelLimit(group: LessonLimitGroup): Pair[Operator, Seq[StdLabel]] = {
    val tmpRes = xtractLimitDirtyWork(group, LessonLimitMeta.StdLabel.id)
    new Pair[Operator, Seq[StdLabel]](tmpRes._1, tmpRes._2.asInstanceOf[Seq[StdLabel]])
  }

  def xtractLimitGroup(group: LessonLimitGroup): LessonLimitGroupPair = {
    val pair = new LessonLimitGroupPair(group)
    val gradeLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Grade.id)
    if (gradeLimit._1 != null) {
      pair.gradeLimit = gradeLimit
    }
    val stdTypeLimit = xtractLimitDirtyWork(group, LessonLimitMeta.StdType.id)
    if (stdTypeLimit._1 != null) {
      pair.stdTypeLimit = stdTypeLimit
    }
    val genderLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Gender.id)
    if (genderLimit._1 != null) {
      pair.genderLimit = genderLimit
    }
    val departLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Department.id)
    if (departLimit._1 != null) {
      pair.departmentLimit = departLimit
    }
    val majorLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Major.id)
    if (majorLimit._1 != null) {
      pair.majorLimit = majorLimit
    }
    val directionLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Direction.id)
    if (directionLimit._1 != null) {
      pair.directionLimit = directionLimit
    }
    val adminclassLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Adminclass.id)
    if (adminclassLimit._1 != null) {
      pair.adminclassLimit = adminclassLimit
    }
    val educationLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Education.id)
    if (educationLimit._1 != null) {
      pair.educationLimit = educationLimit
    }
    val programLimit = xtractLimitDirtyWork(group, LessonLimitMeta.Program.id)
    if (programLimit._1 != null) {
      pair.programLimit = programLimit
    }
    val stdlabelLimit = xtractLimitDirtyWork(group, LessonLimitMeta.StdLabel.id)
    if (stdlabelLimit._1 != null) {
      pair.stdLabelLimit = stdlabelLimit
    }
    pair
  }

}
