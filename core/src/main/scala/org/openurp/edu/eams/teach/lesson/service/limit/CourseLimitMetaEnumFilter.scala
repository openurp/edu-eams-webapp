package org.openurp.edu.eams.teach.lesson.service.limit

import org.openurp.edu.teach.lesson.LessonLimitMeta.LimitMeta

trait LessonLimitMetaFilter {

  def accept(meta: LimitMeta): Boolean
}
