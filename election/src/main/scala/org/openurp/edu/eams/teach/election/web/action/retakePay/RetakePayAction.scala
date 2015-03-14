package org.openurp.edu.eams.teach.election.web.action.retakePay

import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.List
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.security.blueprint.User
import org.beangle.struts2.helper.Params
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.fee.Bill
import org.openurp.edu.eams.fee.Pay
import org.openurp.edu.eams.fee.code.industry.PayState
import org.openurp.edu.eams.fee.code.industry.PayType
import org.openurp.edu.eams.fee.code.school.FeeType
import org.openurp.edu.eams.fee.model.BillBean
import org.openurp.edu.eams.fee.model.BillLogBean.BillLogType
import org.openurp.edu.eams.fee.model.PayBean
import org.openurp.edu.eams.fee.service.BillAmountCalculator
import org.openurp.edu.eams.fee.service.BillCodeGenerator
import org.openurp.edu.eams.fee.service.BillService
import org.openurp.edu.eams.fee.service.PaymentService
import org.openurp.edu.eams.fee.service.impl.BillGenContext
import org.openurp.edu.eams.fee.service.impl.PaymentContext
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import org.openurp.edu.eams.teach.election.service.RetakeFeeConfigService
import org.openurp.edu.teach.lesson.CourseTake
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class RetakePayAction extends SemesterSupportAction {

  private var retakeFeeConfigService: RetakeFeeConfigService = _

  private var paymentService: PaymentService = _

  private var billService: BillService = _

  private var retakeBillCodeGenerator: BillCodeGenerator = _

  private var retakeBillAmountCalculator: BillAmountCalculator = _

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    populateConditions(builder)
    builder.where("courseTake.lesson.semester = :semester", putSemester(null))
    builder.where("courseTake.lesson.project = :project", getProject)
    builder.where("courseTake.courseTakeType.id = :courseTakeTypeId", CourseTakeType.RESTUDY)
    val status = get("status")
    if (Strings.isNotEmpty(status)) {
      if ("0" == status) {
        builder.where("courseTake.retakeFeePaid is false and courseTake.bill is null")
      } else if ("1" == status) {
        builder.where("courseTake.retakeFeePaid is false and courseTake.bill is not null")
      } else if ("2" == status) {
        builder.where("courseTake.retakeFeePaid is true and courseTake.bill is not null")
      }
    }
    val orderBy = Params.get(Order.ORDER_STR)
    builder.orderBy(if (Strings.isEmpty(orderBy)) "courseTake.std.code desc" else orderBy)
    builder.limit(getPageLimit)
    builder
  }

  override def search(): String = {
    put("courseTakes", entityDao.search(getQueryBuilder))
    forward()
  }

  def changePayState(): String = {
    val courseTakes = getModels(classOf[CourseTake], getLongIds("courseTake"))
    val toChanges = CollectUtils.newArrayList()
    val paid = getBool("paid")
    for (courseTake <- courseTakes if null == courseTake.getBill) {
      courseTake.setPaid(paid)
      val student = entityDao.get(classOf[Student], courseTake.getStd.getId)
      toChanges.add(courseTake)
    }
    try {
      entityDao.saveOrUpdate(toChanges)
    } catch {
      case e: Exception => {
        logger.info("info.save.failure", e)
        return redirect("search", "info.save.failure")
      }
    }
    var msg = ""
    msg = if (courseTakes.isEmpty) "未找到匹配上课名单" else if (toChanges.size == courseTakes.size) "info.save.success" else "有" + (courseTakes.size - toChanges.size) + "条已生成订单记录不可更改状态"
    redirect("search", msg)
  }

  def syncBillState(): String = {
    val bills = entityDao.get(classOf[Bill], Array("semester", "feeType.id"), Array(putSemester(null), getLong("bill.feeType.id")))
    val billMap = CollectUtils.newHashMap()
    for (bill <- bills) {
      val paid = paymentService.checkBillOnPurpose(bill)
      if (paid && bill.getState.getId != PayState.PAID) {
        billMap.put(bill, true)
      } else if (!paid && bill.getState.getId == PayState.PAID) {
        billMap.put(bill, false)
      }
    }
    put("bills", billMap.keySet)
    put("billMap", billMap)
    forward()
  }

  override def info(): String = {
    val billId = getLongId("bill")
    if (null == billId) {
      return forwardError("error.model.id.needed")
    }
    val bill = entityDao.get(classOf[Bill], billId)
    put("bill", bill)
    if (PayState.CANCEL == bill.getState.getId) {
      put("courseTakes", Collections.emptyList())
    } else {
      put("courseTakes", entityDao.get(classOf[CourseTake], "bill", bill))
    }
    forward()
  }

  def orderList(): String = {
    val builder = OqlBuilder.from(classOf[Bill], "bill")
    populateConditions(builder)
    builder.where("bill.semester = :semester", putSemester(null))
    builder.where("bill.remark = :remark", "PAYFORRETAKE")
    val orderBy = get(Order.ORDER_STR)
    builder.orderBy(if (Strings.isEmpty(orderBy)) "bill.state.id,bill.createdAt desc" else orderBy)
      .limit(getPageLimit)
    put("bills", entityDao.search(builder))
    put("feeTypes", baseCodeService.getCodes(classOf[FeeType]))
    put("payStates", baseCodeService.getCodes(classOf[PayState]))
    forward()
  }

  override def save(): String = {
    val courseTakes = getModels(classOf[CourseTake], getLongIds("courseTake"))
    if (courseTakes.isEmpty) {
      return redirect("search", "请至少选择一条记录操作")
    }
    var student: Student = null
    for (courseTake <- courseTakes) {
      if (null != student && student == courseTake.getStd) {
        return redirect("search", "请选择同一学生的重修课程生成订单")
      }
      if (null != courseTake.getBill) {
        return redirect("search", "重复申请上课名单")
      }
      if (null == courseTake.getBill && courseTake.isPaid) {
        return redirect("search", "手动修改为'已缴费'学生不能生成订单")
      }
      student = courseTake.getStd
    }
    val semester = putSemester(null)
    val configs = retakeFeeConfigService.getOpenConfigs(getProject, semester)
    var config: RetakeFeeConfig = null
    if (configs.isEmpty) {
      return redirect("search", "当前时间没有重修缴费设置,请先进行配置再生成订单")
    } else {
      config = configs.get(0)
    }
    val context = BillGenContext.create(student, config.getFeeType, semester, retakeBillAmountCalculator)
    context.put("retakeCourseTakes", courseTakes).put("retakeFeeConfig", config)
      .setBillCodeGenerator(retakeBillCodeGenerator)
    val bill = billService.genBill(configs.get(0), context)
    try {
      billService.saveOrUpdate(bill, BillLogType.CREATED, courseTakes)
    } catch {
      case e: Exception => {
        logger.info("info.save.failure", e)
        return redirect("search", "info.save.failure")
      }
    }
    redirect("search", "info.save.success")
  }

  def updateOrderStatus(): String = {
    val bills = getModels(classOf[Bill], getLongIds("bill"))
    if (bills.isEmpty) {
      return redirect("orderList", "请至少选择一条操作")
    }
    val paid = getBool("paid")
    val cancelBills = CollectUtils.newArrayList()
    val paidBills = CollectUtils.newArrayList()
    val notSaveBills = CollectUtils.newArrayList()
    val cancelCourseTakes = CollectUtils.newArrayList()
    val paidCourseTakes = CollectUtils.newArrayList()
    val date = new Date()
    val user = entityDao.get(classOf[User], getUserId)
    val paidState = Model.newInstance(classOf[PayState], PayState.PAID)
    val cancelState = Model.newInstance(classOf[PayState], PayState.CANCEL)
    val courseTakes = entityDao.get(classOf[CourseTake], "bill", bills)
    val billCourseTakes = CollectUtils.newHashMap()
    for (courseTake <- courseTakes) {
      var takes = billCourseTakes.get(courseTake.getBill.getId)
      if (null == takes) {
        takes = new ArrayList[CourseTake]()
        billCourseTakes.put(courseTake.getBill.getId, takes)
      }
      takes.add(courseTake)
    }
    for (bill <- bills) {
      if (PayState.UNPAID == bill.getState.getId) {
        val students = entityDao.get(classOf[Student], "code", bill.getUsername)
        if (students.isEmpty) {
          //continue
        }
        bill.setUpdatedAt(date)
        val takes = billCourseTakes.get(bill.getId)
        for (courseTake <- takes) {
          courseTake.setUpdatedAt(date)
          courseTake.setPaid(paid)
          if (paid) {
            paidCourseTakes.add(courseTake)
          } else {
            courseTake.setBill(null)
            cancelCourseTakes.add(courseTake)
          }
        }
        if (paid) {
          bill.setState(paidState)
          val pay = new PayBean()
          pay.setBill(bill)
          pay.setCreateAt(date)
          pay.setPaid(bill.getAmount)
          pay.setCreator(user.getName)
          pay.setPayAt(date)
          pay.setPayType(Model.newInstance(classOf[PayType], PayType.CASH))
          bill.getPays.add(pay)
          paidBills.add(bill)
        } else {
          bill.setState(cancelState)
          cancelBills.add(bill)
        }
      } else {
        notSaveBills.add(bill)
      }
    }
    try {
      if (!paidBills.isEmpty) {
        billService.saveOrUpdate(paidBills, BillLogType.PAID_ADMIN, paidCourseTakes)
      }
      if (!cancelBills.isEmpty) {
        billService.cancel(cancelBills, cancelCourseTakes)
      }
    } catch {
      case e: Exception => return redirect("orderList", "info.save.failure")
    }
    val success = paidBills.size + cancelBills.size
    redirect("orderList", if (notSaveBills.isEmpty) "info.save.success" else "成功修改" + success + "条," + notSaveBills.size + "条已支付或已退订")
  }

  def cancelTest(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val bills = getModels(classOf[Bill], getLongIds("bill"))
    val date = new Date()
    val toSaveBills = CollectUtils.newArrayList()
    val notSaveList = CollectUtils.newArrayList()
    for (bill <- bills) {
      if (PayState.UNPAID == bill.getState.getId && user.getName == bill.getUsername) {
        bill.setState(Model.newInstance(classOf[PayState], PayState.CANCEL))
        bill.setUpdatedAt(date)
        toSaveBills.add(bill)
      } else {
        notSaveList.add(bill)
      }
    }
    try {
      entityDao.saveOrUpdate(toSaveBills)
    } catch {
      case e: Exception => return redirect("showBillTest", "info.delete.failure")
    }
    if (notSaveList.isEmpty) {
      return redirect("showBillTest", "info.delete.success")
    }
    redirect("showBillTest", "成功退订" + toSaveBills.size + "条;" + notSaveList.size + 
      "条已支付或已废弃,不可退订")
  }

  def showBillTest(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val builder = OqlBuilder.from(classOf[Bill], "bill")
    builder.where("bill.username = :username", user.getName)
    builder.where("bill.semester = :semester", putSemester(null))
    val orderBy = get(Order.ORDER_STR)
    builder.orderBy(if (Strings.isNotEmpty(orderBy)) orderBy else "bill.code desc")
      .limit(getPageLimit)
    val bills = entityDao.search(builder)
    val date = new Date()
    for (bill <- bills if PayState.UNPAID == bill.getState.getId if paymentService.checkBillOnPurpose(bill)) {
      bill.setPaid(bill.getAmount)
      bill.setUpdatedAt(date)
      bill.setState(entityDao.get(classOf[PayState], PayState.PAID))
      val pay = new PayBean()
      pay.setBill(bill)
      pay.setCreateAt(date)
      pay.setPaid(bill.getAmount)
      pay.setCreator(user.getName)
      pay.setPayType(Model.newInstance(classOf[PayType], PayType.EBANK))
      pay.setPayAt(date)
      bill.getPays.add(pay)
    }
    entityDao.saveOrUpdate(bills)
    put("bills", bills)
    val configs = entityDao.search(retakeFeeConfigService.getOpenConfigBuilder(getProject, putSemester(null)))
    if (!configs.isEmpty) {
      put("config", configs.get(0))
    }
    forward()
  }

  def genBillTest(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val configs = entityDao.search(retakeFeeConfigService.getOpenConfigBuilder(getProject, putSemester(null)))
    if (configs.isEmpty) {
      return forwardError("请先设置开关")
    }
    val config = configs.get(0)
    val bill = new BillBean()
    val date = new Date()
    bill.setCode(genBillCode(config.getFeeType, user))
    bill.setUsername(user.getName)
    bill.setPaid(0)
    val pay = new PayBean()
    pay.setPayType(Model.newInstance(classOf[PayType], PayType.CASH))
    pay.setCreateAt(date)
    pay.setCreator(user.getName)
    pay.setBill(bill)
    pay.setPaid(0)
    bill.setAmount(20)
    bill.setCreator(user.getName)
    bill.setCreatedAt(date)
    bill.setUpdatedAt(date)
    bill.setFeeType(config.getFeeType)
    bill.setFullname(user.getFullname)
    bill.setSemester(config.getSemester)
    bill.setState(Model.newInstance(classOf[PayState], PayState.UNPAID))
    bill.setPays(CollectUtils.newHashSet(pay))
    try {
      entityDao.saveOrUpdate(bill)
      redirect("showBillTest", "info.save.success")
    } catch {
      case e: Exception => {
        e.printStackTrace()
        redirect("showBillTest", "info.save.failure")
      }
    }
  }

  private def genBillCode(feeType: FeeType, user: User): String = {
    val hql = "select max(code) from org.openurp.edu.eams.fee.Bill bill " + 
      "where bill.code like :code"
    val code = user.getName + feeType.getId
    val query = OqlBuilder.hql(hql)
    query.param("code", code + "%")
    val maxCode = entityDao.uniqueResult(query)
    if (null == maxCode) {
      return code + "0001"
    }
    val maxCodeStr = maxCode.toString
    val orderCode = (java.lang.Integer.valueOf(maxCodeStr.substring(maxCodeStr.length - 4, maxCodeStr.length)) + 
      1) + 
      ""
    code + Strings.repeat("0", 4 - orderCode.length) + orderCode
  }

  def payTest(): String = {
    val user = entityDao.get(classOf[User], getUserId)
    val semester = putSemester(null)
    val project = getProject
    if (null == user) {
      return forwardError("没有权限")
    }
    val configs = entityDao.search(retakeFeeConfigService.getOpenConfigBuilder(project, semester))
    if (configs.isEmpty) {
      return redirect("showBillTest", "在线支付已关闭")
    }
    val billId = getLongId("bill")
    if (null == billId) {
      return redirect("showBillTest", "error.model.id.needed")
    }
    val bill = entityDao.get(classOf[Bill], billId)
    if (null == bill) {
      return redirect("showBillTest", "没有找到订单")
    }
    if (!bill.inPaymentTime()) {
      return redirect("showBillTest", "订单支付时间未开放或已结束")
    }
    if (PayState.UNPAID != bill.getState.getId) {
      return redirect("showBillTest", "该订单已支付或退订")
    }
    val feedBackUrl = getRequest.getRequestURL.toString
    val returnUrl = feedBackUrl.substring(0, feedBackUrl.indexOf(getRequest.getContextPath) + getRequest.getContextPath.length) + 
      "/payment.action?method=test"
    val context = PaymentContext.create()
    context.put("student", getLoginStudent)
    context.put("semester", semester)
    context.put("project", project)
    context.put("feeConfigs", retakeFeeConfigService.getOpenConfigs(project, semester))
    context.put("billId", bill.getId)
    context.put("remoteAddr", getRemoteAddr)
    context.put("returnUrl", returnUrl)
    context.setBill(bill)
    paymentService.getPaymentParams(context)
    put("payParams", paymentService.getPaymentParams(context))
    put("url", paymentService.getPaymentUrl(context))
    forward()
  }

  def setRetakeFeeConfigService(retakeFeeConfigService: RetakeFeeConfigService) {
    this.retakeFeeConfigService = retakeFeeConfigService
  }

  def setPaymentService(paymentService: PaymentService) {
    this.paymentService = paymentService
  }

  def orderCheckList(): String = {
    var status = getInt("status")
    if (status == null) {
      status = 1
    }
    val semester = putSemester(null)
    put("status", status)
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    status match {
      case 1 => 
        builder.where("courseTake.retakeFeePaid is true and courseTake.bill is not null")
        builder.where("courseTake.bill.semester = :semester", semester)
        builder.where("courseTake.bill.state.id <> :stateId", PayState.PAID)
        builder.limit(getPageLimit).orderBy(get(Order.ORDER_STR))
        put("courseTakes", entityDao.search(builder))

      case 2 => 
        builder.where("courseTake.retakeFeePaid is true and courseTake.bill is not null")
        builder.where("courseTake.bill.semester = :semester", semester)
        builder.join("courseTake.bill.pays", "pay")
        builder.where("pay.payType.id = :payTypeId", PayType.EBANK)
        builder.select("select distinct courseTake")
        var courseTakes = entityDao.search(builder)
        var showList = CollectUtils.newArrayList()
        for (courseTake <- courseTakes if !paymentService.checkBillOnPurpose(courseTake.getBill)) {
          showList.add(courseTake)
        }
        put("courseTakes", showList)

      case 3 => 
        var builder2 = OqlBuilder.from(classOf[Bill], "bill")
        builder2.where("bill.remark = :remark", "PAYFORRETAKE")
        builder2.where("bill.state.id = :stateId", PayState.PAID)
        builder2.where("not exists(from org.openurp.edu.teach.lesson.CourseTake take where take.bill is not null and take.bill = bill)")
        builder2.limit(getPageLimit).orderBy(get(Order.ORDER_STR))
        put("bills", entityDao.search(builder2))

    }
    forward()
  }

  def setRetakeBillCodeGenerator(billCodeGenerator: BillCodeGenerator) {
    this.retakeBillCodeGenerator = billCodeGenerator
  }

  def setBillService(billService: BillService) {
    this.billService = billService
  }

  def setRetakeBillAmountCalculator(retakeBillAmountCalculator: BillAmountCalculator) {
    this.retakeBillAmountCalculator = retakeBillAmountCalculator
  }
}
