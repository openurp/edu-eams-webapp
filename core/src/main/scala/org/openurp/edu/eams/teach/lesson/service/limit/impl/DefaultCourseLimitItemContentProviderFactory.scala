package org.openurp.edu.eams.teach.lesson.service.limit.impl

import org.beangle.commons.collection.Collections
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProvider
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitItemContentProviderFactory
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta._

class DefaultLessonLimitItemContentProviderFactory extends LessonLimitItemContentProviderFactory {

  var providers = Collections.newMap[LimitMeta, LessonLimitItemContentProvider[_]]

  def getProvider(meta: LimitMeta): LessonLimitItemContentProvider[_] = {
    providers.get(meta).orNull
  }

  def getProvider(metaId: Int): LessonLimitItemContentProvider[_] = {
    getProvider(LessonLimitMeta.apply(metaId))
  }

}
