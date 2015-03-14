package org.openurp.edu.eams.teach.election.service.helper

import java.io.StringWriter
import java.io.Writer
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.openurp.edu.eams.teach.election.model.ElectMailTemplate
import org.openurp.edu.teach.lesson.CourseTake
import freemarker.cache.StringTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template

import scala.collection.JavaConversions._

object FreeMarkerHelper {

  def dynamicCompileTemplate(template: ElectMailTemplate, courseTake: CourseTake): ElectMailTemplate = {
    val attrs = CollectUtils.newHashMap()
    attrs.put("courseTake", courseTake)
    val title = dynamicCompile(classOf[ElectMailTemplate].getName + template.getId + 
      ".title", template.getTitle, attrs)
    val content = dynamicCompile(classOf[ElectMailTemplate].getName + template.getId + 
      ".content", template.getContent, attrs)
    val result = new ElectMailTemplate()
    result.setContent(content)
    result.setTitle(title)
    result
  }

  private def dynamicCompile(name: String, templateSource: String, data: Map[String, Any]): String = {
    val cfg = new Configuration()
    cfg.setNumberFormat("#")
    val loader = new StringTemplateLoader()
    if (null == data) {
      data = CollectUtils.newHashMap()
    }
    loader.putTemplate(name, templateSource)
    cfg.setTemplateLoader(loader)
    var template: Template = null
    try {
      template = cfg.getTemplate(name)
      val out = new StringWriter()
      template.process(data, out)
      out.toString
    } catch {
      case e: Exception => {
        e.printStackTrace()
        ""
      }
    }
  }
}
