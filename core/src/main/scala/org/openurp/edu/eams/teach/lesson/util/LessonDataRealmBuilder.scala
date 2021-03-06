package org.openurp.edu.eams.teach.lesson.util

import java.text.MessageFormat

import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.base.code.StdType
import LessonDataRealmBuilder._


object LessonDataRealmBuilder {

  private val IN_PROJECTS = "{0}.project in (:projects_{1})"

  private val IN_TEACT_DEPARTS = "{0}.teachDepart in (:departs_{1})"

  private val IN_STDTYPES = "{0} in (:stdTypes_{1})"

  private val IN_EDUCATIONS = "{0} in (:educations_{1})"

  private val IN_PROJECT_IDS = "{0}.project.id in (:projectIds_{1})"

  private val IN_TEACH_DEPART_IDS = "{0}.teachDepart.id in (:departIds_{1})"

  private val IN_STDTYPE_IDS = "{0}.id in (:stdTypeIds_{1})"

  private val IN_EDUCATION_IDS = "{0}.id in (:educationIds_{1})"

  def start(entityDao: EntityDao, query: OqlBuilder[_], lessonAlias: String): LessonDataRealmBuilder = {
    new LessonDataRealmBuilder(entityDao, query, lessonAlias)
  }
}

class LessonDataRealmBuilder  {

  private var entityDao: EntityDao = _

  private var query: OqlBuilder[_] = _

  private var lessonAlias: String = _

  private def this(entityDao: EntityDao, query: OqlBuilder[_], lessonAlias: String) {
    this()
    this.entityDao = entityDao
    this.query = query
    this.lessonAlias = lessonAlias
  }

  def restrictProjects(projects: List[Project]): LessonDataRealmBuilder = {
    query.where(MessageFormat.format(IN_PROJECTS, lessonAlias, java.lang.Long.valueOf(System.currentTimeMillis)), projects)
    this
  }

  def restrictTeachDeparts(departs: List[Department]): LessonDataRealmBuilder = {
    query.where(MessageFormat.format(IN_TEACT_DEPARTS, lessonAlias, java.lang.Long.valueOf(System.currentTimeMillis)), departs)
    this
  }

  def restrictStdTypes(stdTypes: List[StdType]): LessonDataRealmBuilder = this

  def restrictEducations(educations: List[Education]): LessonDataRealmBuilder = this

  def restrictProjects(projectIds: Array[Integer]): LessonDataRealmBuilder = {
    query.where(MessageFormat.format(IN_PROJECT_IDS, lessonAlias, java.lang.Long.valueOf(System.currentTimeMillis)), projectIds)
    this
  }

  def restrictTeachDeparts(departIds: Array[Long]): LessonDataRealmBuilder = {
    query.where(MessageFormat.format(IN_TEACH_DEPART_IDS, lessonAlias, java.lang.Long.valueOf(System.currentTimeMillis)), departIds)
    this
  }

  def restrictStdTypes(stdTypeIds: Array[Integer]): LessonDataRealmBuilder = this

  def restrictEducations(educationIds: Array[Integer]): LessonDataRealmBuilder = this

  def finish(): OqlBuilder[_] = query
}
