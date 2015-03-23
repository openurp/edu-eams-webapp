package org.openurp.edu.eams.teach.lesson.service.limit.impl


import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings

import org.openurp.edu.base.Program

class LessonLimitProgramProvider extends AbstractLessonLimitNamedEntityProvider[Program, Long] {

  protected override def addCascadeQuery(builder: OqlBuilder[Program], cascadeField: Map[Long, String]) {
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
