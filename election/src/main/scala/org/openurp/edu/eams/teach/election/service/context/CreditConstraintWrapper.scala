package org.openurp.edu.eams.teach.election.service.context

import org.openurp.edu.eams.teach.election.model.constraint.AbstractCreditConstraint

import scala.collection.JavaConversions._

@SerialVersionUID(-6797232529157151263L)
class CreditConstraintWrapper(constraint: AbstractCreditConstraint, l_electedCredits: java.lang.Float)
    extends ElectConstraintWrapper[Float]() {

  private var electedCredits: Float = 0

  private var maxCredits: Float = java.lang.Float.MAX_VALUE

  if (null != constraint) {
    val maxCredits = constraint.getMaxCredit
    if (null != maxCredits) {
      this.maxCredits = maxCredits
    }
  }

  if (null != l_electedCredits) {
    this.electedCredits = l_electedCredits
  }

  def subElectedItem(credits: java.lang.Float): java.lang.Float = {
    if (null != credits) {
      this.electedCredits -= credits
    }
    this.electedCredits
  }

  def addElectedItem(credits: java.lang.Float): java.lang.Float = {
    if (null != credits) {
      this.electedCredits += credits
    }
    this.electedCredits
  }

  def isOverMax(credits: java.lang.Float): Boolean = {
    if (null == credits) return electedCredits > maxCredits
    electedCredits + credits > maxCredits
  }

  override def toString(): String = {
    "已选:" + this.electedCredits + ",上限:" + maxCredits
  }
}
