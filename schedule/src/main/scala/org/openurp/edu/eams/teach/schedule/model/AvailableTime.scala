package org.openurp.edu.eams.teach.schedule.model

import java.io.Serializable
import javax.persistence.Entity
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import org.beangle.commons.entity.pojo.LongIdObject
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.openurp.base.Semester
import org.openurp.edu.eams.base.util.WeekDay
import org.openurp.edu.eams.base.util.WeekDays
import org.beangle.commons.lang.time.WeekDays._
import AvailableTime._




object AvailableTime {

  var STRUCTS: String = "14:5,10"

  var MAX_LENGTH: Int = Semester.MAXUNITS * WeekDays.MAX

  var PRIORITIES: Array[String] = Array("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")

  var commonTeacherAvailTime: String = commonRoomAvailTime

  var commonRoomAvailTime: String = Strings.repeat("1", WeekDays.MAX * Semester.MAXUNITS)

  var commonTaskAvailTime: String = commonRoomAvailTime

  var EMPTY: String = Strings.repeat("0", WeekDays.MAX * Semester.MAXUNITS)
}

@SerialVersionUID(-3056451280716927057L)
@Entity(name = "org.openurp.edu.eams.teach.schedule.model.AvailableTime")
class AvailableTime extends LongIdObject with Serializable with Cloneable {

  def this(available: String) {
    this()
    this.available = available
  }

  override def clone(): AvailableTime = {
    val time = new AvailableTime()
    time.setAvailable(new String(this.available))
    time.setRemark(this.remark)
    time.setStruct(this.struct)
    time.setUnits(this.units)
    time
  }

  @NotNull
  @Size(max = 200)
  
  var available: String = _

  @Size(max = 200)
  
  var remark: String = _

  @NotNull
  @Size(max = 20)
  
  var struct: String = AvailableTime.STRUCTS

  private var units: Int = _

  def abbreviate(textResource: TextResource): String = {
    if (Strings.isEmpty(available)) return ""
    if (available.indexOf("0") == -1) {
      return textResource.getText("time.week")
    }
    val sb = new StringBuffer()
    val segments = Strings.split(Strings.substringAfter(struct, ":"), ",")
    val segs = Array.ofDim[Int](segments.length)
    for (i <- 0 until segments.length) {
      segs(i) = Numbers.toInt(segments(i))
    }
    val allZeroUnit = Strings.repeat("0", units)
    for (i <- 0 until WeekDays.All.length) {
      val dayAvalible = available.substring(i * units, i * units + units)
      if (dayAvalible == allZeroUnit) //continue
      sb.append(textResource.getText(WeekDay.getDay(i + 1).getI18nKey))
      if (!Strings.contains(dayAvalible, '0')) {
        sb.append(" ")
        //continue
      } else {
        sb.append("(")
        val morning = abbreviateUnit(textResource.getText("time.morning"), dayAvalible.substring(0, segs(0)), 
          0)
        val afternoon = abbreviateUnit(textResource.getText("time.afternoon"), dayAvalible.substring(segs(0), 
          segs(1)), segs(0))
        val evening = abbreviateUnit(textResource.getText("time.evening"), dayAvalible.substring(segs(1), 
          units), segs(1))
        var hasOne = false
        if (!Strings.isEmpty(morning)) {
          hasOne = true
          sb.append(morning)
        }
        if (!Strings.isEmpty(afternoon)) {
          if (hasOne) sb.append(" ")
          sb.append(afternoon)
          hasOne = true
        }
        if (!Strings.isEmpty(evening)) {
          if (hasOne) sb.append(" ")
          sb.append(evening)
        }
        sb.append(") ")
      }
    }
    sb.toString
  }

  def abbreviate(): String = {
    if (Strings.isEmpty(available)) return ""
    val sb = new StringBuffer()
    val segments = Strings.split(Strings.substringAfter(struct, ":"), ",")
    val segs = Array.ofDim[Int](segments.length)
    for (i <- 0 until segments.length) {
      segs(i) = Numbers.toInt(segments(i))
    }
    val allZeroUnit = Strings.repeat("0", units)
    for (i <- 0 until WeekDays.All.length) {
      val dayAvalible = available.substring(i * units, i * units + units)
      if (dayAvalible == allZeroUnit) //continue
      sb.append(WeekDays.All(i).getName)
      if (!Strings.contains(dayAvalible, '0')) {
        sb.append(" ")
        //continue
      } else {
        sb.append("(")
        val morning = abbreviateUnit("上午", dayAvalible.substring(0, segs(0)), 0)
        val afternoon = abbreviateUnit("下午", dayAvalible.substring(segs(0), segs(1)), segs(0))
        val evening = abbreviateUnit("晚上", dayAvalible.substring(segs(1), units), segs(1))
        var hasOne = false
        if (!Strings.isEmpty(morning)) {
          hasOne = true
          sb.append(morning)
        }
        if (!Strings.isEmpty(afternoon)) {
          if (hasOne) sb.append(" ")
          sb.append(afternoon)
          hasOne = true
        }
        if (!Strings.isEmpty(evening)) {
          if (hasOne) sb.append(" ")
          sb.append(evening)
        }
        sb.append(") ")
      }
    }
    sb.toString
  }

  private def abbreviateUnit(segInfo: String, units: String, start: Int): String = {
    val sb = new StringBuffer()
    if (units == Strings.repeat("0", units.length)) return "" else sb.append(segInfo)
    if (Strings.contains(units, "0")) {
      for (i <- 0 until units.length if units.charAt(i) != '0') sb.append(start + i + 1).append(",")
      sb.deleteCharAt(sb.length - 1)
    }
    sb.toString
  }

  def setAvailableFor(week: Int, unit: Int, available: Boolean) {
    val sb = new StringBuffer(this.available)
    sb.setCharAt((week - 1) * getUnits + unit - 1, if (available) '1' else '0')
    setAvailable(sb.toString)
  }

  def mergeWith(time: AvailableTime) {
    if (null != time && Strings.isNotEmpty(time.getAvailable)) {
      val buffer = new StringBuffer()
      for (i <- 0 until time.getAvailable.length) {
        val own = getAvailable.charAt(i) - 48
        val other = time.getAvailable.charAt(i) - 48
        if (own + other > 9) buffer.append("9") else buffer.append(String.valueOf(own + other))
      }
      setAvailable(buffer.toString)
    }
  }

  def detachWith(time: AvailableTime) {
    if (null != time && Strings.isNotEmpty(time.getAvailable)) {
      val buffer = new StringBuffer()
      for (i <- 0 until time.getAvailable.length) {
        val own = getAvailable.charAt(i) - 48
        val other = time.getAvailable.charAt(i) - 48
        if (own - other < 0) buffer.append("0") else buffer.append(String.valueOf(own - other))
      }
      setAvailable(buffer.toString)
    }
  }

  def isValid(): Boolean = {
    if (Strings.isEmpty(getAvailable)) return false
    if (getAvailable.length != WeekDays.MAX * getUnits) return false
    for (i <- 0 until WeekDays.MAX * getUnits if getAvailable.charAt(i) < '0' || getAvailable.charAt(i) > '9') return false
    true
  }

  def setStruct(struct: String) {
    this.struct = struct
    if (Strings.isNotEmpty(struct)) {
      units = Numbers.toInt(Strings.substringBefore(struct, ":"))
    }
  }

  def getUnits(): Int = {
    if (0 == units) {
      Semester.MAXUNITS
    } else {
      units
    }
  }

  def setUnits(units: Int) {
    this.units = units
  }

  override def toString(): String = {
    Objects.toStringBuilder(this).add("id", this.id).add("available", this.available)
      .toString
  }
}
