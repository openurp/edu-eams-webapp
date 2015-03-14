package org.openurp.edu.eams.teach.election.service.context

import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.entity.Entity
import org.beangle.ems.rule.model.SimpleContext
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.election.model.Enum.ElectRuleType
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.teach.lesson.Lesson
import ElectionCourseContext._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object ElectionCourseContext {

  object Params extends Enumeration {

    val CONFLICT_LESSONS = new Params()

    val CONFLICT_COURSE_TAKES = new Params()

    val COUNT_LIMIT_CHECKER = new Params()

    val WITHDRAW_LESSON_IDS = new Params()

    val STD_CREDIT_CONSTRAINT = new Params()

    val STD_COURSE_COUNT_CONSTRAINT = new Params()

    val ALL_STD_COURSE_COUNT_CONSTRAINT = new Params()

    val STD_TOTAL_CREDIT_CONSTRAINT = new Params()

    class Params extends Val

    implicit def convertValue(v: Value): Params = v.asInstanceOf[Params]
  }
}

class ElectionCourseContext extends SimpleContext() {

  @BeanProperty
  var op: ElectRuleType = _

  @BeanProperty
  var student: Student = _

  @BeanProperty
  var state: ElectState = _

  @BeanProperty
  var courseTake: CourseTake = _

  @BeanProperty
  var toBeSaved: Set[Entity[_]] = CollectUtils.newHashSet()

  @BeanProperty
  var toBeRemoved: Set[Entity[_]] = CollectUtils.newHashSet()

  def this(student: Student, state: ElectState) {
    this()
    this.student = student
    this.state = state
  }

  def getLesson(): Lesson = courseTake.getLesson
}
