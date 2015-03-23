package org.openurp.edu.eams.teach.lesson.service.limit.impl

import java.util.Date

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.base.Direction




class LessonLimitDirectionProvider extends AbstractLessonLimitNamedEntityProvider[Direction, Integer] {

  protected override def addCascadeQuery(builder: OqlBuilder[Direction], cascadeField: Map[Long, String]) {
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
