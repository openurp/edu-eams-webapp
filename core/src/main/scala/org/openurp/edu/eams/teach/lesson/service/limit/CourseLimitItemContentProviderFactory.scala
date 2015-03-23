package org.openurp.edu.eams.teach.lesson.service.limit

import org.openurp.edu.teach.lesson.LessonLimitMeta



trait LessonLimitItemContentProviderFactory {

  def getProvider(courseLimitMetaEnum: LessonLimitMeta): LessonLimitItemContentProvider[_]

  def getProvider(courseLimitMeta: LessonLimitMeta): LessonLimitItemContentProvider[_]

  def getProvider(courseLimitMetaId: Long): LessonLimitItemContentProvider[_]
}
