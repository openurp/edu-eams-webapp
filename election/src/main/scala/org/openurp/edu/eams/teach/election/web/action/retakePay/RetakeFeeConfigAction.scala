package org.openurp.edu.eams.teach.election.web.action.retakePay

import java.util.Collection
import java.util.Date
import java.util.HashMap
import java.util.concurrent.TimeUnit
import org.beangle.commons.entity.Entity
import org.openurp.edu.eams.fee.code.school.FeeType
import org.openurp.edu.eams.fee.service.PaymentService
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import org.openurp.edu.eams.teach.election.service.RetakeFeeConfigService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction

import scala.collection.JavaConversions._

class RetakeFeeConfigAction extends SemesterSupportAction {

  private var retakeFeeConfigService: RetakeFeeConfigService = _

  private var paymentService: PaymentService = _

  protected override def getEntityName(): String = classOf[RetakeFeeConfig].getName

  protected override def indexSetting() {
    put("configs", retakeFeeConfigService.getConfigs(getProject, putSemester(null)))
  }

  protected override def editSetting(entity: Entity[_]) {
    put("project", getProject)
    put("semester", putSemester(null))
    put("feeTypes", baseCodeService.getCodes(classOf[FeeType]))
    put("timeUnits", TimeUnit.values)
  }

  def calPayDuration(): String = {
    val fromUnit = get("fromUnit")
    val toUnit = get("toUnit")
    var duration = getLong("duration")
    if (fromUnit != toUnit) {
      duration = calPayDuration(get("fromUnit"), get("toUnit"), duration)
    }
    put("duration", duration)
    forward()
  }

  private def calPayDuration(fromUnit: String, toUnit: String, duration: java.lang.Long): java.lang.Long = {
    if (null == duration) {
      return duration
    }
    val fromTimeUnit = TimeUnit.valueOf(fromUnit)
    val toTimeUnit = TimeUnit.valueOf(toUnit)
    toTimeUnit match {
      case DAYS => fromTimeUnit.toDays(duration)
      case HOURS => fromTimeUnit.toHours(duration)
      case MINUTES => fromTimeUnit.toMinutes(duration)
      case SECONDS => fromTimeUnit.toSeconds(duration)
      case MICROSECONDS => fromTimeUnit.toMicros(duration)
      case NANOSECONDS => fromTimeUnit.toNanos(duration)
      case _ => fromTimeUnit.toMillis(duration)
    }
  }

  protected def saveAndForward(entity: Entity[_]): String = {
    val config = entity.asInstanceOf[RetakeFeeConfig]
    if (config.isTransient) {
      val persistedConfig = retakeFeeConfigService.getConfig(config)
      if (null != persistedConfig) {
        return redirect("index", "设置的开放时间段已存在" + persistedConfig.getFeeType.getName + "的缴费设置")
      }
    }
    val pricePerCredit = getFloat("pricePerCredit")
    if (pricePerCredit != null) {
      config.setPricePerCredit((pricePerCredit * 100).toInt)
    } else {
      config.setPricePerCredit(0)
    }
    val date = new Date()
    if (!config.isPersisted) {
      config.setCreatedAt(date)
    }
    config.setUpdatedAt(date)
    val payDuration = calPayDuration(get("timeUnit"), TimeUnit.MILLISECONDS.toString, getLong("duration"))
    if (payDuration > 0) {
      config.setPayDuration(payDuration)
    }
    try {
      retakeFeeConfigService.saveOrUpdate(config)
      redirect("index", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("info.save.failure", e)
        redirect("index", "info.save.failure")
      }
    }
  }

  protected override def removeAndForward(entities: Collection[_]): String = {
    try {
      remove(entities)
    } catch {
      case e: Exception => {
        logger.info("removeAndForwad failure", e)
        return redirect("index", "info.delete.failure")
      }
    }
    redirect("index", "info.remove.success")
  }

  def queryFeeTypeCodes(): String = {
    put("feeTypeCodes", paymentService.queryFeeTypeCodes(new HashMap[String, Any]()))
    forward()
  }

  def setRetakeFeeConfigService(retakeFeeConfigService: RetakeFeeConfigService) {
    this.retakeFeeConfigService = retakeFeeConfigService
  }

  def setPaymentService(paymentService: PaymentService) {
    this.paymentService = paymentService
  }
}
