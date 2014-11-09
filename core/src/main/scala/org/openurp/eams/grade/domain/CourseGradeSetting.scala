package org.openurp.eams.grade.domain

import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.code.ExamType
import collection.mutable
import org.openurp.teach.core.ProjectBasedObject
import org.openurp.teach.core.Project
import org.openurp.teach.code.model.GradeTypeBean
import scala.collection.mutable.Buffer
/**
 * 课程成绩配置
 *
 * @author chaostone
 */
class CourseGradeSetting extends ProjectBasedObject[Long] {

  /**
   * 可以转换为最终成绩的考试成绩类型id
   */
  var finalCandinateTypes: Buffer[GradeType] = new mutable.ListBuffer[GradeType]

  /**
   * 总评成绩的组成部分
   */
  var gaElementTypes: Buffer[GradeType] = new mutable.ListBuffer[GradeType]

  /**
   * 可以发布的成绩
   */
  var publishableTypes: Buffer[GradeType] = new mutable.ListBuffer[GradeType]

  /**
   * 允许补考考试类型
   */
  var allowExamStatuses: collection.Set[ExamStatus] = new mutable.HashSet[ExamStatus]

  /**
   * 是否计算总评的考试情况
   */
  var calcGaExamStatus: Boolean = false

  /**
   * 不允许录入成绩的考试类型列表
   */
  var emptyScoreStatuses: collection.Set[ExamStatus] = new mutable.HashSet[ExamStatus]

  /**
   * 是否提交即发布
   */
  var submitIsPublish: Boolean = false

  def this(project: Project) {
    this()
    finalCandinateTypes += new GradeTypeBean(GradeType.GA_ID)
    finalCandinateTypes += new GradeTypeBean(GradeType.MAKEUP_ID)
    finalCandinateTypes += new GradeTypeBean(GradeType.DELAY_ID)
    publishableTypes += new GradeTypeBean(GradeType.FINAL_ID)
    publishableTypes += new GradeTypeBean(GradeType.MAKEUP_ID)
    publishableTypes += new GradeTypeBean(GradeType.DELAY_ID)
    publishableTypes += new GradeTypeBean(GradeType.GA_ID)
    gaElementTypes += new GradeTypeBean(GradeType.USUAL_ID)
    gaElementTypes += new GradeTypeBean(GradeType.MIDDLE_ID)
    gaElementTypes += new GradeTypeBean(GradeType.END_ID)
    allowExamStatuses += new ExamStatus(ExamStatus.NORMAL)
    allowExamStatuses += new ExamStatus(ExamStatus.MISC)
    emptyScoreStatuses += new ExamStatus(ExamStatus.ABSENT)
    emptyScoreStatuses += new ExamStatus(ExamStatus.CHEAT)
    emptyScoreStatuses += new ExamStatus(ExamStatus.VIOLATION)
    emptyScoreStatuses += new ExamStatus(ExamStatus.DELAY)
    emptyScoreStatuses += new ExamStatus(ExamStatus.MISC)
    emptyScoreStatuses += new ExamStatus(ExamStatus.UNQUALIFY)
  }
}

class GradeTypeConfig {

  var publishable: Boolean = _

  var finalCandinate: Boolean = _

  var gaElement: Boolean = _

  var examType: ExamType = _

  var markStyles = new collection.mutable.ListBuffer[ScoreMarkStyle]

  var precision: Int = _
}
