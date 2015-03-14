package org.openurp.edu.eams.grade.domain

import org.openurp.teach.code.ScoreMarkStyle
import org.openurp.teach.code.GradeType
import org.openurp.teach.code.ExamStatus
import org.openurp.teach.code.ExamType
import collection.mutable
import org.openurp.teach.core.ProjectBasedObject
import org.openurp.teach.core.Project
import org.openurp.teach.code.model.GradeTypeBean
import scala.collection.mutable.Buffer
import org.openurp.teach.code.model.ExamStatusBean
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
    finalCandinateTypes += new GradeTypeBean(GradeType.Ga)
    finalCandinateTypes += new GradeTypeBean(GradeType.Makeup)
    finalCandinateTypes += new GradeTypeBean(GradeType.Delay)
    publishableTypes += new GradeTypeBean(GradeType.Final)
    publishableTypes += new GradeTypeBean(GradeType.Makeup)
    publishableTypes += new GradeTypeBean(GradeType.Delay)
    publishableTypes += new GradeTypeBean(GradeType.Ga)
    gaElementTypes += new GradeTypeBean(GradeType.Usual)
    gaElementTypes += new GradeTypeBean(GradeType.Middle)
    gaElementTypes += new GradeTypeBean(GradeType.End)
    allowExamStatuses += new ExamStatusBean(ExamStatus.Normal)
    allowExamStatuses += new ExamStatusBean(ExamStatus.Misc)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Absent)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Cheat)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Violation)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Delay)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Misc)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Unqualify)
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
