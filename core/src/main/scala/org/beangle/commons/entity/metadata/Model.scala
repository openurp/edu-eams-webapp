package org.beangle.commons.entity.metadata

import org.beangle.data.jpa.hibernate.EntityMetadataBuilder
import org.beangle.data.model.meta.EntityMetadata
import org.beangle.commons.inject.Container
import org.hibernate.SessionFactory
import org.beangle.data.model.Entity
import org.beangle.data.model.meta.EntityType

object Model {

  val meta: EntityMetadata = new EntityMetadataBuilder(Container.ROOT.getBean(classOf[SessionFactory]).toList).build()

  def newInstance[T <: Entity[_]](clazz: Class[T]): T = {
    meta.newInstance(clazz) match {
      case Some(t) => t
      case None => null.asInstanceOf[T]
    }
  }
  def newInstance[T <: Entity[ID], ID](clazz: Class[T], id: ID): T = {
    meta.newInstance(clazz, id) match {
      case Some(t) => t
      case None => null.asInstanceOf[T]
    }
  }

  def getType(entityName: String): EntityType = {
    meta.getType(entityName).orNull
  }

  def getType(clazz: Class[_]): EntityType = {
    meta.getType(clazz).orNull
  }
}