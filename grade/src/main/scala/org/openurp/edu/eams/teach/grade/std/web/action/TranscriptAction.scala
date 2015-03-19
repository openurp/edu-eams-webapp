package org.openurp.edu.eams.teach.grade.std.web.action

import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormatimport java.util.Date


import javax.servlet.http.HttpServletRequest
import org.apache.commons.io.FileUtils
import org.beangle.commons.lang.Strings
import org.apache.struts2.views.freemarker.FreemarkerResult
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.web.url.UrlBuilder
import org.beangle.security.codec.EncryptUtil
import org.openurp.edu.base.Student
import org.openurp.edu.eams.system.report.ReportTemplate
import org.openurp.edu.eams.system.report.service.ReportTemplateService
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.transcript.service.TranscriptDataProvider
import org.openurp.edu.eams.teach.grade.transcript.service.impl.SpringTranscriptDataProviderRegistry
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.action.BaseAction
import org.openurp.edu.eams.web.util.DownloadHelper
import com.google.gson.Gson
import com.opensymphony.xwork2.ActionContext



class TranscriptAction extends BaseAction {

  private var reportTemplateService: ReportTemplateService = _

  private var dataProviderRegistry: SpringTranscriptDataProviderRegistry = _

  def index(): String = {
    val me = getLoginStudent
    val checksum = get("checksum")
    val now = new Date(System.currentTimeMillis())
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val mycheck = EncryptUtil.encode(me.getCode + dateFormat.format(now))
    if (!mycheck.equalsIgnoreCase(checksum)) {
      return "errorChecksum"
    }
    val students = Collections.singletonList(me)
    var template: ReportTemplate = null
    val templateName = get("template")
    if (null != templateName) template = reportTemplateService.getTemplate(me.getProject, templateName)
    var options = CollectUtils.newHashMap()
    if (null != template) options = new Gson().fromJson(template.getOptions, classOf[Map[_,_]])
    if (null == options) options = CollectUtils.newHashMap()
    for (provider <- dataProviderRegistry.getProviders(options.get("providers"))) {
      put(provider.getDataName, provider.getDatas(students, options))
    }
    put("date", now)
    put("school", me.getProject.getSchool)
    put("students", students)
    put("RESTUDY", CourseTakeType.RESTUDY)
    put("GA", entityDao.get(classOf[GradeType], GradeTypeConstants.GA_ID))
    val format = get("format")
    if (null != template && "pdf" == format) {
      val html = getHtml(template)
      val tempHtmlFile = File.createTempFile("transcript", ".html")
      FileUtils.writeStringToFile(tempHtmlFile, html)
      val tmpHtmlName = tempHtmlFile.getName
      val path = Strings.substringBefore(tempHtmlFile.getCanonicalPath, tmpHtmlName)
      val tmpPdfName = Strings.substringBefore(tmpHtmlName, ".") + ".pdf"
      val tempPdfFile = new File(path + "/" + tmpPdfName)
      val pb = new ProcessBuilder()
      pb.redirectErrorStream(true)
      pb.command(Array("wkhtmltopdf", "-s", template.getPageSize, "-O", template.getOrientation, tempHtmlFile.getCanonicalPath, tempPdfFile.getCanonicalPath))
      val p = pb.start()
      val result = p.waitFor()
      var title = "学生成绩总表.pdf"
      if (students.size == 1) title = students.get(0).getCode + " " + students.get(0).getName + 
        " " + 
        title
      if (0 == result) DownloadHelper.download(getRequest, getResponse, tempPdfFile, title)
      tempPdfFile.delete()
      tempHtmlFile.delete()
      null
    } else {
      if (null == template) forward() else {
        var templatePath = template.getTemplate
        if (templatePath.startsWith("freemarker:")) templatePath = templatePath.substring("freemarker:".length)
        if (templatePath.endsWith(".ftl")) templatePath = Strings.substringBeforeLast(templatePath, ".ftl")
        forward(templatePath)
      }
    }
  }

  private def getHtml(template: ReportTemplate): String = {
    val result = new FreemarkerResult()
    ActionContext.getContext.getContainer.inject(result)
    val sw = new StringWriter()
    result.setWriter(sw)
    try {
      var templatePath = template.getTemplate
      if (templatePath.startsWith("freemarker:")) templatePath = templatePath.substring("freemarker:".length)
      if (!templatePath.endsWith(".ftl")) templatePath = templatePath + ".ftl"
      result.doExecute(templatePath, ActionContext.getContext.getActionInvocation)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    val request = getRequest
    val builder = new UrlBuilder(request.getContextPath)
    builder.scheme(request.getScheme)
    builder.port(request.getServerPort)
    builder.serverName(request.getServerName)
    var templateResult = sw.toString
    templateResult = Strings.replace(templateResult, "src=\"" + request.getContextPath, "src=\"" + builder.buildUrl())
    templateResult
  }

  def setReportTemplateService(reportTemplateService: ReportTemplateService) {
    this.reportTemplateService = reportTemplateService
  }

  def setDataProviderRegistry(dataProviderRegistry: SpringTranscriptDataProviderRegistry) {
    this.dataProviderRegistry = dataProviderRegistry
  }
}
