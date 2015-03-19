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
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.eams.teach.lesson.NormalClass
import org.openurp.edu.eams.teach.lesson.model.CourseLimitGroupBean
import org.openurp.edu.eams.teach.lesson.model.CourseLimitItemBean
import org.openurp.edu.eams.teach.lesson.model.CourseLimitMetaBean
import org.openurp.edu.eams.teach.lesson.service.CourseLimitGroupBuilder
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.base.Program



class DefaultCourseLimitGroupBuilder(val group: CourseLimitGroup) extends CourseLimitGroupBuilder() {

  var strategy: DefaultTeachClassNameStrategy = new DefaultTeachClassNameStrategy()

  def inGrades(grades: String*): CourseLimitGroupBuilder = {
    if (grades.length > 0 && grades(0) != null) {
      val item = getOrCreateItem(CourseLimitMetaEnum.GRADE.getMetaId)
      addValues(item, true, grades)
    }
    this
  }

  def notInGrades(grades: String*): CourseLimitGroupBuilder = {
    val item = getOrCreateItem(CourseLimitMetaEnum.GRADE.getMetaId)
    addValues(item, false, grades)
    this
  }

  def in[T <: Entity[_]](entities: T*): CourseLimitGroupBuilder = {
    if (entities.length > 0 && entities(0) != null) {
      val first = entities(0)
      val item = getItem(first)
      addValues(item, true, ids(entities))
    }
    if (entities.length > 0 && entities(0).isInstanceOf[Adminclass]) {
      clear(new CourseLimitMetaBean(CourseLimitMetaEnum.GRADE.getMetaId))
      clear(new CourseLimitMetaBean(CourseLimitMetaEnum.STDTYPE.getMetaId))
      clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DEPARTMENT.getMetaId))
      clear(new CourseLimitMetaBean(CourseLimitMetaEnum.MAJOR.getMetaId))
      clear(new CourseLimitMetaBean(CourseLimitMetaEnum.DIRECTION.getMetaId))
      clear(new CourseLimitMetaBean(CourseLimitMetaEnum.EDUCATION.getMetaId))
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
      inGrades(grades)
      in(stdTypes)
      in(departments)
      in(majors)
      in(directions)
      in(educations)
    }
    this
  }

  def notIn[T <: Entity[_]](entities: T*): CourseLimitGroupBuilder = {
    if (entities.length >= 0 && entities(0) != null) {
      val first = entities(0)
      val item = getItem(first)
      addValues(item, false, ids(entities))
    }
    this
  }

  def clear(meta: CourseLimitMeta): CourseLimitGroupBuilder = {
    var removed: CourseLimitItem = null
    for (item <- group.getItems if item.getMeta == meta) {
      removed = item
    }
    if (null != removed) {
      group.getItems.remove(removed)
    }
    this
  }

  def build(): CourseLimitGroup = group

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

  private def addValues(item: CourseLimitItem, contain: Boolean, values: String*) {
    var old = item.getContent
    old = if (old.length > 0) Strings.concat(old, ",", Strings.join(values, ",")) else Strings.join(values, 
      ",")
    if (-1 != old.indexOf(',')) {
      if (!old.startsWith(",")) old = "," + old
      if (!old.endsWith(",")) old = old + ","
    }
    item.setContent(old)
    if (contain) {
      if (-1 != old.indexOf(',')) {
        item.setOperator(CourseLimitMeta.Operator.IN)
      } else {
        item.setOperator(CourseLimitMeta.Operator.EQUAL)
      }
    } else {
      if (-1 != old.indexOf(',')) {
        item.setOperator(CourseLimitMeta.Operator.NOT_IN)
      } else {
        item.setOperator(CourseLimitMeta.Operator.NOT_EQUAL)
      }
    }
  }

  private def getOrCreateItem(metaId: java.lang.Long): CourseLimitItem = {
    for (item <- group.getItems if item.getMeta.id == metaId) {
      return item
    }
    val item = new CourseLimitItemBean()
    item.setMeta(new CourseLimitMetaBean(metaId))
    item.setOperator(CourseLimitMeta.Operator.EQUAL)
    item.setContent("")
    item.setGroup(group)
    group.getItems.add(item)
    item
  }

  private def getItem[T](first: T): CourseLimitItem = {
    var item: CourseLimitItem = null
    if (first.isInstanceOf[Gender]) {
      item = getOrCreateItem(CourseLimitMetaEnum.GENDER.getMetaId)
    } else if (first.isInstanceOf[StdType]) {
      item = getOrCreateItem(CourseLimitMetaEnum.STDTYPE.getMetaId)
    } else if (first.isInstanceOf[Department]) {
      item = getOrCreateItem(CourseLimitMetaEnum.DEPARTMENT.getMetaId)
    } else if (first.isInstanceOf[Major]) {
      item = getOrCreateItem(CourseLimitMetaEnum.MAJOR.getMetaId)
    } else if (first.isInstanceOf[Direction]) {
      item = getOrCreateItem(CourseLimitMetaEnum.DIRECTION.getMetaId)
    } else if (first.isInstanceOf[Adminclass]) {
      item = getOrCreateItem(CourseLimitMetaEnum.ADMINCLASS.getMetaId)
    } else if (first.isInstanceOf[Education]) {
      item = getOrCreateItem(CourseLimitMetaEnum.EDUCATION.getMetaId)
    } else if (first.isInstanceOf[Program]) {
      item = getOrCreateItem(CourseLimitMetaEnum.PROGRAM.getMetaId)
    } else if (first.isInstanceOf[NormalClass]) {
      item = getOrCreateItem(CourseLimitMetaEnum.NORMALCLASS.getMetaId)
    } else if (first.isInstanceOf[StdLabel]) {
      item = getOrCreateItem(CourseLimitMetaEnum.STDLABEL.getMetaId)
    } else {
      throw new RuntimeException("no support limit meta class " + first.getClass.getName)
    }
    item
  }
}
