package org.openurp.edu.eams.teach.grade.service

import java.util.List
import org.openurp.edu.base.Project
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.teach.grade.CourseGrade

import scala.collection.JavaConversions._

trait GradeRateService {

  def calcGp(grade: CourseGrade): java.lang.Float

  def convert(score: String, scoreMarkStyle: ScoreMarkStyle, project: Project): java.lang.Float

  def convert(score: java.lang.Float, scoreMarkStyle: ScoreMarkStyle, project: Project): String

  def isPassed(score: java.lang.Float, scoreMarkStyle: ScoreMarkStyle, project: Project): Boolean

  def getConfig(project: Project, scoreMarkStyle: ScoreMarkStyle): GradeRateConfig

  def getMarkStyles(project: Project): List[ScoreMarkStyle]
}
