package org.openurp.eams.action

import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.webmvc.entity.action.RestfulAction
import org.openurp.base.code.DisciplineCategory
import org.openurp.edu.base.{ Major, Project }

class MajorAction extends RestfulAction[Major] {
  override def editSetting(entity: Major) = {

    val projects = findItems(classOf[Project])
    put("projects", projects)

    val categories = findItems(classOf[DisciplineCategory])
    put("categories", categories)

    //    val journals = findItems(classOf[MajorJournal])
    //    put("journals", journals)
    //
    //    val educations = findItems(classOf[Education])
    //    put("educations", educations)
    //
    //    val directions = findItems(classOf[Direction])
    //    put("directions", directions)

    super.editSetting(entity)
  }

  private def findItems[T <: Entity[_]](clazz: Class[T]): Seq[T] = {
    val query = OqlBuilder.from(clazz)
    query.orderBy("name")
    val items = entityDao.search(query)
    items
  }

  //  protected override def saveAndRedirect(entity: Major): View = {
  //    
  //    val major = entity.asInstanceOf[MajorBean]
  //  
  //    major.journals.clear()
  //    val journalsIds = getAll("journalsId2nd", classOf[Integer])
  //    major.journals ++= entityDao.find(classOf[MajorJournal], journalsIds)
  //    
  //    major.educations.clear()
  //    val educationsIds = getAll("educationsId2nd", classOf[Integer])
  //    major.educations ++= entityDao.find(classOf[Education], educationsIds)
  //    
  //    major.directions.clear()
  //    val directionsIds = getAll("directionsId2nd", classOf[Integer])
  //    major.directions ++= entityDao.find(classOf[Direction], directionsIds)
  //    
  //    super.saveAndRedirect(entity)
  //  }

}


