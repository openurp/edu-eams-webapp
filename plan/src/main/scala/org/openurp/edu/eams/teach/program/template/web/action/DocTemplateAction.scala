package org.openurp.edu.eams.teach.program.template.web.action


import java.util.Date


import java.util.Locale

import javax.servlet.http.HttpServletRequest
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.struts2.convention.route.Action
import com.ekingstar.eams.core.Project
import com.ekingstar.eams.core.code.school.StdType
import org.openurp.edu.eams.teach.program.doc.ProgramDocMeta
import org.openurp.edu.eams.teach.program.doc.ProgramDocTemplate
import org.openurp.edu.eams.teach.program.doc.model.ProgramDocMetaBean
import org.openurp.edu.eams.teach.program.doc.model.ProgramDocTemplateBean
import com.ekingstar.eams.web.action.common.ProjectSupportAction
//remove if not needed


class DocTemplateAction extends ProjectSupportAction {

  def index(): String = forward()

  override def search(): String = {
    val project = getProject
    val builder = OqlBuilder.from(classOf[ProgramDocTemplate], "template")
    builder.where("template.project =:project", project)
    builder.limit(getPageLimit)
    builder.orderBy(Order.parse(get(Order.ORDER_STR)))
    put("programDocTemplates", entityDao.search(builder))
    forward()
  }

  def edit(): String = {
    put("programDocTemplate", getEntity(classOf[ProgramDocTemplateBean], "template"))
    val locales = Array(new Locale("zh", "CN"), new Locale("en", "US"))
    val remark = Array("中文", "英文")
    val maps = new HashMap[Locale, String]()
    maps.put(locales(0), remark(0))
    maps.put(locales(1), remark(1))
    put("maps", maps)
    put("stdTypes", getStdTypes)
    put("educations", getEducations)
    put("project", getProject)
    forward()
  }

  def remove(): String = {
    try {
      val entityIds = getLongIds("template")
      val programDocTemplateBeans = entityDao.get(classOf[ProgramDocTemplateBean], entityIds)
      entityDao.remove(programDocTemplateBeans)
      redirect("search", "info.delete.success")
    } catch {
      case e: Exception => {
        e.printStackTrace()
        redirect("search", "info.delete.failure")
      }
    }
  }

  def info(): String = {
    val templateId = getLong("templateId")
    put("programDocTemplate", entityDao.get(classOf[ProgramDocTemplateBean], templateId))
    forward()
  }

  def save(): String = {
    try {
      val request = getRequest
      val strIds = request.getParameterValues("metaId")
      val names = request.getParameterValues("metaName")
      val lengths = request.getParameterValues("metaLength")
      val indexnos = request.getParameterValues("metaIndexno")
      val strLocale = request.getParameter("programDocTemplate.locale")
      val locale = new Locale(strLocale)
      val stdTypeIds = getAll("stdType.ids", classOf[Integer])
      val template = populateEntity(classOf[ProgramDocTemplateBean], "programDocTemplate")
      template.setLocale(locale)
      template.getTypes.clear()
      if (null != stdTypeIds && stdTypeIds.length > 0) {
        template.getTypes.addAll(entityDao.get(classOf[StdType], stdTypeIds))
      }
      if (null == template.getCreatedAt) template.setCreatedAt(new Date())
      template.setUpdatedAt(new Date())
      if (strIds == null || names == null || lengths == null || indexnos == null) {
        template.getMetas.clear()
        entityDao.saveOrUpdate(template)
        return redirect("search")
      }
      val ids = new ArrayList[Long]()
      for (i <- 0 until strIds.length) {
        if ("" == strIds(i).trim()) {
          ids.add(new java.lang.Long(-1))
        } else {
          ids.add(java.lang.Long.parseLong(strIds(i)))
        }
      }
      val docMetaBeans = template.getMetas
      for (i <- 0 until docMetaBeans.size; j <- 0 until ids.size) {
        if (ids.get(j) == docMetaBeans.get(i).id) {
          docMetaBeans.get(i).setName(names(j))
          docMetaBeans.get(i).setMaxlength(java.lang.Integer.parseInt(lengths(j)))
          docMetaBeans.get(i).setIndexno(java.lang.Integer.parseInt(indexnos(j)))
          //break
        }
        if (j >= ids.size - 1) {
          docMetaBeans.remove(i)
          i -= 1
        }
      }
      entityDao.saveOrUpdate(template)
      if (docMetaBeans.size <= 0) {
        for (i <- 0 until ids.size) {
          val docMetaBean = new ProgramDocMetaBean()
          docMetaBean.setName(names(i))
          docMetaBean.setMaxlength(java.lang.Integer.parseInt(lengths(i)))
          docMetaBean.setIndexno(java.lang.Integer.parseInt(indexnos(i)))
          docMetaBeans.add(docMetaBean)
        }
      } else {
        val len = docMetaBeans.size
        for (i <- 0 until ids.size; j <- 0 until len) {
          if (ids.get(i) == docMetaBeans.get(j).id) {
            //break
          }
          if (j >= len - 1) {
            val docMetaBean = new ProgramDocMetaBean()
            docMetaBean.setName(names(i))
            docMetaBean.setMaxlength(java.lang.Integer.parseInt(lengths(i)))
            docMetaBean.setIndexno(java.lang.Integer.parseInt(indexnos(i)))
            docMetaBeans.add(docMetaBean)
          }
        }
      }
      if (template.id != null) {
        for (i <- 0 until docMetaBeans.size) {
          docMetaBeans.get(i).setTemplate(template)
        }
        template.getMetas.addAll(docMetaBeans)
        entityDao.saveOrUpdate(template)
      }
      redirect(new Action(classOf[DocTemplateAction], "search"), "info.save.success")
    } catch {
      case e: Exception => {
        e.printStackTrace()
        redirect("search", "info.save.failure")
      }
    }
  }
}
