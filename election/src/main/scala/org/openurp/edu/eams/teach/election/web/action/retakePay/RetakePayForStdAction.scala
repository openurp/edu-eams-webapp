package org.openurp.edu.eams.teach.election.web.action.retakePay

import java.util.Date



import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.data.model.Entity
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.fee.Bill
import org.openurp.edu.eams.fee.code.industry.PayState
import org.openurp.edu.eams.fee.model.BillLogBean.BillLogType
import org.openurp.edu.eams.fee.service.BillAmountCalculator
import org.openurp.edu.eams.fee.service.BillCodeGenerator
import org.openurp.edu.eams.fee.service.BillService
import org.openurp.edu.eams.fee.service.PaymentService
import org.openurp.edu.eams.fee.service.impl.BillGenContext
import org.openurp.edu.eams.fee.service.impl.PaymentContext
import org.openurp.edu.eams.fee.web.action.PaymentSupportAction
import org.openurp.edu.teach.code.CourseTakeType
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import org.openurp.edu.eams.teach.election.service.RetakeFeeCalculator
import org.openurp.edu.eams.teach.election.service.RetakeFeeConfigService
import org.openurp.edu.teach.lesson.CourseTake



class RetakePayForStdAction extends PaymentSupportAction {

  var retakeFeeConfigService: RetakeFeeConfigService = _

  var retakeBillCodeGenerator: BillCodeGenerator = _

  var retakeBillAmountCalculator: BillAmountCalculator = _

  var retakeFeeCalculator: RetakeFeeCalculator = _

  protected override def preparePaymentContext(context: PaymentContext) {
    val project = getProject
    val semester = putSemester(null)
    val billId = getLongId("bill")
    val remoteAddr = getRemoteAddr
    context.put("student", getLoginStudent)
    context.put("semester", semester)
    context.put("project", project)
    context.put("feeConfigs", retakeFeeConfigService.getOpenConfigs(project, semester))
    context.put("billId", billId)
    context.put("remoteAddr", remoteAddr)
    if (null != billId) {
      context.setBill(entityDao.get(classOf[Bill], billId))
    }
  }

  protected override def indexSetting() {
    val student = getLoginStudent
    if (null == student) {
      return
    }
    val semester = putSemester(null)
    val project = getProject
    paymentService.checkBillOnPurposeForRetakeFee(student, semester)
    val configs = retakeFeeConfigService.getOpenConfigs(project, semester)
    if (!configs.isEmpty) {
      put("config", configs.get(0))
    }
    put("retakeFeeCalculator", retakeFeeCalculator)
    put("courseTakes", retakeFeeConfigService.getRetakeCourseTakes(student, semester))
  }

  override def search(): String = {
    val student = getLoginStudent
    if (null == student) {
      return forwardError("没有权限")
    }
    val semester = putSemester(null)
    val project = getProject
    paymentService.checkBillOnPurposeForRetakeFee(student, semester)
    val builder = OqlBuilder.from(classOf[Bill], "bill")
    builder.where("bill.semester = :semester", semester)
    builder.where("bill.username = :username", student.getCode)
    builder.where("bill.remark = :remark", "PAYFORRETAKE")
    builder.where("bill.state.id <> :stateId", PayState.CANCEL)
    val orderBy = get(Order.ORDER_STR)
    builder.orderBy(if (Strings.isEmpty(orderBy)) "bill.code" else orderBy)
    val configs = retakeFeeConfigService.getOpenConfigs(project, semester)
    if (!configs.isEmpty) {
      put("config", configs.get(0))
    }
    put("bills", entityDao.search(builder))
    put("now", new Date())
    forward()
  }

  override def info(): String = {
    val billId = getLongId("bill")
    if (null == billId) {
      return redirect("search", "error.model.id.needed")
    }
    val bill = entityDao.get(classOf[Bill].getName, billId)
    put("bill", bill)
    put("courseTakes", entityDao.get(classOf[CourseTake], "bill.id", billId))
    forward()
  }

  override def save(): String = {
    val student = getLoginStudent
    val semester = putSemester(null)
    val project = getProject
    if (null == student) {
      return forwardError("没有权限")
    }
    val configs = retakeFeeConfigService.getOpenConfigs(project, semester)
    if (configs.isEmpty) {
      return redirect("index", "在线支付已关闭")
    }
    val builder = OqlBuilder.from(classOf[CourseTake], "courseTake")
    builder.where("courseTake.courseTakeType.id=:retakeTypeId", CourseTakeType.RESTUDY)
    builder.where("courseTake.id in(:courseTakeId)", getLongIds("courseTake"))
    builder.orderBy("courseTake.createdAt asc")
    var courseTakes: List[CourseTake] = null
    try {
      courseTakes = entityDao.search(builder)
    } catch {
      case e: Exception => {
        logger.info("info.save.failure", e)
        return redirect("info.save.failure")
      }
    }
    if (courseTakes.isEmpty) {
      return redirect("search", "error.model.ids.needed")
    }
    val config = configs.get(0)
    val context = BillGenContext.create(student, config.getFeeType, semester, retakeBillAmountCalculator)
      .setRemark("PAYFORRETAKE")
    context.put("retakeCourseTakes", courseTakes).put("startPayDurationAt", courseTakes.get(0).getCreatedAt)
    context.put("retakeFeeConfig", config).setBillCodeGenerator(retakeBillCodeGenerator)
    val bill = billService.genBill(config, context)
    for (courseTake <- courseTakes if null != courseTake.getBill) {
      return redirect("index", "重复申请上课名单")
    }
    for (courseTake <- courseTakes) {
      courseTake.setBill(bill)
    }
    try {
      billService.saveOrUpdate(bill, BillLogType.CREATED, courseTakes)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        logger.info("save failure", e)
        return redirect("index", "info.save.failure")
      }
    }
    redirect("payment", null, "bill.id=" + bill.id)
  }

  override def remove(): String = {
    val student = getLoginStudent
    if (null == student) {
      return forwardError("没有权限")
    }
    val bills = getModels(classOf[Bill], getLongIds("bill"))
    val courseTakes = entityDao.get(classOf[CourseTake], "bill", bills)
    val saveBills = Collections.newMap[Any]
    val cancelBills = Collections.newSet[Any]
    val cancelCourseTakes = Collections.newSet[Any]
    val paidBills = Collections.newSet[Any]
    val paidCourseTakes = Collections.newSet[Any]
    val saveEntities = Collections.newSet[Any]
    val date = new Date()
    for (courseTake <- courseTakes) {
      var unPaid: java.lang.Boolean = null
      var bill = courseTake.getBill
      val saveBill = saveBills.get(bill.id)
      if (null != saveBill) {
        bill = saveBill
        val state = bill.getState.id
        unPaid = PayState.UNPAID == state || PayState.CANCEL == state
      } else {
        try {
          unPaid = if (PayState.PAID == bill.getState.id || PayState.COMPLETED == bill.getState.id) false else !paymentService.checkBillOnPurpose(bill)
        } catch {
          case e: Exception => //continue
        }
      }
      if (student.getCode == bill.getUsername) {
        if (null == saveBill) {
          if (unPaid) {
            bill.setState(Model.newInstance(classOf[PayState], PayState.CANCEL))
            courseTake.setBill(null)
            cancelBills.add(bill)
          } else {
            bill.setState(Model.newInstance(classOf[PayState], PayState.PAID))
            bill.setPaid(bill.getAmount)
            paidBills.add(bill)
          }
          bill.setUpdatedAt(date)
          saveBills.put(bill.id, bill)
        }
        courseTake.setPaid(!unPaid)
        courseTake.setUpdatedAt(date)
        if (unPaid) {
          courseTake.setBill(null)
          cancelCourseTakes.add(courseTake)
        } else {
          courseTake.setBill(bill)
          paidCourseTakes.add(courseTake)
        }
        saveEntities.add(courseTake)
      }
    }
    try {
      billService.cancel(cancelBills, cancelCourseTakes)
      billService.saveOrUpdate(paidBills, BillLogType.PAID_SEARCH, paidCourseTakes)
    } catch {
      case e: Exception => {
        logger.info("info.delete.failure", e)
        return redirect("search", "info.delete.failure")
      }
    }
    val successSize = saveBills.size
    val notSaveSize = bills.size - successSize
    if (notSaveSize == 0) {
      return redirect("search", "info.delete.success")
    }
    redirect("search", "成功退订" + successSize + "条;" + notSaveSize + "条已支付,不可退订")
  }

}
