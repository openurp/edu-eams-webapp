package org.openurp.edu.eams.teach.grade.course.web.action




import org.beangle.commons.lang.Strings
import org.beangle.commons.bean.comparators.PropertyComparator
import org.beangle.commons.collection.Collections
import org.beangle.commons.collection.Order
import org.beangle.commons.dao.query.builder.Condition
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.lang.Strings
import org.beangle.struts2.helper.QueryHelper
import org.openurp.base.Department
import org.openurp.edu.base.Project
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.code.industry.ExamMode
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.code.industry.ScoreMarkStyle
import org.openurp.edu.eams.teach.grade.lesson.service.LessonGradeService
import org.openurp.edu.eams.teach.grade.service.CourseGradeService
import org.openurp.edu.eams.teach.grade.service.GradeRateService
import org.openurp.edu.teach.grade.CourseGrade
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.teach.grade.model.ExamGradeState
import org.openurp.edu.eams.teach.lesson.GradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class StateAction extends SemesterSupportAction {

  var lessonGradeService: LessonGradeService = _

  var courseGradeService: CourseGradeService = _

  var gradeRateService: GradeRateService = _

  protected def indexSetting() {
    put("examModes", baseCodeService.getCodes(classOf[ExamMode]))
    put("stdTypeList", getStdTypes)
  }

  def search(): String = {
    val builder = getQueryBuilder
    builder.select("distinct course")
    val courses = entityDao.search(builder)
    builder.limit(null)
    builder.select("lesson")
    val lessons = entityDao.search(builder)
    val courseMap = getCourseOfLessons(lessons)
    val query = OqlBuilder.from(classOf[CourseGrade], "grade")
    query.where("grade.semester.id = :semesterId", getInt("semester.id"))
    if (Collections.isEmpty(courses)) {
      query.join("grade.course", "course")
      builder.where(QueryHelper.extractConditions(classOf[Course], "course", null))
    } else {
      query.where("grade.course in (:courses)", courses)
    }
    query.where("grade.lesson is not null")
    val grades = entityDao.search(query)
    var it = grades.iterator()
    while (it.hasNext) {
      val grade = it.next().asInstanceOf[CourseGrade]
      val obj = courseMap.get(grade.getCourse.id.toString)
      if (null == obj) {
        //continue
      }
      obj(2) = grade
    }
    put("courseMap", courseMap)
    put("courses", courses)
    forward()
  }

  protected def getQueryBuilder(): OqlBuilder[Lesson] = {
    val projects = getProjects
    val departments = getDeparts
    val query = OqlBuilder.from(classOf[Lesson], "lesson")
    populateConditions(query)
    if (Collections.isEmpty(projects) || Collections.isNotEmpty(departments)) {
    }
    query.join("lesson.course", "course")
    query.where(QueryHelper.extractConditions(classOf[Course], "course", null))
    query.where("lesson.project in (:projects)", projects)
    query.where("lesson.teachDepart in (:departs)", departments)
    var hql = "exists (from org.openurp.edu.teach.grade.model.CourseGradeState courseGradeState where courseGradeState.lesson = lesson and "
    if (false == getBoolean("isPercentSetting")) {
      hql += "not "
    }
    hql += "exists (from courseGradeState.states examGradeState))"
    query.where(new Condition(hql))
    query
  }

  def info(): String = {
    val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson.id", getLong("lessonId"))
    if (Collections.isNotEmpty(courseGradeStates)) {
      put("gradeState", courseGradeStates.get(0))
      put("lesson", courseGradeStates.get(0).getLesson)
    }
    forward()
  }

  protected def buildhGradeTypeQuery(): OqlBuilder[GradeType] = {
    OqlBuilder.from(classOf[GradeType], "gradeType").where("gradeType.enabled =:enabled", ExamType.FINAL)
  }

  def editBatchPercent(): String = {
    val courseIds = Strings.splitToLong(get("courseIds"))
    val query = getQueryBuilder
    query.where(new Condition("course.id in (:courseIds)", courseIds))
    query.orderBy(Order.parse("course.id desc"))
    put("courseMap", getCourseOfLessons(entityDao.search(query)))
    put("canInputGradeTypes", lessonGradeService.getCanInputGradeTypes(true))
    put("markStyles", gradeRateService.getMarkStyles(getProject))
    forward("batchPercentForm")
  }

  def saveBatchPercent(): String = {
    val courseIds = Strings.splitToLong(get("courseIds"))
    val query = getQueryBuilder
    query.where(new Condition("course.id in (:courseIds)", courseIds))
    val lessons = entityDao.search(query)
    val gaMarkStyleId = getInt("gaMarkStyleId")
    val percision = getInt("percision")
    val canInputGradeTypes = lessonGradeService.getCanInputGradeTypes(true)
    for (lesson <- lessons) {
      val list = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
      var gradeState: CourseGradeState = null
      if (!list.isEmpty) {
        gradeState = list.get(0)
      }
      gradeState.setPrecision(percision)
      gradeState.setScoreMarkStyle(new ScoreMarkStyle(gaMarkStyleId))
      gradeState.getStates.clear()
      var it2 = canInputGradeTypes.iterator()
      while (it2.hasNext) {
        val gradeType = it2.next()
        val percent = getFloat("percent" + gradeType.id).floatValue() / 100
        if (percent == 0.0) {
          //continue
        }
        val markStyleId = getInt("markStyle" + gradeType.id)
        val gradeTypeState = Model.newInstance(classOf[ExamGradeState]).asInstanceOf[ExamGradeState]
        gradeTypeState.setGradeState(gradeState)
        gradeTypeState.setScoreMarkStyle(new ScoreMarkStyle(markStyleId))
        gradeTypeState.setGradeType(gradeType)
        gradeTypeState.setPercent(percent)
        gradeState.getStates.add(gradeTypeState)
        courseGradeService.recalculate(gradeState)
      }
    }
    entityDao.saveOrUpdate(lessons)
    redirect("search", "info.action.success")
  }

  def statusStat(): String = {
    val gradeTypes = baseCodeService.getCodes(classOf[GradeType])
    val departments = getDeparts
    val results = Collections.newBuffer[Any]
    var iter = gradeTypes.iterator()
    while (iter.hasNext) {
      val gradeType = iter.next()
      val query = OqlBuilder.from(classOf[CourseGradeState], "gradeState")
      populateConditions(query)
      if (getProject == null || Collections.isNotEmpty(departments)) {
        query.where("gradeState is null")
      } else {
        query.where("gradeState.lesson.project = :project", getProject)
        query.where("gradeState.lesson.teachDepart in (:departments)", departments)
      }
      if (gradeType.id == GradeTypeConstants.FINAL_ID) {
        //continue
      }
      query.join("gradeState.states", "examGradeState")
      query.where("examGradeState.gradeType = :gradeType", gradeType)
      query.groupBy("examGradeState.status")
      query.orderBy("examGradeState.status")
      query.select("examGradeState.status, count(*)")
      val queryResults = entityDao.search(query)
      val obj = Array.ofDim[Any](5)
      obj(0) = gradeType
      var it = queryResults.iterator()
      while (it.hasNext) {
        val data = it.next()
        obj(data(0).asInstanceOf[Number].intValue() + 1) = data(1)
      }
      results.add(obj)
    }
    put("results", results)
    forward()
  }

  def publishStat(): String = {
    val gradeTypes = entityDao.search(buildhGradeTypeQuery())
    val projects = getProjects
    val departments = getDeparts
    val results = Collections.newBuffer[Any]
    var iter = gradeTypes.iterator()
    while (iter.hasNext) {
      val gradeType = iter.next()
      val obj = Array.ofDim[Any](3)
      obj(0) = gradeType
      val query = OqlBuilder.from(classOf[CourseGradeState], "gradeState")
      populateConditions(query)
      if (Collections.isEmpty(projects) || Collections.isNotEmpty(departments)) {
        query.where("gradeState is null")
      } else {
        query.where("gradeState.lesson.project in (:project)", getProjects)
        query.where("gradeState.lesson.teachDepart in (:departments)", getDeparts)
      }
      if (null != gradeType && gradeType.id == GradeTypeConstants.FINAL_ID) {
        query.where(new Condition("gradeState.confirmed=true"))
        query.groupBy("gradeState.published")
        query.orderBy(Order.parse("gradeState.published"))
      } else {
        query.join("gradeState.states", "gradeTypeState")
        query.where("gradeTypeState.confirmed = true")
        query.groupBy("gradeTypeState.published")
        query.orderBy(Order.parse("gradeTypeState.published"))
      }
      query.select("count(*)")
      val queryResults = entityDao.search(query)
      if (Collections.isNotEmpty(queryResults)) {
        obj(1) = queryResults.get(0)
        obj(2) = if (queryResults.size > 1) queryResults.get(1) else new java.lang.Integer("0")
      } else {
        obj(1) = new java.lang.Integer("0")
        obj(2) = new java.lang.Integer("0")
      }
      results.add(obj)
    }
    put("results", results)
    forward()
  }

  def percentStat(): String = {
    val gradeTypes = lessonGradeService.getCanInputGradeTypes(true)
    val gradeTypeMap = Collections.newMap[Any]
    for (gradeType <- gradeTypes) {
      gradeTypeMap.put(gradeType.id, gradeType)
    }
    val lessons = entityDao.get(classOf[Lesson], "semester.id", getInt("semester.id"))
    val results = Collections.newMap[Any]
    for (lesson <- lessons) {
      val key = new StringBuilder()
      getState(key, lesson, gradeTypeMap.get(GradeTypeConstants.USUAL_ID).asInstanceOf[GradeType])
      key.append("_")
      getState(key, lesson, gradeTypeMap.get(GradeTypeConstants.MIDDLE_ID).asInstanceOf[GradeType])
      key.append("_")
      getState(key, lesson, gradeTypeMap.get(GradeTypeConstants.END_ID).asInstanceOf[GradeType])
      key.append("_")
      getState(key, lesson, gradeTypeMap.get(GradeTypeConstants.MAKEUP_ID).asInstanceOf[GradeType])
      key.append("_")
      getState(key, lesson, gradeTypeMap.get(GradeTypeConstants.DELAY_ID).asInstanceOf[GradeType])
      var obj = results.get(key.toString)
      obj = if (null == obj) new java.lang.Integer(1) else new java.lang.Integer(obj.intValue() + 1)
      results.put(key.toString, obj)
    }
    put("results", results)
    forward()
  }

  protected def getState(key: StringBuilder, lesson: Lesson, gradeType: GradeType) {
    val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
    var gradeState: CourseGradeState = null
    if (Collections.isNotEmpty(courseGradeStates)) {
      gradeState = courseGradeStates.get(0)
    }
    if (null == gradeState) {
      key.append("0")
      return
    }
    val gradeTypeState = gradeState.getState(gradeType)
    if (null == gradeTypeState) {
      key.append("0")
    } else {
      key.append((gradeTypeState.getPercent * 100).toInt)
    }
  }

  protected def gettingGradeType(gradeTypeId: java.lang.Integer): GradeType = {
    entityDao.get(classOf[GradeType], gradeTypeId)
  }

  protected def getCourseOfLessons(lessons: Iterable[Lesson]): Map[String, Array[Any]] = {
    val isPercentSetting = getBoolean("isPercentSetting")
    val courseMap = Collections.newMap[Any]
    val gradeStateMap = Collections.newMap[Any]
    for (lesson <- lessons) {
      val courseGradeStates = entityDao.get(classOf[CourseGradeState], "lesson", lesson)
      var gradeState: CourseGradeState = null
      if (Collections.isNotEmpty(courseGradeStates)) {
        gradeState = courseGradeStates.get(0)
      }
      val courseKey = lesson.getCourse.id.toString
      if (!courseMap.containsKey(courseKey)) {
        courseMap.put(courseKey, Array(Collections.newBuffer[Any], Collections.newBuffer[Any], null))
      }
      val obj = courseMap.get(courseKey).asInstanceOf[Array[Any]]
      val courseOfTasks = obj(0).asInstanceOf[List[Lesson]]
      courseOfTasks.add(lesson)
      if (true == isPercentSetting) {
        val pc = new PropertyComparator("gradeType.id")
        val states = Collections.newBuffer[Any](gradeState.getStates)
        Collections.sort(states, pc)
        if (Collections.isNotEmpty(states)) {
          val gradeStateKey = new StringBuilder()
          gradeStateKey.append(courseKey + ":")
          var it2 = states.iterator()
          while (it2.hasNext) {
            val gradeTypeState = it2.next().asInstanceOf[ExamGradeState]
            gradeStateKey.append(gradeTypeState.gradeType.id)
            gradeStateKey.append("_")
            gradeStateKey.append(gradeTypeState.getPercent)
            if (it2.hasNext) {
              gradeStateKey.append("|")
            }
          }
          if (!gradeStateMap.containsKey(gradeStateKey.toString)) {
            gradeStateMap.put(gradeStateKey.toString, gradeState)
          }
        }
      }
    }
    if (true == isPercentSetting) {
      var it = gradeStateMap.keySet.iterator()
      while (it.hasNext) {
        val key = it.next()
        val keys = Strings.split(key, ":")
        val obj = courseMap.get(keys(0)).asInstanceOf[Array[Any]]
        val gradeStates = obj(1).asInstanceOf[Iterable[GradeState]]
        gradeStates.add(gradeStateMap.get(key))
      }
    }
    courseMap
  }
}
