package org.openurp.edu.eams.teach.lesson.service

import org.beangle.commons.dao.query.builder.Condition
import org.beangle.commons.entity.Entity
import org.beangle.commons.lang.Strings
import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.base.Program

import scala.collection.JavaConversions._

object CourseLimitUtils {

  def build[T <: Entity[_]](entity: T, alias: String): Condition = {
    var metaId: java.lang.Long = null
    if (entity.isInstanceOf[Gender]) {
      metaId = CourseLimitMetaEnum.GENDER.getMetaId
    } else if (entity.isInstanceOf[StdType]) {
      metaId = CourseLimitMetaEnum.STDTYPE.getMetaId
    } else if (entity.isInstanceOf[Department]) {
      metaId = CourseLimitMetaEnum.DEPARTMENT.getMetaId
    } else if (entity.isInstanceOf[Major]) {
      metaId = CourseLimitMetaEnum.MAJOR.getMetaId
    } else if (entity.isInstanceOf[Direction]) {
      metaId = CourseLimitMetaEnum.DIRECTION.getMetaId
    } else if (entity.isInstanceOf[Adminclass]) {
      metaId = CourseLimitMetaEnum.ADMINCLASS.getMetaId
    } else if (entity.isInstanceOf[Education]) {
      metaId = CourseLimitMetaEnum.EDUCATION.getMetaId
    } else if (entity.isInstanceOf[Program]) {
      metaId = CourseLimitMetaEnum.PROGRAM.getMetaId
    } else {
      throw new RuntimeException("not supported limit meta class " + entity.getClass.getName)
    }
    build(metaId, alias, entity.getId.toString)
  }

  def build(metaId: java.lang.Long, alias: String, id: String): Condition = {
    var template = " alias.meta.id = :metaId and case when alias.operator='EQUAL' and alias.content=:value then 1 " + 
      "when alias.operator='IN' and instr(','||alias.content||',',:values)>0 then 1 " + 
      "when alias.operator='NOT_EQUAL' and alias.content<>:value then 1 " + 
      "when alias.operator='NOT_IN' and instr(','||alias.content||',',:values)=0 then 1 " + 
      "else 0 end = 1 "
    val paramName = "metaValue" + randomInt()
    val paramName2 = paramName + "s"
    template = Strings.replace(template, "alias", alias)
    template = Strings.replace(template, "values", paramName2)
    template = Strings.replace(template, "value", paramName)
    new Condition(template).param(metaId).param(id).param("," + id + ",")
  }

  private def randomInt(): String = {
    var d = String.valueOf(Math.random())
    d = Strings.replace(d, ".", "")
    d = d.substring(0, 8)
    d
  }
}
