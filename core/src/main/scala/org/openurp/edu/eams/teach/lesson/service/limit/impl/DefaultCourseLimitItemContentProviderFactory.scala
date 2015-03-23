package org.openurp.edu.eams.teach.lesson.service.limit.impl

import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProviderFactory


class DefaultLessonLimitItemContentProviderFactory extends LessonLimitItemContentProviderFactory {

  private var providers: Map[LessonLimitMeta, LessonLimitItemContentProvider] = new HashMap[LessonLimitMeta, LessonLimitItemContentProvider]()

  def getProvider(courseLimitMetaEnum: LessonLimitMeta): LessonLimitItemContentProvider = {
    val provider = providers.get(courseLimitMetaEnum)
    provider.metaEnum=courseLimitMetaEnum
    provider
  }

  def getProvider(courseLimitMeta: LessonLimitMeta): LessonLimitItemContentProvider = getProvider(courseLimitMeta.id)

  def getProvider(courseLimitMetaId: Long): LessonLimitItemContentProvider = {
    val values = LessonLimitMeta.values
    for (courseLimitMetaEnum <- values if courseLimitMetaId == courseLimitMetaEnum.id) {
      return getProvider(courseLimitMetaEnum)
    }
    null
  }

  def setProviders(providers: Map[LessonLimitMeta, LessonLimitItemContentProvider]) {
    this.providers = providers
  }
}
