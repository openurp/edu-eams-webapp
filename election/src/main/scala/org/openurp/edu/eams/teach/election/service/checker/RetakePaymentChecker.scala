package org.openurp.edu.eams.teach.election.service.checker


import org.openurp.edu.base.Student
import org.openurp.edu.eams.fee.Bill
import org.openurp.edu.eams.fee.code.industry.PayState
import org.openurp.edu.eams.fee.service.PaymentChecker
import org.openurp.edu.eams.fee.service.impl.PaymentContext
import org.openurp.edu.eams.teach.election.RetakeFeeConfig



class RetakePaymentChecker extends PaymentChecker {

  def check(context: PaymentContext): String = {
    val student = context.get("student", classOf[Student])
    if (null == student) {
      return "没有权限"
    }
    val configs = context.get("feeConfigs").asInstanceOf[List[RetakeFeeConfig]]
    if (configs.isEmpty) {
      return "在线支付已关闭"
    }
    val bill = context.getBill
    if (null == bill) {
      return "没有找到订单"
    }
    if (!bill.inPaymentTime()) {
      return "订单支付时间未开放或已结束"
    }
    if (PayState.UNPAID != bill.getState.id) {
      return "该订单已支付或退订"
    }
    null
  }
}
