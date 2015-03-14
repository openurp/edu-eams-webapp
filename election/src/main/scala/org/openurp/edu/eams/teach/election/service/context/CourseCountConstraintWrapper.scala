package org.openurp.edu.eams.teach.election.service.context

import org.openurp.edu.eams.teach.election.model.constraint.StdCourseCountConstraint

import scala.collection.JavaConversions._

@SerialVersionUID(6271661100769855121L)
class CourseCountConstraintWrapper extends ElectConstraintWrapper[Integer]() {

  private var electedCount: Int = 0

  private var maxCount: Int = java.lang.Integer.MAX_VALUE

  def this(maxCount: java.lang.Integer, electedCount: java.lang.Integer) {
    this()
    if (null != maxCount) {
      this.maxCount = maxCount
    }
    if (null != electedCount) {
      this.electedCount = electedCount
    }
  }

  def this(constraint: StdCourseCountConstraint, electedCount: java.lang.Integer) {
    this()
    if (null != constraint) {
      val maxCount = constraint.getMaxCourseCount
      if (null != maxCount) {
        this.maxCount = maxCount
      }
    }
    if (null != electedCount) {
      this.electedCount = electedCount
    }
  }

  def subElectedItem(count: java.lang.Integer): java.lang.Integer = {
    if (null == count) {
      this.electedCount -= 1
    } else {
      this.electedCount -= count
    }
    this.electedCount
  }

  def addElectedItem(count: java.lang.Integer): java.lang.Integer = {
    if (null == count) {
      this.electedCount += 1
    } else {
      this.electedCount += count
    }
    this.electedCount
  }

  def isOverMax(count: java.lang.Integer): Boolean = {
    if (null == count) {
      return this.electedCount > this.maxCount
    }
    electedCount + count > this.maxCount
  }

  override def toString(): String = {
    "已选:" + this.electedCount + "门,上限:" + maxCount + "门"
  }
}
