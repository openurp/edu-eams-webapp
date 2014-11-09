package org.openurp.eams.grade.service.internal

import java.text.NumberFormat
import java.util.Iterator
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.impl.BaseServiceImpl
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.commons.script.ExpressionEvaluator
import com.ekingstar.eams.core.Project
import com.ekingstar.eams.teach.code.industry.GradeType
import com.ekingstar.eams.teach.code.industry.ScoreMarkStyle
import com.ekingstar.eams.teach.grade.model.GradeRateConfig
import com.ekingstar.eams.teach.grade.model.GradeRateItem
import com.ekingstar.eams.teach.grade.service.GradeRateService
import com.ekingstar.eams.teach.lesson.CourseGrade
import com.ekingstar.eams.teach.lesson.GradeTypeConstants
//remove if not needed
import scala.collection.JavaConversions._

class GradeRateServiceImpl extends BaseServiceImpl with GradeRateService {

  private var expressionEvaluator: ExpressionEvaluator = _

  /**
   * 依照绩点规则计算平均绩点
   *
   * @param rule
   */
  def calcGp(grade: CourseGrade): java.lang.Float = {
    val conifg = getConfig(grade.getStd.getProject, grade.getMarkStyle)
    if (null != conifg) {
      var gp = calcGp(grade.score, conifg)
      if (null != gp && gp.floatValue() > 1 && null != grade.score && 
        grade.score < 61) {
        if (null != 
          grade.getExamGrade(new GradeType(GradeTypeConstants.MAKEUP_ID))) gp = 1.0f
      }
      return gp
    }
    null
  }

  /**
   * 计算分数对应的绩点
   *
   * @param score
   * @param conifg
   * @return
   */
  private def calcGp(score: java.lang.Float, conifg: GradeRateConfig): java.lang.Float = {
    if (null == score || score.floatValue() <= 0) return new java.lang.Float(0) else {
      var iter = conifg.getItems.iterator()
      while (iter.hasNext) {
        val gradeRateItem = iter.next()
        if (gradeRateItem.inScope(score)) {
          if (Strings.isNotEmpty(gradeRateItem.gpExp)) {
            val data = CollectUtils.newHashMap()
            data.put("score", score)
            return expressionEvaluator.eval(gradeRateItem.gpExp, data, classOf[Float])
          } else {
            return null
          }
        }
      }
    }
    new java.lang.Float(0)
  }

  /**
   * 将字符串按照成绩记录方式转换成数字.<br>
   * 空成绩将转换成null
   *
   * @param score
   * @param markStyle
   * @return
   */
  def convert(score: String, scoreMarkStyle: ScoreMarkStyle, project: Project): java.lang.Float = {
    if (Strings.isBlank(score)) return null
    val config = getConfig(project, scoreMarkStyle).asInstanceOf[GradeRateConfig]
    if (null == config || config.getItems.size == 0) {
      if (Numbers.isDigits(score)) new java.lang.Float(Numbers.toFloat(score)) else null
    } else {
      val newScore = config.convert(score)
      if (null != newScore) {
        return newScore
      }
      if (Numbers.isDigits(score)) {
        return new java.lang.Float(Numbers.toFloat(score))
      }
      null
    }
  }

  def isPassed(score: java.lang.Float, scoreMarkStyle: ScoreMarkStyle, project: Project): Boolean = {
    val config = getConfig(project, scoreMarkStyle)
    if (null == config || null == score) {
      false
    } else {
      Float.compare(score, config.getPassScore) >= 0
    }
  }

  /**
   * 将字符串按照成绩记录方式转换成数字.<br>
   * 空成绩将转换成""
   *
   * @param score
   * @param markStyle
   * @return
   */
  def convert(score: java.lang.Float, scoreMarkStyle: ScoreMarkStyle, project: Project): String = {
    if (null == score) {
      return ""
    }
    val config = getConfig(project, scoreMarkStyle)
    if (null == config) {
      NumberFormat.getInstance.format(score.floatValue())
    } else {
      config.convert(score)
    }
  }

  /**
   * 查询记录方式对应的配置
   */
  def getConfig(project: Project, scoreMarkStyle: ScoreMarkStyle): GradeRateConfig = {
    if (null == project || project.isTransient) return null
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "config")
      .where("config.project=:project and config.scoreMarkStyle=:markStyle", project, scoreMarkStyle)
      .cacheable()
    entityDao.uniqueResult(builder)
  }

  /**
   * 获得支持的记录方式
   *
   * @param project
   * @return
   */
  def getMarkStyles(project: Project): List[ScoreMarkStyle] = {
    val builder = OqlBuilder.from(classOf[GradeRateConfig], "config")
      .where("config.project=:project", project)
      .select("config.scoreMarkStyle")
      .cacheable()
    entityDao.search(builder)
  }

  def setExpressionEvaluator(expressionEvaluator: ExpressionEvaluator) {
    this.expressionEvaluator = expressionEvaluator
  }
}
