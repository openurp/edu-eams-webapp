package org.openurp.edu.eams.web.view.component

import java.math.BigDecimal

import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.ClosingUIBean
import com.opensymphony.xwork2.util.ValueStack




class NumRange(stack: ValueStack) extends ClosingUIBean(stack) {

  
  var label: String = _

  
  var name: String = _

  
  var comment: String = _

  
  var required: String = _

  
  var format: String = _

  private var minRange: AnyRef = _

  private var maxRange: AnyRef = _

  
  var minVal: AnyRef = _

  
  var maxVal: AnyRef = _

  
  var debug: AnyRef = _

  
  var minField: NumTextfield = _

  
  var maxField: NumTextfield = _

  protected override def evaluateParams() {
    val nameArray = Strings.split(name, ',')
    val requiredArray = Strings.split(required, ',')
    val commentArray = Strings.split(comment, ',')
    val labelArray = Strings.split(label, ',')
    val formatArray = Strings.split(format, ",")
    minField = new NumTextfield(stack)
    maxField = new NumTextfield(stack)
    minField.setValue(minVal)
    maxField.setValue(maxVal)
    if (nameArray != null) {
      if (nameArray.length == 1) {
        minField.setName(name + "Min")
        maxField.setName(name + "Max")
      } else {
        minField.setName(nameArray(0))
        maxField.setName(nameArray(1))
      }
    }
    if (requiredArray != null) {
      if (nameArray.length == 1) {
        minField.setRequired(required)
        maxField.setRequired(required)
      } else {
        minField.setRequired(requiredArray(0))
        maxField.setRequired(requiredArray(1))
      }
    }
    if (commentArray != null) {
      if (nameArray.length == 1) {
        minField.setRequired(comment)
        maxField.setRequired(comment)
      } else {
        minField.setRequired(commentArray(0))
        maxField.setRequired(commentArray(1))
      }
    }
    if (formatArray != null) {
      if (nameArray.length == 1) {
        minField.setRequired(format)
        maxField.setRequired(format)
      } else {
        minField.setRequired(formatArray(0))
        maxField.setRequired(formatArray(1))
      }
    }
    if (labelArray != null) {
      if (labelArray.length == 1) {
        minField.setLabel(label)
      } else {
        minField.setLabel(labelArray(0))
        maxField.setLabel(labelArray(1))
      }
    }
    minField.setTitle(minField.getLabel)
    maxField.setTitle(maxField.getLabel)
    minField.evalParams()
    maxField.evalParams()
    for ((key, value) <- parameters) {
      val key = key
      val paramVals = value.toString.split(",")
      if (paramVals.length == 1) {
        minField.getParameters.put(key, value)
        maxField.getParameters.put(key, value)
      } else {
        minField.getParameters.put(key, paramVals(0))
        maxField.getParameters.put(key, paramVals(1))
      }
    }
    if (minRange != null) {
      if (minRange.isInstanceOf[Number]) {
        minField.setMin(minRange.toString)
        maxField.setMin(minRange.toString)
      }
    } else {
      maxField.setMin("#" + minField.id)
    }
    if (maxRange != null) {
      if (maxRange.isInstanceOf[Number]) {
        minField.setMax(maxRange.toString)
        maxField.setMax(maxRange.toString)
      }
    } else {
      minField.setMax("#" + maxField.id)
    }
  }

  def setMinVal(minVal: AnyRef) {
    if (null != minVal) {
      if (Numbers.isDigits(minVal.toString)) {
        this.minVal = new BigDecimal(minVal.toString)
      }
    }
  }

  def setMaxVal(maxVal: AnyRef) {
    if (null != maxVal) {
      if (Numbers.isDigits(maxVal.toString)) {
        this.maxVal = new BigDecimal(maxVal.toString)
      }
    }
  }

  def setMinRange(minRange: AnyRef) {
    this.minRange = minRange
  }

  def setMaxRange(maxRange: AnyRef) {
    this.maxRange = maxRange
  }
}
