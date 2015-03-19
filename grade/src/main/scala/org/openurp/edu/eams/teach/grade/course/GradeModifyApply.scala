package org.openurp.edu.eams.teach.grade.course

import org.beangle.data.model.Entity
import org.beangle.commons.entity.TimeEntity
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.model.GradeModifyApplyBean.GradeModifyStatus



trait GradeModifyApply extends Entity[Long] with TimeEntity {

  def getGradeType(): GradeType

  def setGradeType(gradeType: GradeType): Unit

  def getExamStatus(): ExamStatus

  def setExamStatus(examStatus: ExamStatus): Unit

  def getScore(): java.lang.Float

  def setScore(score: java.lang.Float): Unit

  def getCourse(): Course

  def setCourse(course: Course): Unit

  def getStd(): Student

  def setStd(std: Student): Unit

  def getSemester(): Semester

  def setSemester(semester: Semester): Unit

  def getProject(): Project

  def setProject(project: Project): Unit

  def getStatus(): GradeModifyStatus

  def setStatus(status: GradeModifyStatus): Unit

  def getApplyer(): String

  def setApplyer(applyer: String): Unit

  def getAuditer(): String

  def setAuditer(auditer: String): Unit

  def getFinalAuditer(): String

  def setFinalAuditer(finalAuditer: String): Unit

  def getScoreText(): String

  def setScoreText(scoreText: String): Unit

  def getOrigScoreText(): String

  def setOrigScoreText(origScoreText: String): Unit

  def getOrigScore(): java.lang.Float

  def setOrigScore(origScore: java.lang.Float): Unit

  def getExamStatusBefore(): ExamStatus

  def setExamStatusBefore(examStatusBefore: ExamStatus): Unit

  def hasChange(): Boolean

  def getApplyReason(): String

  def setApplyReason(applyReason: String): Unit

  def getAuditReason(): String

  def setAuditReason(auditReason: String): Unit
}
