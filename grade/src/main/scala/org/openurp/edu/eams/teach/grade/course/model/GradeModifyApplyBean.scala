package org.openurp.edu.eams.teach.grade.course.model

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.NumberIdTimeObject
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.base.Course
import org.openurp.edu.teach.code.ExamStatus
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import GradeModifyApplyBean._




object GradeModifyApplyBean {

  object GradeModifyStatus extends Enumeration {

    val NOT_AUDIT = new GradeModifyStatus("未审核")

    val DEPART_AUDIT_PASSED = new GradeModifyStatus("院系审核通过")

    val DEPART_AUDIT_UNPASSED = new GradeModifyStatus("院系审核未通过")

    val ADMIN_AUDIT_PASSED = new GradeModifyStatus("院长审核通过")

    val ADMIN_AUDIT_UNPASSED = new GradeModifyStatus("院长审核未通过")

    val FINAL_AUDIT_PASSED = new GradeModifyStatus("最终审核通过")

    val FINAL_AUDIT_UNPASSED = new GradeModifyStatus("最终审核未通过")

    val GRADE_DELETED = new GradeModifyStatus("成绩已被删除")

    class GradeModifyStatus private () extends Val {

      
      var fullName: String = _

      private def this(fullName: String) {
        this()
        this.fullName = fullName
      }
    }

    implicit def convertValue(v: Value): GradeModifyStatus = v.asInstanceOf[GradeModifyStatus]
  }
}

@SerialVersionUID(-4325413107423926231L)
@Entity(name = "org.openurp.edu.eams.teach.grade.course.model.GradeModifyApply")
class GradeModifyApplyBean extends NumberIdTimeObject[Long] with GradeModifyApply {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var std: Student = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var semester: Semester = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var project: Project = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var course: Course = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var gradeType: GradeType = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var examStatus: ExamStatus = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  
  var examStatusBefore: ExamStatus = _

  
  var origScore: java.lang.Float = _

  
  var origScoreText: String = _

  
  var score: java.lang.Float = _

  
  var scoreText: String = _

  @NotNull
  @Enumerated(value = EnumType.STRING)
  
  var status: GradeModifyStatus = GradeModifyStatus.NOT_AUDIT

  
  var applyReason: String = _

  
  var auditReason: String = _

  @Size(max = 50)
  
  var applyer: String = _

  @Size(max = 50)
  
  var auditer: String = _

  @Size(max = 50)
  
  var finalAuditer: String = _

  def hasChange(): Boolean = {
    if (this.score == null || this.origScore == null) {
      return this.score != this.origScore || this.examStatus != this.examStatusBefore
    }
    this.score != this.origScore || this.examStatus != this.examStatusBefore
  }
}
