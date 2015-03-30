package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.io.Serializable
import org.beangle.commons.bean.PropertyUtils
import org.beangle.data.model.Entity
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.base.Adminclass
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import java.util.Date
import org.openurp.edu.base.Direction
import org.beangle.commons.collection.page.PageLimit
import org.openurp.edu.base.code.StdType
import org.openurp.edu.base.Major
import org.openurp.edu.base.Program
import org.openurp.edu.base.code.StdLabel
import org.openurp.code.edu.Education
import org.openurp.code.person.Gender
import org.beangle.commons.lang.Arrays
import scala.collection.mutable.HashSet
import scala.collection.mutable.LinkedHashMap
import org.beangle.commons.collection.Collections

class LessonLimitAdminclassProvider extends AbstractLessonLimitNamedEntityProvider[Adminclass, java.lang.Long] {

  protected override def addCascadeQuery(builder: OqlBuilder[Adminclass], cascadeField: collection.Map[java.lang.Long, String]) {
    if (cascadeField.isEmpty) {
      return
    }
    val grades = cascadeField.get(LessonLimitMeta.Grade.id).orNull
    val educationIds = cascadeField.get(LessonLimitMeta.Education.id).orNull
    val stdTypeIds = cascadeField.get(LessonLimitMeta.StdType.id).orNull
    val departIds = cascadeField.get(LessonLimitMeta.Department.id).orNull
    val majorIds = cascadeField.get(LessonLimitMeta.Major.id).orNull
    val directionIds = cascadeField.get(LessonLimitMeta.Direction.id).orNull
    if (Strings.isNotBlank(grades)) {
      builder.where("entity.grade in (:grades)", Strings.split(grades))
    }
    if (Strings.isNotBlank(educationIds)) {
      builder.where("entity.education.id in (:educationIds)", Strings.splitToInt(educationIds))
    }
    if (Strings.isNotBlank(stdTypeIds)) {
      builder.where("entity.stdType.id in (:stdTypeIds)", Strings.splitToInt(stdTypeIds))
    }
    if (Strings.isNotBlank(departIds)) {
      builder.where("entity.department.id in (:departIds)", Strings.splitToInt(departIds))
    }
    if (Strings.isNotBlank(majorIds)) {
      builder.where("entity.major.id in (:majorIds)", Strings.splitToInt(majorIds))
    }
    if (Strings.isNotBlank(directionIds)) {
      builder.where("entity.direction.id in (:directionIds)", Strings.splitToInt(directionIds))
    }
  }
}

class LessonLimitDepartmentProvider extends AbstractLessonLimitNamedEntityProvider[Department, Integer]

class LessonLimitDirectionProvider extends AbstractLessonLimitNamedEntityProvider[Direction, Integer] {

  protected override def addCascadeQuery(builder: OqlBuilder[Direction], cascadeField: collection.Map[java.lang.Long, String]) {
    if (cascadeField.isEmpty) {
      return
    }
    val majorIds = cascadeField.get(LessonLimitMeta.Major.id).orNull
    val departIds = cascadeField.get(LessonLimitMeta.Department.id).orNull
    val educationIds = cascadeField.get(LessonLimitMeta.Education.id).orNull
    if (Strings.isNotBlank(majorIds)) {
      builder.where("entity.major.id in (:majorIds)", Strings.splitToInt(majorIds))
    }
    if (Strings.isNotBlank(departIds) || Strings.isNotBlank(educationIds)) {
      val sb = new StringBuilder("exists(from entity.departs journal where journal.effectiveAt <= :now and (journal.invalidAt is null or journal.invalidAt >= :now)")
      if (Strings.isNotBlank(departIds)) {
        sb.append(" and journal.depart.id in (:departIds)")
      }
      if (Strings.isNotBlank(educationIds)) {
        sb.append(" and journal.education.id in (:educationIds)")
      }
      sb.append(")")
      builder.where(sb.toString, new Date(), if (Strings.isBlank(departIds)) null else Strings.splitToInt(departIds),
        if (Strings.isBlank(educationIds)) null else Strings.splitToInt(educationIds))
    }
  }
}

class LessonLimitEducationProvider extends AbstractLessonLimitNamedEntityProvider[Education, Integer]

class LessonLimitGenderProvider extends AbstractLessonLimitNamedEntityProvider[Gender, Integer] {

  var excludedIds = Collections.newSet[Integer]

  protected override def getContentMap(content: Array[Serializable]): collection.Map[String, Gender] = {
    val contentMap = super.getContentMap(content)
    val results = new LinkedHashMap[String, Gender]()
    for ((key, value) <- contentMap) {
      val gender = value
      val id = gender.id
      if (!excludedIds.contains(id)) {
        results.put(key, gender)
      }
    }
    results
  }
}

class LessonLimitMajorProvider extends AbstractLessonLimitNamedEntityProvider[Major, Integer] {

  protected override def addCascadeQuery(builder: OqlBuilder[Major], cascadeField: collection.Map[java.lang.Long, String]) {
    if (cascadeField.isEmpty) {
      return
    }
    val departIds = cascadeField.get(LessonLimitMeta.Department.id).orNull
    val educationIds = cascadeField.get(LessonLimitMeta.Education.id).orNull
    if (Strings.isNotBlank(departIds) || Strings.isNotBlank(educationIds)) {
      val sb = new StringBuilder("exists(from entity.journals journal where journal.effectiveAt <= :now and (journal.invalidAt is null or journal.invalidAt >= :now)")
      if (Strings.isNotBlank(departIds)) {
        sb.append(" and journal.depart.id in (:departIds)")
      }
      if (Strings.isNotBlank(educationIds)) {
        sb.append(" and journal.education.id in (:educationIds)")
      }
      sb.append(")")
      builder.where(sb.toString, new Date(), if (Strings.isBlank(departIds)) null else Strings.splitToInt(departIds),
        if (Strings.isBlank(educationIds)) null else Strings.splitToInt(educationIds))
    }
  }
}

class LessonLimitProgramProvider extends AbstractLessonLimitNamedEntityProvider[Program, java.lang.Long] {

  protected override def addCascadeQuery(builder: OqlBuilder[Program], cascadeField: collection.Map[java.lang.Long, String]) {
    if (cascadeField.isEmpty) {
      return
    }
    val grades = cascadeField.get(LessonLimitMeta.Grade.id).orNull
    val educationIds = cascadeField.get(LessonLimitMeta.Education.id).orNull
    val stdTypeIds = cascadeField.get(LessonLimitMeta.StdType.id).orNull
    val departIds = cascadeField.get(LessonLimitMeta.Department.id).orNull
    val majorIds = cascadeField.get(LessonLimitMeta.Major.id).orNull
    val directionIds = cascadeField.get(LessonLimitMeta.Direction.id).orNull
    if (Strings.isNotBlank(grades)) {
      builder.where("entity.grade in (:grades)", Strings.split(grades))
    }
    if (Strings.isNotBlank(educationIds)) {
      builder.where("entity.education.id in (:educationIds)", Strings.splitToInt(educationIds))
    }
    if (Strings.isNotBlank(stdTypeIds)) {
      builder.where("entity.stdType.id in (:stdTypeIds)", Strings.splitToInt(stdTypeIds))
    }
    if (Strings.isNotBlank(departIds)) {
      builder.where("entity.department.id in (:departIds)", Strings.splitToInt(departIds))
    }
    if (Strings.isNotBlank(majorIds)) {
      builder.where("entity.major.id in (:majorIds)", Strings.splitToInt(majorIds))
    }
    if (Strings.isNotBlank(directionIds)) {
      builder.where("entity.direction.id in (:directionIds)", Strings.splitToInt(directionIds))
    }
  }
}

class LessonLimitStdLabelProvider extends AbstractLessonLimitNamedEntityProvider[StdLabel, Integer]

class LessonLimitStdTypeProvider extends AbstractLessonLimitNamedEntityProvider[StdType, Integer]

class LessonLimitGradeProvider extends AbstractLessonLimitContentProvider[String] {

  protected def getContentMap(content: Array[Serializable]): collection.Map[String, String] = {
    val results = new collection.mutable.LinkedHashMap[String, String]()
    for (value <- content) {
      val grade = value.asInstanceOf[String]
      results.put(grade, grade)
    }
    results
  }

  protected override def getOtherContents(content: Array[Serializable], term: String, limit: PageLimit): Seq[String] = {
    val builder = OqlBuilder.from(classOf[Program].getName + " program")
    if (!Arrays.isEmpty(content)) {
      builder.where("program.grade not in(:grades)", content)
    }
    if (null != term) {
      builder.where("program.grade like :grade", "%" + term + "%")
    }
    builder.select("distinct program.grade")
    builder.orderBy("grade")
    builder.limit(limit)
    entityDao.search(builder)
  }

  def getContentIdTitleMap(content: String): collection.Map[String, String] = {
    super.getContents(content)
  }

  protected override def getCascadeContents(content: Array[Serializable],
    term: String,
    limit: PageLimit,
    cascadeField: collection.Map[java.lang.Long, String]): List[String] = null
}