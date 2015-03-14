package org.openurp.edu.eams.teach.grade.course.model

import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.NumberIdTimeObject
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.code.industry.ExamStatus
import org.openurp.edu.eams.teach.code.industry.GradeType
import org.openurp.edu.eams.teach.grade.course.GradeModifyApply
import GradeModifyApplyBean._
import scala.reflect.{BeanProperty, BooleanBeanProperty}

import scala.collection.JavaConversions._

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

      @BeanProperty
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
  @BeanProperty
  var std: Student = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var semester: Semester = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var project: Project = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var course: Course = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var gradeType: GradeType = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var examStatus: ExamStatus = _

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @BeanProperty
  var examStatusBefore: ExamStatus = _

  @BeanProperty
  var origScore: java.lang.Float = _

  @BeanProperty
  var origScoreText: String = _

  @BeanProperty
  var score: java.lang.Float = _

  @BeanProperty
  var scoreText: String = _

  @NotNull
  @Enumerated(value = EnumType.STRING)
  @BeanProperty
  var status: GradeModifyStatus = GradeModifyStatus.NOT_AUDIT

  @BeanProperty
  var applyReason: String = _

  @BeanProperty
  var auditReason: String = _

  @Size(max = 50)
  @BeanProperty
  var applyer: String = _

  @Size(max = 50)
  @BeanProperty
  var auditer: String = _

  @Size(max = 50)
  @BeanProperty
  var finalAuditer: String = _

  def hasChange(): Boolean = {
    if (this.score == null || this.origScore == null) {
      return this.score != this.origScore || this.examStatus != this.examStatusBefore
    }
    this.score != this.origScore || this.examStatus != this.examStatusBefore
  }
}
