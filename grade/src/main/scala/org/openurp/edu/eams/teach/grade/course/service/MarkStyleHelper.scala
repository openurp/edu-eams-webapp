package org.openurp.edu.eams.teach.grade.course.service



import org.beangle.commons.lang.Numbers
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.model.dao.EntityDao
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.model.GradeRateConfig
import org.openurp.edu.eams.teach.grade.model.GradeRateItem



class MarkStyleHelper private () {

  private var gradeStyles: Map[String, ScoreMarkStyle] = CollectUtils.newHashMap()

  private var styles: Map[String, ScoreMarkStyle] = CollectUtils.newHashMap()

  private var defaultNumberStyle: ScoreMarkStyle = _

  private var entityDao: EntityDao = _

  def init(defaultNumberStyleId: java.lang.Integer) {
    val configs = entityDao.getAll(classOf[GradeRateConfig])
    for (config <- configs if !config.getScoreMarkStyle.isNumStyle) {
      val items = config.getItems
      for (item <- items if null != item.grade) {
        gradeStyles.put(item.grade, config.getScoreMarkStyle)
      }
    }
    val mss = entityDao.getAll(classOf[ScoreMarkStyle])
    for (style <- mss) {
      styles.put(style.getCode, style)
    }
    if (null != defaultNumberStyleId) {
      defaultNumberStyle = entityDao.get(classOf[ScoreMarkStyle], defaultNumberStyleId).asInstanceOf[ScoreMarkStyle]
    }
  }

  def styleForCode(code: String): ScoreMarkStyle = {
    val style = styles.get(code)
    if (null == style) {
      defaultNumberStyle
    } else {
      style
    }
  }

  def styleForScore(score: String): ScoreMarkStyle = {
    var style = gradeStyles.get(score)
    if (null == style) {
      if (Numbers.isDigits(score)) {
        style = defaultNumberStyle
      }
    }
    style
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
  }
}
