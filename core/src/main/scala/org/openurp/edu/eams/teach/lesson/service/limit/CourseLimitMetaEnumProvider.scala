package org.openurp.edu.eams.teach.lesson.service.limit

import org.openurp.edu.teach.lesson.LessonLimitMeta.LimitMeta


trait LessonLimitMetaProvider {

  def getLessonLimitMetas(): List[LimitMeta]

  def getLessonLimitMetaIds(): List[Int]

  def getLessonLimitMetaPairs(): Pair[List[Int], List[LimitMeta]]
}
