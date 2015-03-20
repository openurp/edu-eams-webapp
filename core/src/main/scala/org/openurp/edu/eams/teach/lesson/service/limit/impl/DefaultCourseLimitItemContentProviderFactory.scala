package org.openurp.edu.eams.teach.lesson.service.limit.impl



import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitItemContentProviderFactory
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum



class DefaultCourseLimitItemContentProviderFactory extends CourseLimitItemContentProviderFactory {

  private var providers: Map[CourseLimitMetaEnum, CourseLimitItemContentProvider] = new HashMap[CourseLimitMetaEnum, CourseLimitItemContentProvider]()

  def getProvider(courseLimitMetaEnum: CourseLimitMetaEnum): CourseLimitItemContentProvider = {
    val provider = providers.get(courseLimitMetaEnum)
    provider.metaEnum=courseLimitMetaEnum
    provider
  }

  def getProvider(courseLimitMeta: CourseLimitMeta): CourseLimitItemContentProvider = getProvider(courseLimitMeta.id)

  def getProvider(courseLimitMetaId: Long): CourseLimitItemContentProvider = {
    val values = CourseLimitMetaEnum.values
    for (courseLimitMetaEnum <- values if courseLimitMetaId == courseLimitMetaEnum.metaId) {
      return getProvider(courseLimitMetaEnum)
    }
    null
  }

  def setProviders(providers: Map[CourseLimitMetaEnum, CourseLimitItemContentProvider]) {
    this.providers = providers
  }
}
