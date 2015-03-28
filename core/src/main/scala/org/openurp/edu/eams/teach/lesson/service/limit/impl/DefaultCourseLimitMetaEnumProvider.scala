package org.openurp.edu.eams.teach.lesson.service.limit.impl

import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaFilter
import org.openurp.edu.teach.lesson.LessonLimitMeta.LimitMeta
import org.openurp.edu.eams.teach.lesson.service.limit.LessonLimitMetaProvider
import org.beangle.commons.collection.Collections
import org.openurp.edu.teach.lesson.LessonLimitMeta

class DefaultLessonLimitMetaProvider extends LessonLimitMetaProvider {

  var filters = Collections.newBuffer[LessonLimitMetaFilter]

  def getLessonLimitMetas(): Seq[LimitMeta] = {
    val results = Collections.newBuffer[LimitMeta]
    val iter = LessonLimitMeta.values.iterator
    while (iter.hasNext) {
      val meta = iter.next()
      val append = !filters.exists { f => !f.accept(meta) }
      if (append) results += meta
    }
    results
  }

  def getLessonLimitMetaIds(): Seq[Int] = {
    val results = Collections.newBuffer[Int]
    val iter = LessonLimitMeta.values.iterator
    while (iter.hasNext) {
      val meta = iter.next()
      val append = !filters.exists { f => !f.accept(meta) }
      if (append) results += meta.id
    }
    results
  }

  def getLessonLimitMetaPairs(): Pair[Seq[Int], Seq[LimitMeta]] = {
    val ids = Collections.newBuffer[Int]
    val metas = Collections.newBuffer[LimitMeta]
    val iter = LessonLimitMeta.values.iterator
    while (iter.hasNext) {
      val meta = iter.next()
      val append = !filters.exists { f => !f.accept(meta) }
      if (append) {
        ids += meta.id
        metas += meta
      }
    }
    new Pair[Seq[Int], Seq[LimitMeta]](ids, metas)
  }
}
