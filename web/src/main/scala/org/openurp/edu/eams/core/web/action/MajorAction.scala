package org.openurp.edu.eams.core.web.action

import java.util.Date



import javax.persistence.EntityExistsException
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.TransferListener
import org.beangle.commons.transfer.importer.listener.ImporterForeignerListener
import org.beangle.struts2.helper.Params
import org.springframework.dao.DataIntegrityViolationException
import org.openurp.edu.base.Major
import org.openurp.edu.base.MajorJournal
import org.openurp.code.edu.Education
import org.openurp.edu.eams.core.code.ministry.DisciplineCategory
import org.openurp.edu.eams.core.model.MajorJournalBean
import org.openurp.edu.eams.core.service.listener.MajorImportListener



class MajorAction extends MajorSearchAction {

  def edit(): String = {
    val major = getEntity(classOf[Major], "major")
    if (major.isTransient) {
      major.setProject(getProject)
    }
    builderMajorParamsForPage(major)
    forward()
  }

  protected def builderMajorParamsForPage(major: Major) {
    put("major", major)
    put("educations", getEducations)
    put("categories", baseCodeService.getCodes(classOf[DisciplineCategory]))
  }

  def journals(): String = {
    val builder = OqlBuilder.from(classOf[MajorJournal], "journal")
    populateConditions(builder)
    var orderBy = get(Order.ORDER_STR)
    if (Strings.isEmpty(orderBy)) orderBy = "journal.effectiveAt desc"
    builder.orderBy(orderBy)
    put("journals", entityDao.search(builder))
    forward()
  }

  def saveJournal(): String = {
    val journal = populateEntity(classOf[MajorJournal], "journal")
    val major = entityDao.get(classOf[Major], journal.major.id)
    if (journal.isTransient) {
      major.getJournals.add(journal)
    }
    entityDao.save(journal)
    calcEffective(major)
    redirect("journals", "info.save.success")
  }

  private def calcEffective(major: Major) {
    var effectiveAt: Date = null
    var invalidAt = java.sql.Date.valueOf("1970-01-01")
    for (mj <- major.getJournals) {
      if (null == effectiveAt || mj.getEffectiveAt.before(effectiveAt)) effectiveAt = mj.getEffectiveAt
      if (null == mj.getInvalidAt) {
        invalidAt = null
      } else if (null != invalidAt && mj.getInvalidAt.after(invalidAt)) {
        invalidAt = mj.getInvalidAt
      }
    }
    if (effectiveAt != null) {
      major.setEffectiveAt(effectiveAt)
      major.setInvalidAt(invalidAt)
    }
    entityDao.saveOrUpdate(major)
  }

  def removeJournal(): String = {
    val journalIds = getIntIds("journal")
    val journals = entityDao.get(classOf[MajorJournal], journalIds)
    val major = journals.get(0).major
    major.getJournals.removeAll(journals)
    entityDao.remove(journals)
    entityDao.refresh(major)
    calcEffective(major)
    redirect("journals", "info.remove.success")
  }

  def editJournal(): String = {
    val journalId = getInt("journal.id")
    val majorId = getInt("journal.major.id")
    var journal: MajorJournal = null
    if (null != journalId) {
      journal = entityDao.get(classOf[MajorJournal], journalId)
    } else {
      journal = new MajorJournalBean()
      journal.setMajor(entityDao.get(classOf[Major], majorId))
    }
    put("journal", journal)
    put("educations", getEducations)
    put("departments", getDeparts)
    put("categories", baseCodeService.getCodes(classOf[DisciplineCategory]))
    forward()
  }

  def save(): String = {
    val majorId = getIntId("major")
    val majorCode = get("major.code")
    val duplicateQuery = OqlBuilder.from(classOf[Major], "major")
    duplicateQuery.where("major.project = :project", getProject)
      .where("major.code = :code", majorCode)
    if (majorId != null) {
      duplicateQuery.where("major.id <> :majorId", majorId)
    }
    val majors = entityDao.search(duplicateQuery)
    if (Collections.isNotEmpty(majors)) {
      builderMajorParamsForPage(populateEntity(classOf[Major], "major"))
      addError(getText("error.code.existed"))
      return "edit"
    }
    var major: Major = null
    val majorParams = Params.sub("major")
    try {
      val now = new Date()
      if (null == majorId) {
        major = Model.newInstance(classOf[Major])
        populate(major, majorParams)
        logHelper.info("Create a major with name:" + major.getName)
        major.setCreatedAt(now)
        major.setUpdatedAt(now)
      } else {
        major = entityDao.get(classOf[Major], majorId)
        logHelper.info("Update a major with name:" + major.getName)
        populate(major, majorParams)
        major.setUpdatedAt(now)
      }
      val educationIds = Strings.splitToInt(get("fake.education.ids"))
      major.educations.clear()
      if (educationIds.length > 0) {
        major.educations.addAll(entityDao.get(classOf[Education], educationIds))
      }
      fillMajorDeparts(major)
      saveOrUpdate(major)
    } catch {
      case e: EntityExistsException => {
        logHelper.info("Failure save or update a major with name:" + major.getName, e)
        return forwardError(Array("entity.major", "error.model.existed"))
      }
      case e: Exception => {
        logHelper.info("Failure save orreturn update a major with name:" + major.getName, e)
        return forwardError("error.occurred")
      }
    }
    redirect("search", "info.save.success")
  }

  private def fillMajorDeparts(major: Major): Boolean = {
    val count = getInt("mdcount")
    if (null == count || count == 0) {
      return false
    }
    major.getJournals.clear()
    entityDao.save(major)
    val mdCahe = Collections.newSet[Any]
    var i = 0
    while (i <= count) {
      val md = populate(classOf[MajorJournal], "md" + i)
      if (md.education == null || md.education.id == null || 
        md.getDepart == null || 
        md.getDepart.id == null || 
        md.getEffectiveAt == null || 
        mdCahe.contains((md.education.id + "" + md.getDepart.id).hashCode)) {
        //continue
      }
      md.setMajor(major)
      major.getJournals.add(md)
      mdCahe.add((md.education.id + "" + md.getDepart.id).hashCode)
      i += 1
    }
    !major.getJournals.isEmpty
  }

  def remove(): String = {
    try {
      entityDao.remove(entityDao.get(classOf[Major], getIntIds("major")))
    } catch {
      case e: DataIntegrityViolationException => {
        logger.error(e.getMessage)
        addError(getText("error.remove.beenUsed"))
        put("majors", entityDao.search(buildMajorQuery()))
        return "search"
      }
    }
    redirect("search", "info.action.success")
  }

  protected def getImporterListeners(): List[TransferListener] = {
    val listeners = Collections.newBuffer[Any]
    listeners.add(new ImporterForeignerListener(entityDao))
    listeners.add(new MajorImportListener(entityDao))
    listeners
  }
}
