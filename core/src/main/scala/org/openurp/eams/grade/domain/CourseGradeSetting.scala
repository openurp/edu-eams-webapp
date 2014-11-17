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
import org.openurp.teach.code.model.ExamStatusBean
/**
 * 课程成绩配置
 *
 * @author chaostone
 */
class CourseGradeSetting extends ProjectBasedObject[Long] {

  var precision: Short = 0
  /**
   * 总评成绩的组成部分
   */
  var endGaElements: collection.Set[GradeType] = new mutable.HashSet[GradeType]

  /**
   * 总评成绩的组成部分
   */
  var delayGaElements: collection.Set[GradeType] = new mutable.HashSet[GradeType]
  /**
   * 总评成绩的组成部分
   */
  var makeupGaElements: collection.Set[GradeType] = new mutable.HashSet[GradeType]

  /**
   * 允许补考考试类型
   */
  var allowExamStatuses: collection.Set[ExamStatus] = new mutable.HashSet[ExamStatus]

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
    endGaElements += new GradeTypeBean(GradeType.Usual)
    endGaElements += new GradeTypeBean(GradeType.Middle)
    endGaElements += new GradeTypeBean(GradeType.End)

    delayGaElements += new GradeTypeBean(GradeType.Usual)
    delayGaElements += new GradeTypeBean(GradeType.Middle)
    delayGaElements += new GradeTypeBean(GradeType.Delay)

    makeupGaElements += new GradeTypeBean(GradeType.Makeup)

    allowExamStatuses += new ExamStatusBean(ExamStatus.Normal)
    allowExamStatuses += new ExamStatusBean(ExamStatus.Misc)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Absent)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Cheat)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Violation)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Delay)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Misc)
    emptyScoreStatuses += new ExamStatusBean(ExamStatus.Unqualify)
  }

  def getRemovableElements(gradeType: GradeType): collection.Set[GradeType] = {
    gradeType.id match {
      case GradeType.EndGa    => endGaElements
      case GradeType.DelayGa  => Set(new GradeTypeBean(GradeType.Delay))
      case GradeType.MakeupGa => makeupGaElements
      case _                  => Set.empty
    }
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
