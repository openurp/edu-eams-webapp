package org.openurp.edu.eams.teach.lesson.service.internal.filterStrategy

import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategy
import org.openurp.edu.eams.teach.lesson.service.LessonFilterStrategyFactory
import org.beangle.commons.collection.Collections
import org.openurp.edu.eams.teach.lesson.service.impl.filterStrategy.AbstractLessonFilterStrategy

class DefaultLessonFilterStrategyFactory extends LessonFilterStrategyFactory {

  var lessonFilterStrategies = Collections.newMap[String, LessonFilterStrategy]

  def getLessonFilterCategory(strategyName: String): LessonFilterStrategy = {
    lessonFilterStrategies.get(strategyName).orNull
  }
}

class LessonFilterByAdminclassStrategy extends AbstractLessonFilterStrategy("adminclass") with LessonFilterStrategy {

  override def filterString: String = {
    " inner join lesson.teachClass.courseTakes take where take.std.adminclass.id= :id "
  }
}

class LessonFilterByCourseTypeStrategy extends AbstractLessonFilterStrategy("courseType") with LessonFilterStrategy {

  override def filterString: String = " where lesson.courseType.id= :id "
}

class LessonFilterByDirectionStrategy extends AbstractLessonFilterStrategy("direction") with LessonFilterStrategy {

  override def filterString: String = {
    " where lesson.teachClass.direction.id= :id "
  }
}

class LessonFilterByMajorStrategy extends AbstractLessonFilterStrategy("major") with LessonFilterStrategy {

  override def filterString: String = {
    " where lesson.teachClass.major.id = :id "
  }
}

class LessonFilterByStdStrategy extends AbstractLessonFilterStrategy("std") with LessonFilterStrategy {

  override def filterString: String = {
    " join lesson.teachClass.courseTakes as takeInfo where (takeInfo.std.id= :id)"
  }
}

class LessonFilterByStdTypeStrategy extends AbstractLessonFilterStrategy("stdType") with LessonFilterStrategy {

  override def filterString: String = {
    " where lesson.teachClass.stdType.id= :id "
  }
}

class LessonFilterByTeachCLassDepartStrategy extends AbstractLessonFilterStrategy("depart") with LessonFilterStrategy {

  override def filterString: String = {
    " where lesson.teachClass.depart.id = :id "
  }
}

class LessonFilterByTeachDepartStrategy extends AbstractLessonFilterStrategy("teachDepart") with LessonFilterStrategy {

  override def filterString: String = " where lesson.teachDepart.id = :id "
}

class LessonFilterByTeacherStrategy extends AbstractLessonFilterStrategy("teacher") with LessonFilterStrategy {

  override def filterString: String = {
    " join lesson.teachers as teacher where teacher.id = :id "
  }
}

