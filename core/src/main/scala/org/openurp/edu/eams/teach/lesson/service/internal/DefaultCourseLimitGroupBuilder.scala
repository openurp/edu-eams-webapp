package org.openurp.edu.eams.teach.lesson.service.internal

import org.beangle.data.model.Entity
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.LessonLimitMeta._
import org.openurp.edu.eams.teach.lesson.service.LessonLimitGroupBuilder
import org.openurp.edu.base.Program
import org.openurp.edu.teach.lesson.model.LessonLimitItemBean

class DefaultLessonLimitGroupBuilder(val group: LessonLimitGroup) extends LessonLimitGroupBuilder() {

  var strategy: DefaultTeachClassNameStrategy = new DefaultTeachClassNameStrategy()

  def inGrades(grades: String*): LessonLimitGroupBuilder = {
    if (grades.length > 0 && grades(0) != null) {
      val item = getOrCreateItem(LessonLimitMeta.Grade.id)
      addValues(item, true, grades: _*)
    }
    this
  }

  def notInGrades(grades: String*): LessonLimitGroupBuilder = {
    val item = getOrCreateItem(LessonLimitMeta.Grade.id)
    addValues(item, false, grades: _*)
    this
  }

  def in[T <: Entity[_]](entities: T*): LessonLimitGroupBuilder = {
    if (entities.length > 0 && entities(0) != null) {
      val first = entities(0)
      val item = getItem(first)
      addValues(item, true, ids(entities: _*): _*)
    }
    if (entities.length > 0 && entities(0).isInstanceOf[Adminclass]) {
      clear(LessonLimitMeta.Grade)
      clear(LessonLimitMeta.StdType)
      clear(LessonLimitMeta.Department)
      clear(LessonLimitMeta.Major)
      clear(LessonLimitMeta.Direction)
      clear(LessonLimitMeta.Education)
      val grades = Array.ofDim[String](entities.length)
      val stdTypes = Array.ofDim[StdType](entities.length)
      val departments = Array.ofDim[Department](entities.length)
      val majors = Array.ofDim[Major](entities.length)
      val directions = Array.ofDim[Direction](entities.length)
      val educations = Array.ofDim[Education](entities.length)
      for (i <- 0 until entities.length) {
        val adminclass = entities(i).asInstanceOf[Adminclass]
        grades(i) = adminclass.grade
        stdTypes(i) = adminclass.stdType
        departments(i) = adminclass.department
        majors(i) = adminclass.major
        directions(i) = adminclass.direction
        educations(i) = adminclass.education
      }
      inGrades(grades: _*)
      in(stdTypes: _*)
      in(departments: _*)
      in(majors: _*)
      in(directions: _*)
      in(educations: _*)
    }
    this
  }

  def notIn[T <: Entity[_]](entities: T*): LessonLimitGroupBuilder = {
    if (entities.length >= 0 && entities(0) != null) {
      val first = entities(0)
      val item = getItem(first)
      addValues(item, false, ids(entities: _*): _*)
    }
    this
  }

  def clear(meta: LimitMeta): LessonLimitGroupBuilder = {
    var removed: LessonLimitItem = null
    for (item <- group.items if item.meta == meta) {
      removed = item
    }
    if (null != removed) {
      group.items -= removed
    }
    this
  }

  def build(): LessonLimitGroup = group

  private def ids(entities: Entity[_]*): Array[String] = {
    val ids = Array.ofDim[String](entities.length)
    for (i <- 0 until entities.length) {
      if (entities(i) == null) {
        //continue
      }
      ids(i) = String.valueOf(entities(i).id)
    }
    ids
  }

  private def addValues(item: LessonLimitItem, contain: Boolean, values: String*) {
    var old = item.content
    old = if (old.length > 0) Strings.concat(old, ",", Strings.join(values, ",")) else Strings.join(values,
      ",")
    if (-1 != old.indexOf(',')) {
      if (!old.startsWith(",")) old = "," + old
      if (!old.endsWith(",")) old = old + ","
    }
    item.content = old
    if (contain) {
      if (-1 != old.indexOf(',')) {
        item.operator = LessonLimitMeta.Operators.IN
      } else {
        item.operator = LessonLimitMeta.Operators.Equals
      }
    } else {
      if (-1 != old.indexOf(',')) {
        item.operator = LessonLimitMeta.Operators.NOT_IN
      } else {
        item.operator = LessonLimitMeta.Operators.NOT_EQUAL
      }
    }
  }

  private def getOrCreateItem(metaId: Int): LessonLimitItem = {
    for (item <- group.items if item.meta.id == metaId) {
      return item
    }
    val item = new LessonLimitItemBean()
    item.meta = LessonLimitMeta(metaId)
    item.operator = LessonLimitMeta.Operators.Equals
    item.content = ""
    item.group = group
    group.items += item
    item
  }

  private def getItem[T](first: T): LessonLimitItem = {
    var item: LessonLimitItem = null
    if (first.isInstanceOf[Gender]) {
      item = getOrCreateItem(LessonLimitMeta.Gender.id)
    } else if (first.isInstanceOf[StdType]) {
      item = getOrCreateItem(LessonLimitMeta.StdType.id)
    } else if (first.isInstanceOf[Department]) {
      item = getOrCreateItem(LessonLimitMeta.Department.id)
    } else if (first.isInstanceOf[Major]) {
      item = getOrCreateItem(LessonLimitMeta.Major.id)
    } else if (first.isInstanceOf[Direction]) {
      item = getOrCreateItem(LessonLimitMeta.Direction.id)
    } else if (first.isInstanceOf[Adminclass]) {
      item = getOrCreateItem(LessonLimitMeta.Adminclass.id)
    } else if (first.isInstanceOf[Education]) {
      item = getOrCreateItem(LessonLimitMeta.Education.id)
    } else if (first.isInstanceOf[Program]) {
      item = getOrCreateItem(LessonLimitMeta.Program.id)
    } else if (first.isInstanceOf[StdLabel]) {
      item = getOrCreateItem(LessonLimitMeta.StdLabel.id)
    } else {
      throw new RuntimeException("no support limit meta class " + first.getClass.getName)
    }
    item
  }
}
