package org.openurp.edu.eams.teach.grade.lesson.web.action



import org.beangle.commons.transfer.TransferListener
import org.beangle.commons.transfer.importer.listener.ImporterForeignerListener
import org.beangle.commons.transfer.importer.listener.ItemImporterListener
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.service.CourseGradeImportListener
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.eams.web.util.DownloadHelper
import com.opensymphony.xwork2.util.ClassLoaderUtil



class RevokeAction extends AuditAction {

  protected override def getEntityName(): String = classOf[CourseGrade].getName

  def downloadTemplate(): String = {
    val template = get("template")
    DownloadHelper.download(getRequest, getResponse, ClassLoaderUtil.getResource(template, this.getClass), 
      null)
    null
  }

  def revoke(): String = {
    val gradeTypeId = getInt("gradeTypeId")
    var gradeTypes: Array[GradeType] = null
    gradeTypes = if (null == gradeTypeId) baseCodeService.getCodes(classOf[GradeType]).toArray().asInstanceOf[Array[GradeType]] else if (settings.getSetting(getProject).getFinalCandinateTypes
      .contains(new GradeType(gradeTypeId))) entityDao.get(classOf[GradeType], Array(gradeTypeId, GradeTypeConstants.FINAL_ID))
      .toArray(Array.ofDim[GradeType](2)) else Array(baseCodeService.getCode(classOf[GradeType], gradeTypeId))
    courseGradeService.publish(get("lessonIds"), gradeTypes, false)
    redirect("search", "取消发布成功", "status=" + get("status"))
  }

  protected override def getImporterListeners(): List[_ <: TransferListener] = {
    val listeners = new ArrayList[ItemImporterListener]()
    listeners.add(new ImporterForeignerListener(entityDao))
    listeners.add(new CourseGradeImportListener(entityDao, getProject, calculator))
    listeners
  }
}
