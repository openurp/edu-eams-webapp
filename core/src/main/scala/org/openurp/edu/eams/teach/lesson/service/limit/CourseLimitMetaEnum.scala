package org.openurp.edu.eams.teach.lesson.service.limit

import java.io.Serializable
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Department
import org.openurp.code.person.Gender
import org.openurp.edu.base.Adminclass
import org.openurp.edu.base.Direction
import org.openurp.edu.base.Major
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdLabel
import org.openurp.edu.base.code.StdType
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.NormalClass
import org.openurp.edu.base.Program
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

object CourseLimitMetaEnum extends Enumeration {

  val GRADE = new CourseLimitMetaEnum(1L, classOf[String], null, Operator.IN, Operator.NOT_IN)

  val STDTYPE = new CourseLimitMetaEnum(2L, classOf[StdType], null, Operator.IN, Operator.NOT_IN)

  val GENDER = new CourseLimitMetaEnum(3L, classOf[Gender], null, Operator.IN, Operator.NOT_IN)

  val EDUCATION = new CourseLimitMetaEnum(8L, classOf[Education], null, Operator.IN, Operator.NOT_IN)

  val DEPARTMENT = new CourseLimitMetaEnum(4L, classOf[Department], null, Operator.IN, Operator.NOT_IN)

  val MAJOR = new CourseLimitMetaEnum(5L, classOf[Major], null, Operator.IN, Operator.NOT_IN)

  val DIRECTION = new CourseLimitMetaEnum(6L, classOf[Direction], null, Operator.IN, Operator.NOT_IN)

  val ADMINCLASS = new CourseLimitMetaEnum(7L, classOf[Adminclass], null, Operator.IN, Operator.NOT_IN)

  val PROGRAM = new CourseLimitMetaEnum(9L, classOf[Program], null, Operator.IN, Operator.NOT_IN)

  val NORMALCLASS = new CourseLimitMetaEnum(10L, classOf[NormalClass], null, Operator.IN, Operator.NOT_IN)

  val STDLABEL = new CourseLimitMetaEnum(11L, classOf[StdLabel], null, Operator.IN, Operator.NOT_IN)

  class CourseLimitMetaEnum(@BeanProperty var metaId: Long, 
      clazz: Class[_ <: Serializable], 
      @BeanProperty var format: String, 
      @BeanProperty var operators: Operator*) extends Val {

    @BeanProperty
    var contentType: Class[_ <: Serializable] = clazz

    private var contentValueType: Class[_ <: Serializable] = _

    getContentValueType

    def getContentValueType(): Class[_ <: Serializable] = {
      if (null == contentValueType && null != contentType) {
        this.contentValueType = if (classOf[Entity].isAssignableFrom(contentType)) Model.getType(contentType).getIdType else contentType
      }
      contentValueType
    }
  }

  implicit def convertValue(v: Value): CourseLimitMetaEnum = v.asInstanceOf[CourseLimitMetaEnum]
}
