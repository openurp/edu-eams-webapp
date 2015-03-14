package org.openurp.edu.eams.teach.election.web.action.courseTake

import java.util.Collection
import java.util.List
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.StudentJournal
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.teach.code.ElectionMode
import org.openurp.edu.eams.teach.election.model.ElectMailTemplate
import org.openurp.edu.eams.teach.election.service.propertyExtractor.CourseTakePropertyExtractor
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class CourseTakeSearchAction extends SemesterSupportAction {

  protected override def getEntityName(): String = classOf[CourseTake].getName

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    put("courseTakeTypes", baseCodeService.getCodes(classOf[CourseTakeType]))
    put("electionModes", baseCodeService.getCodes(classOf[ElectionMode]))
    put("teachDeparts", getCollegeOfDeparts)
    putSemester(null)
    forward()
  }

  def search(): String = {
    val stdCode = get("courseTake.std.code")
    if (Strings.isNotBlank(stdCode)) {
      val stds = entityDao.get(classOf[Student], "code", stdCode.trim())
      if (stds.size == 1) {
        put("searchStd", stds.get(0))
      }
    }
    put("courseTakeTypes", baseCodeService.getCodes(classOf[CourseTakeType]))
    put("withdrawTemplateId", ElectMailTemplate.WITHDRAW)
    super.search()
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = super.getQueryBuilder
    builder.where(getShortName + ".lesson.semester=:semester", putSemester(null))
    populateConditions(builder, "courseTake.std.type.id")
    restrictionHelper.applyRestriction(builder)
    val departs = getDeparts
    if (departs.isEmpty) {
      builder.where("1=2")
    } else {
      builder.where("courseTake.lesson.teachDepart in(:departs)", departs)
    }
    val isCourseEvaluated = getBoolean("courseTake.isCourseEvaluated")
    if (null != isCourseEvaluated) {
      builder.where("courseTake.lesson.semester in(:semester)", putSemester(null))
      if (!isCourseEvaluated) {
        builder.where("not exists(from org.openurp.edu.eams.quality.evaluate.course.model.EvaluateResult er  where er.student=courseTake.std and er.lesson = courseTake.lesson) " + 
          "and exists(from org.openurp.edu.eams.quality.evaluate.course.model.QuestionnaireLesson ql " + 
          "where ql.lesson=courseTake.lesson)")
      } else {
        builder.where("exists(from org.openurp.edu.eams.quality.evaluate.course.model.EvaluateResult er  where er.student=courseTake.std and er.lesson = courseTake.lesson) " + 
          "or not exists(from org.openurp.edu.eams.quality.evaluate.course.model.QuestionnaireLesson ql " + 
          "where ql.lesson=courseTake.lesson)")
      }
    }
    val inSchool = getBoolean("courseTake.std.inSchool")
    if (null != inSchool) {
      if (inSchool) {
        builder.where("exists(" + "select max(stdJournal.id) from " + classOf[StudentJournal].getName + 
          " stdJournal " + 
          "where stdJournal.std=courseTake.std " + 
          "and stdJournal.beginOn<=:now and stdJournal.endOn>=:now " + 
          "and stdJournal.inschool = 1)", new java.sql.Date(System.currentTimeMillis()))
      } else {
        builder.where("not exists(" + "select max(stdJournal.id) from " + classOf[StudentJournal].getName + 
          " stdJournal " + 
          "where stdJournal.std=courseTake.std " + 
          "and stdJournal.beginOn<=:now and stdJournal.endOn>=:now " + 
          "and stdJournal.inschool = 1)", new java.sql.Date(System.currentTimeMillis()))
      }
    }
    val project = getProject
    builder.where("courseTake.lesson.project=:project", project)
    builder.where("courseTake.std.project=:project", project)
    builder
  }

  protected override def getExportDatas(): Collection[_] = {
    val builder = getQueryBuilder.asInstanceOf[OqlBuilder[CourseTake]]
    val courseTakeIds = getLongIds("courseTake")
    if (null != courseTakeIds && courseTakeIds.length > 0) {
      builder.where("courseTake.id in (:courseTakeIds)", courseTakeIds)
    }
    builder.limit(null)
    entityDao.search(builder)
  }

  protected override def getPropertyExtractor(): PropertyExtractor = {
    new CourseTakePropertyExtractor(getTextResource)
  }
}
