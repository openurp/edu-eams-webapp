package org.openurp.edu.eams.teach.lesson.service.limit

import org.openurp.edu.teach.lesson.LessonLimitMeta.LimitMeta


trait LessonLimitMetaProvider {

  def getLessonLimitMetas(): Seq[LimitMeta]

  def getLessonLimitMetaIds(): Seq[Int]

  def getLessonLimitMetaPairs(): Pair[Seq[Int], Seq[LimitMeta]]
}
