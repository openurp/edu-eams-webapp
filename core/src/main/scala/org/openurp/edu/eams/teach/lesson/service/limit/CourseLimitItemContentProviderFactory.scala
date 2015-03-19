package org.openurp.edu.eams.teach.lesson.service.limit

import org.openurp.edu.teach.lesson.CourseLimitMeta



trait CourseLimitItemContentProviderFactory {

  def getProvider(courseLimitMetaEnum: CourseLimitMetaEnum): CourseLimitItemContentProvider[_]

  def getProvider(courseLimitMeta: CourseLimitMeta): CourseLimitItemContentProvider[_]

  def getProvider(courseLimitMetaId: Long): CourseLimitItemContentProvider[_]
}
