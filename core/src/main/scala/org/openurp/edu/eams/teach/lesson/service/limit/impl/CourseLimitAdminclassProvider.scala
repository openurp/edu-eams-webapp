package org.openurp.edu.eams.teach.lesson.service.limit.impl


import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Adminclass
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum



class CourseLimitAdminclassProvider extends AbstractCourseLimitNamedEntityProvider[Adminclass, Integer] {

  protected override def addCascadeQuery(builder: OqlBuilder[Adminclass], cascadeField: Map[Long, String]) {
    if (cascadeField.isEmpty) {
      return
    }
    val grades = cascadeField.get(CourseLimitMetaEnum.GRADE.getMetaId)
    val educationIds = cascadeField.get(CourseLimitMetaEnum.EDUCATION.getMetaId)
    val stdTypeIds = cascadeField.get(CourseLimitMetaEnum.STDTYPE.getMetaId)
    val departIds = cascadeField.get(CourseLimitMetaEnum.DEPARTMENT.getMetaId)
    val majorIds = cascadeField.get(CourseLimitMetaEnum.MAJOR.getMetaId)
    val directionIds = cascadeField.get(CourseLimitMetaEnum.DIRECTION.getMetaId)
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
