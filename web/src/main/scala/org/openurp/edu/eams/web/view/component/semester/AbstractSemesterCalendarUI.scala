package org.openurp.edu.eams.web.view.component.semester

import java.io.InputStream
import java.lang.reflect.Method
import java.util.Collection
import java.util.Collections
import java.util.Comparator
import java.util.List
import java.util.Map
import java.util.Map.Entry
import java.util.Properties
import java.util.TreeMap
import java.util.concurrent.TimeUnit
import org.apache.commons.beanutils.BeanUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.beangle.struts2.view.component.Form
import org.beangle.struts2.view.component.UIBean
import org.beangle.struts2.view.template.Theme
import org.openurp.edu.eams.base.Semester
import org.openurp.edu.eams.web.view.component.semester.ui.SemesterCalendarUI
import org.openurp.edu.eams.web.view.component.semester.ui.SemesterUIFactory
import com.opensymphony.xwork2.util.ValueStack
import freemarker.ext.beans.HashAdapter
import Rule._
import AbstractSemesterCalendarUI._

import scala.collection.JavaConversions._

object AbstractSemesterCalendarUI {

  val config = new SemesterCalendarConfig()

  object Rule {

    def getRule(method: Method, params: Array[Any]): Rule = new Rule(method, params)

    def getRules(rulesStr: String, paramTypeMap: Map[String, Class[_]]): List[Rule] = {
      val result = CollectUtils.newArrayList()
      val rules = rulesStr.split(":")
      for (s <- rules) {
        val entry = s.substring(0, s.length - 1).split("\\(")
        val name = entry(0)
        val paramsStr = entry(1).split(",")
        val c = paramTypeMap.get(name)
        var params: Array[Any] = null
        var parameterTypes: Array[Class[_]] = null
        if (null == c) {
          parameterTypes = Array.ofDim[Class[_]](0)
          params = paramsStr
        } else if (classOf[Int] == c) {
          parameterTypes = Array.ofDim[Class[_]](paramsStr.length)
          params = Array.ofDim[Integer](paramsStr.length)
          for (i <- 0 until paramsStr.length) {
            parameterTypes(i) = c
            params(i) = java.lang.Integer.valueOf(paramsStr(i))
          }
        } else {
          parameterTypes = Array.ofDim[Class[_]](paramsStr.length)
          for (i <- 0 until parameterTypes.length) {
            parameterTypes(i) = c
          }
          params = paramsStr
        }
        val m = classOf[String].getDeclaredMethod(name, parameterTypes)
        result.add(Rule.getRule(m, params))
      }
      result
    }
  }

  private class Rule(private var method: Method, private var params: Array[Any])
      {

    def invoke(obj: AnyRef): String = this.method.invoke(obj, params) + ""
  }

  private class SemesterCalendarConfig private () {

    private var defaultUiType: String = ""

    private var timeToLiveMills: Long = _

    private var loadAt: Long = -1

    paramTypeMap.put("concat", classOf[String])

    paramTypeMap.put("replace", classOf[CharSequence])

    paramTypeMap.put("replaceAll", classOf[String])

    paramTypeMap.put("replaceFirst", classOf[String])

    paramTypeMap.put("substring", classOf[Int])

    paramTypeMap.put("subSequence", classOf[Int])

    paramTypeMap.put("charAt", classOf[Int])

    protected var paramTypeMap: Map[String, Class[_]] = CollectUtils.newHashMap()

    private def updateConfig(timeToLiveMills: java.lang.Long, defaultUiType: String) {
      if (null == timeToLiveMills) {
        timeToLiveMills = TimeUnit.MINUTES.toMillis(30)
      }
      this.timeToLiveMills = timeToLiveMills
      if (null == defaultUiType) {
        this.defaultUiType = ""
      } else {
        try {
          SemesterUIFactory.get(defaultUiType)
          this.defaultUiType = defaultUiType
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
      loadAt = System.currentTimeMillis()
    }

    private def isExpired(): Boolean = {
      System.currentTimeMillis() - (loadAt + timeToLiveMills) > 
        0
    }
  }
}

abstract class AbstractSemesterCalendarUI(stack: ValueStack) extends UIBean(stack) {

  protected var name: String = _

  protected var label: String = _

  protected var title: String = _

  protected var check: String = _

  protected var multi: String = _

  protected var format: String = _

  protected var uiType: String = _

  protected var indexes: List[Integer] = CollectUtils.newArrayList()

  protected var tRules: List[Rule] = CollectUtils.newArrayList()

  protected var yRules: List[Rule] = CollectUtils.newArrayList()

  protected var onChange: String = _

  protected var onClick: String = _

  protected var onKeyup: String = _

  protected var onKeypress: String = _

  protected var onKeydown: String = _

  protected var onFocus: String = _

  protected var onBlur: String = _

  protected var beforeInit: String = _

  protected var initCallback: String = _

  protected var empty: AnyRef = _

  protected var required: AnyRef = _

  protected var value: AnyRef = _

  protected var items: AnyRef = _

  protected var yearRules: AnyRef = _

  protected var termRules: AnyRef = _

  protected var valueIndex: Int = _

  protected var termIndex: Int = _

  protected var defaultValue: Semester = _

  protected var semesterTree: Map[String, List[Semester]] = new TreeMap[String, List[Semester]](new Comparator[String]() {

    def compare(o1: String, o2: String): Int = {
      if (o1 == o2) {
        return 0
      }
      if (o1 == "") {
        return -1
      }
      if (o2 == "") {
        return 1
      }
      o1.compareTo(o2)
    }
  })

  try {
    loadProperties()
  } catch {
    case e: Exception => e.printStackTrace()
  }

  protected override def evaluateParams() {
    if (null == this.id) generateIdIfEmpty()
    if (null != label) label = getText(label, label)
    title = if (null != title) getText(title) else label
    if (null != yearRules) {
      yRules = Rule.getRules(yearRules.asInstanceOf[String], config.paramTypeMap)
    }
    if (null != termRules) {
      tRules.addAll(Rule.getRules(termRules.asInstanceOf[String], config.paramTypeMap))
    }
    required = "true" == required + ""
    empty = "false" != empty + ""
    var semesterCalendarUI: SemesterCalendarUI = null
    if (Strings.isBlank(uiType)) {
      uiType = config.defaultUiType
    } else {
      semesterCalendarUI = SemesterUIFactory.get(uiType)
    }
    if (null != items) {
      if (items.isInstanceOf[Collection[_]]) {
        val c = CollectUtils.newArrayList(items.asInstanceOf[Collection[Semester]])
        Collections.sort(c)
        val termFormat = null != format && format.contains("T")
        val yearFormat = null != format && format.matches("^yy(-T)?$")
        val formatIsBlank = Strings.isBlank(format)
        if (!formatIsBlank && null != value) {
          var colneValue: Semester = null
          colneValue = (value = BeanUtils.cloneBean(value)).asInstanceOf[Semester]
          if (yearFormat) {
            colneValue.setSchoolYear(editSchoolYear(colneValue.getSchoolYear))
          }
          if (termFormat) {
            colneValue.setName(editTerm(colneValue.getName))
          }
        }
        for (semester <- c) {
          var colneSemester: Semester = null
          colneSemester = if (termFormat) BeanUtils.cloneBean(semester).asInstanceOf[Semester] else semester
          var key = ""
          if (!formatIsBlank) {
            key += if (yearFormat) editSchoolYear(colneSemester.getSchoolYear) else colneSemester.getSchoolYear
            if (termFormat) {
              colneSemester.setName(editTerm(colneSemester.getName))
            }
          } else {
            key += colneSemester.getSchoolYear
          }
          var terms = semesterTree.get(key)
          if (null == terms) {
            terms = CollectUtils.newArrayList()
          }
          semesterTree.put(key, terms)
          terms.add(colneSemester)
        }
      }
    }
    if (null != semesterCalendarUI) {
      val newItems = semesterCalendarUI.adapteItems(semesterTree)
      if (null != newItems) {
        items = newItems
      }
    }
    if (null != value) {
      if (value.isInstanceOf[Semester]) {
        val semester = value.asInstanceOf[Semester]
        listTree: for (schoolYear <- semesterTree.keySet) {
          if (schoolYear == semester.getSchoolYear) {
            val semesters = semesterTree.get(schoolYear)
            for (semester2 <- semesters) {
              if (semester == semester2) {
                defaultValue = semester2
                //break
              }
              termIndex += 1
            }
          }
          valueIndex += 1
        }
      } else if (value.isInstanceOf[HashAdapter]) {
        val semester = value.asInstanceOf[HashAdapter]
        listHashAdapter: for (schoolYear <- semesterTree.keySet) {
          if (schoolYear == semester.get("schoolYear")) {
            val semesters = semesterTree.get(schoolYear)
            for (semester2 <- semesters) {
              if (semester == semester2) {
                defaultValue = semester2
                //break
              }
              termIndex += 1
            }
          }
          valueIndex += 1
        }
      }
    } else {
      if (false == empty) {
        valueIndex = 0
        termIndex = 0
        if (!semesterTree.entrySet().isEmpty) {
          defaultValue = semesterTree.entrySet().iterator().next().getValue.get(0)
        }
      } else {
        valueIndex = -1
        termIndex = -1
      }
    }
    val myform = findAncestor(classOf[Form])
    if (null != myform) {
      if ("true" == required) myform.addCheck(id, "require()")
      if (null != check) myform.addCheck(id, check)
    }
    if (Strings.isNotBlank(uiType)) {
      SemesterUIFactory.get(uiType)
    }
  }

  private def editSchoolYear(schoolYear: String): String = {
    var j = 0
    val schoolYearBuilder = new StringBuilder()
    for (i <- 0 until schoolYear.length) {
      if (j < indexes.size) {
        if (i == indexes.get(j)) {
          i += 2
          j += 1
        }
        schoolYearBuilder.append(schoolYear.charAt(i))
      } else {
        schoolYearBuilder.append(schoolYear.charAt(i))
      }
    }
    var result = schoolYearBuilder.toString
    for (rule <- yRules) {
      result = rule.invoke(result)
    }
    result
  }

  private def editTerm(term: String): String = {
    for (rule <- tRules) {
      term = rule.invoke(term)
    }
    term
  }

  private def loadProperties() {
    if (!config.isExpired) {
      return
    }
    val properties = new Properties()
    var is = getClass.getResourceAsStream("/eams-ui-default.properties")
    if (null != is) {
      properties.load(is)
    }
    is = getClass.getResourceAsStream("/eams-ui.properties")
    if (null != is) {
      properties.load(is)
    }
    var timeToLiveMills: java.lang.Long = null
    var defaultUiType: String = null
    for ((key, value) <- properties) {
      val key = key.asInstanceOf[String]
      val value = value.asInstanceOf[String]
      if (key == "semesterCalendar.properties.timeToLiveSeconds") {
        timeToLiveMills = java.lang.Long.parseLong(value.trim()) * 1000
      } else if (key == "semesterCalendar.type") {
        defaultUiType = value.trim()
      } else if (key == "semesterCalendar.year.indexes") {
        val indexes = value.split(",")
        for (index <- indexes) {
          this.indexes.add(java.lang.Integer.valueOf(index))
        }
      } else if (key.contains("semesterCalendar.term.method.")) {
        val name = key.replace("semesterCalendar.term.method.", "")
        val paramsStr = value.split(",")
        val clazz = config.paramTypeMap.get(name)
        var params: Array[Any] = null
        var parameterTypes: Array[Class[_]] = null
        if (null == clazz) {
          parameterTypes = Array.ofDim[Class[_]](0)
          params = paramsStr
        } else if (classOf[Int] == clazz) {
          parameterTypes = Array.ofDim[Class[_]](paramsStr.length)
          params = Array.ofDim[Integer](paramsStr.length)
          for (i <- 0 until paramsStr.length) {
            parameterTypes(i) = classOf[Int]
            params(i) = java.lang.Integer.valueOf(paramsStr(i))
          }
        } else {
          parameterTypes = Array.ofDim[Class[_]](paramsStr.length)
          for (i <- 0 until parameterTypes.length) {
            parameterTypes(i) = classOf[String]
          }
          params = paramsStr
        }
        val m = classOf[String].getDeclaredMethod(name, parameterTypes)
        tRules.add(Rule.getRule(m, params))
      }
    }
    config.updateConfig(timeToLiveMills, defaultUiType)
  }

  def isEmptyTree(): Boolean = {
    if (null == items) {
      return true
    }
    if (items.isInstanceOf[Collection[_]]) {
      return items.asInstanceOf[Collection[_]].isEmpty
    }
    semesterTree.isEmpty
  }

  def getSemesterTree(): Map[String, List[Semester]] = {
    if (semesterTree.isEmpty) {
      evaluateParams()
    }
    semesterTree
  }

  def getTemplateName(): String = Theme.getTemplateName(this.getClass)

  def getLabel(): String = label

  def setLabel(label: String) {
    this.label = label
  }

  def getTitle(): String = title

  def setTitle(title: String) {
    this.title = title
  }

  def getRequired(): AnyRef = required

  def setRequired(required: AnyRef) {
    this.required = required
  }

  def getCheck(): String = check

  def setCheck(check: String) {
    this.check = check
  }

  def getEmpty(): AnyRef = empty

  def setEmpty(empty: AnyRef) {
    this.empty = empty
  }

  def getMulti(): String = multi

  def setMulti(multi: String) {
    this.multi = multi
  }

  def setValue(value: AnyRef) {
    this.value = value
  }

  def getValue(): AnyRef = value

  def setName(name: String) {
    this.name = name
  }

  def getName(): String = name

  def setItems(items: AnyRef) {
    this.items = items
  }

  def getItems(): AnyRef = items

  def setSemesterTree(semesterTree: Map[String, List[Semester]]) {
    this.semesterTree = semesterTree
  }

  def setFormat(format: String) {
    this.format = format
  }

  def getFormat(): String = format

  def getIndexes(): List[Integer] = indexes

  def setIndexes(indexes: List[Integer]) {
    this.indexes = indexes
  }

  def setYearRules(yearRules: AnyRef) {
    this.yearRules = yearRules
  }

  def setTermRules(termRules: AnyRef) {
    this.termRules = termRules
  }

  def getOnChange(): String = onChange

  def setOnChange(onChange: String) {
    this.onChange = onChange
  }

  def getValueIndex(): Int = valueIndex

  def setValueIndex(valueIndex: Int) {
    this.valueIndex = valueIndex
  }

  def getTermIndex(): Int = termIndex

  def setTermIndex(termIndex: Int) {
    this.termIndex = termIndex
  }

  def getDefaultValue(): Semester = defaultValue

  def setDefaultValue(defaultValue: Semester) {
    this.defaultValue = defaultValue
  }

  def getInitCallback(): String = initCallback

  def setInitCallback(initCallback: String) {
    this.initCallback = initCallback
  }

  def getUiType(): String = uiType

  def setUiType(uiType: String) {
    this.uiType = uiType
  }

  def getOnClick(): String = onClick

  def setOnClick(onClick: String) {
    this.onClick = onClick
  }

  def getOnKeyup(): String = onKeyup

  def setOnKeyup(onKeyup: String) {
    this.onKeyup = onKeyup
  }

  def getOnKeypress(): String = onKeypress

  def setOnKeypress(onKeypress: String) {
    this.onKeypress = onKeypress
  }

  def getOnKeydown(): String = onKeydown

  def setOnKeydown(onKeydown: String) {
    this.onKeydown = onKeydown
  }

  def getOnFocus(): String = onFocus

  def setOnFocus(onFocus: String) {
    this.onFocus = onFocus
  }

  def getOnBlur(): String = onBlur

  def setOnBlur(onBlur: String) {
    this.onBlur = onBlur
  }

  def getBeforeInit(): String = beforeInit

  def setBeforeInit(beforeInit: String) {
    this.beforeInit = beforeInit
  }
}
