package org.openurp.edu.eams.teach.program.major.web.action

import java.util.Date
import java.util.List
import java.util.Locale
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.text.seq.SeqPattern
import org.openurp.edu.eams.teach.program.Program
import org.openurp.edu.eams.teach.program.doc.ProgramDocMeta
import org.openurp.edu.eams.teach.program.doc.model.ProgramDocBean
import org.openurp.edu.eams.teach.program.doc.model.ProgramDocSectionBean
import org.openurp.edu.eams.teach.program.doc.model.ProgramDocTemplateBean
import org.openurp.edu.eams.teach.program.major.MajorPlan
import com.ekingstar.eams.web.action.common.ProjectSupportAction
//remove if not needed
import scala.collection.JavaConversions._

class ProgramDocAction extends ProjectSupportAction {

  def indexSetting() {
    val majorPlanId = getLongId("majorPlan")
    val plan = entityDao.get(classOf[MajorPlan], majorPlanId)
    val docs = entityDao.get(classOf[ProgramDocBean], "program", plan.getProgram)
    put("docs", docs)
  }

  def info(): String = {
    val majorPlanId = getLongId("majorPlan")
    val plan = entityDao.get(classOf[MajorPlan], majorPlanId)
    put("plan", plan)
    val program = plan.getProgram
    val request_locale = getLocale
    val builder = OqlBuilder.from(classOf[ProgramDocBean], "pd")
    builder.where("pd.program =:program", program)
    if (request_locale == null) {
      builder.where("pd.locale=:locale", new Locale("zh", "CN"))
    } else {
      builder.where("pd.locale=:locale", request_locale)
    }
    var seqPattern: SeqPattern = null
    seqPattern = if (request_locale == new Locale("zh", "CN")) new SeqPattern(new HanZi2SeqStyle(), "{1}") else new SeqPattern(new LuomaSeqStyle(), 
      "{1}")
    put("seqPattern", seqPattern)
    val docs = entityDao.search(builder)
    var doc: ProgramDocBean = null
    if (docs.size > 0) doc = docs.get(0)
    put("doc", doc)
    forward()
  }

  override def edit(): String = {
    val majorPlanId = getLongId("majorPlan")
    val templateId = getLongId("template")
    val plan = entityDao.get(classOf[MajorPlan], majorPlanId)
    put("plan", plan)
    val program = plan.getProgram
    val builder2 = OqlBuilder.from(classOf[ProgramDocTemplateBean], "pdt")
    builder2.where("pdt.project=:project and pdt.education=:education", program.getMajor.getProject, 
      plan.getProgram.getEducation)
    if (null != program.getStdType) {
      builder2.where(":givenType = some elements(pdt.types) or size(pdt.types)=0", program.getStdType)
    }
    builder2.where("pdt.effectiveAt <=:invalidAt and( pdt.invalidAt is null or :effetiveOn<=pdt.invalidAt)", 
      if (program.getInvalidOn == null) program.getEffectiveOn else program.getInvalidOn, program.getEffectiveOn)
    var template: ProgramDocTemplateBean = null
    val templates = entityDao.search(builder2)
    put("templates", templates)
    if (null != templateId) {
      template = entityDao.get(classOf[ProgramDocTemplateBean], templateId)
    } else if (!templates.isEmpty) {
      template = templates.get(0)
    }
    if (null != template) put("template", template)
    val builder = OqlBuilder.from(classOf[ProgramDocBean], "pd")
    builder.where("pd.program =:program", program)
    if (null != template) builder.where("pd.locale=:locale", template.getLocale)
    val docs = entityDao.search(builder)
    var doc: ProgramDocBean = null
    if (!docs.isEmpty) {
      doc = docs.get(0)
    } else {
      doc = new ProgramDocBean()
      if (template != null) {
        val sections = CollectUtils.newArrayList()
        for (meta <- template.getMetas) {
          val section = new ProgramDocSectionBean()
          section.setName(meta.getName)
          section.setCode(String.valueOf(meta.getIndexno))
          section.setDoc(doc)
          sections.add(section)
        }
        doc.setSections(sections)
      }
    }
    put("doc", doc)
    forward()
  }

  def save(): String = {
    val majorPlanId = getLongId("majorPlan")
    val plan = entityDao.get(classOf[MajorPlan], majorPlanId)
    val doc = populateEntity(classOf[ProgramDocBean], "programDoc")
    val template = getEntity(classOf[ProgramDocTemplateBean], "template")
    if (doc.isTransient) {
      doc.setProgram(plan.getProgram)
      doc.setLocale(template.getLocale)
      doc.setCreatedAt(new Date())
      doc.setUpdatedAt(new Date())
    }
    val sections = CollectUtils.newHashMap()
    for (section <- doc.getSections) {
      sections.put(section.getName, section)
    }
    var i = 1
    for (meta <- template.getMetas) {
      var section = sections.get(meta.getName)
      if (null == section) {
        section = new ProgramDocSectionBean()
        section.setDoc(doc)
        section.setName(meta.getName)
        doc.getSections.add(section)
      }
      section.setCode(String.valueOf(i))
      i += 1
      val content = get("content" + meta.getIndexno)
      section.setContent(content)
    }
    entityDao.save(doc)
    redirect("edit", "info.save.success", "&majorPlan.id=" + majorPlanId)
  }
}
