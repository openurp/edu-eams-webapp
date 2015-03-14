package org.openurp.edu.eams.teach.grade.lesson

import org.beangle.commons.inject.bind.AbstractBindModule
import org.openurp.edu.eams.teach.grade.lesson.service.impl.LessonGradeServiceImpl
import org.openurp.edu.eams.teach.grade.lesson.web.action.AuditAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.InputAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.InputSwitchAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.MakeupAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.PublishAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.ReportAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.RetakeAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.RevokeAction
import org.openurp.edu.eams.teach.grade.lesson.web.action.TeacherReportAction

import scala.collection.JavaConversions._

class DefaultModule extends AbstractBindModule {

  protected override def doBinding() {
    bind(classOf[InputAction], classOf[AuditAction], classOf[PublishAction], classOf[RevokeAction], classOf[InputSwitchAction], 
      classOf[MakeupAction], classOf[RetakeAction], classOf[ReportAction], classOf[TeacherReportAction])
    bind("lessonGradeService", classOf[LessonGradeServiceImpl])
  }
}
