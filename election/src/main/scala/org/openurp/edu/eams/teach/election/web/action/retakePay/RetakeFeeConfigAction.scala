package org.openurp.edu.eams.teach.election.web.action.retakePay


import java.util.Date

import java.util.concurrent.YearWeekTime
import org.beangle.data.model.Entity
import org.openurp.edu.eams.fee.code.school.FeeType
import org.openurp.edu.eams.fee.service.PaymentService
import org.openurp.edu.eams.teach.election.RetakeFeeConfig
import org.openurp.edu.eams.teach.election.service.RetakeFeeConfigService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class RetakeFeeConfigAction extends SemesterSupportAction {

  var retakeFeeConfigService: RetakeFeeConfigService = _

  var paymentService: PaymentService = _

  protected override def getEntityName(): String = classOf[RetakeFeeConfig].getName

  protected override def indexSetting() {
    put("configs", retakeFeeConfigService.getConfigs(getProject, putSemester(null)))
  }

  protected override def editSetting(entity: Entity[_]) {
    put("project", getProject)
    put("semester", putSemester(null))
    put("feeTypes", baseCodeService.getCodes(classOf[FeeType]))
    put("timeUnits", YearWeekTime.values)
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
    val fromYearWeekTime = YearWeekTime.valueOf(fromUnit)
    val toYearWeekTime = YearWeekTime.valueOf(toUnit)
    toYearWeekTime match {
      case DAYS => fromYearWeekTime.toDays(duration)
      case HOURS => fromYearWeekTime.toHours(duration)
      case MINUTES => fromYearWeekTime.toMinutes(duration)
      case SECONDS => fromYearWeekTime.toSeconds(duration)
      case MICROSECONDS => fromYearWeekTime.toMicros(duration)
      case NANOSECONDS => fromYearWeekTime.toNanos(duration)
      case _ => fromYearWeekTime.toMillis(duration)
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
    val payDuration = calPayDuration(get("timeUnit"), YearWeekTime.MILLISECONDS.toString, getLong("duration"))
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

  protected override def removeAndForward(entities: Iterable[_]): String = {
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
}
