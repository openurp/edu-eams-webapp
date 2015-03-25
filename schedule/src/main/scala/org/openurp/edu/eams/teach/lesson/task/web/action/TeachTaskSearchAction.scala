package org.openurp.edu.eams.teach.lesson.task.web.action






import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.openurp.base.Campus
import org.openurp.base.Department
import org.openurp.base.Semester
import org.beangle.commons.lang.time.WeekDays
import org.openurp.edu.eams.base.util.WeekStates
import org.openurp.edu.base.Project
import org.openurp.edu.base.Student
import org.openurp.edu.eams.teach.code.industry.TeachLangType
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonMaterial
import org.openurp.edu.teach.lesson.LessonTag
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.lesson.service.limit.CourseLimitMetaEnum
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.task.util.ProjectUtils
import org.openurp.edu.eams.teach.lesson.task.util.TeachTaskPropertyExtractor
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class TeachTaskSearchAction extends SemesterSupportAction {

  var lessonService: LessonService = _

  var lessonSearchHelper: LessonSearchHelper = _

  var lessonPlanRelationService: LessonPlanRelationService = _

  var courseLimitService: CourseLimitService = _

  def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    val semester = getAttribute("semester").asInstanceOf[Semester]
    val project = getProject
    val teachDeparts = ProjectUtils.getTeachDeparts(project)
    if (semester != null) {
      put("attendDeparts", getCollegeOfDeparts)
      put("teachDeparts", teachDeparts)
      put("courseTypes", lessonService.courseTypesOfSemester(Collections.singletonList(project), teachDeparts, 
        semester))
      put("weeks", WeekDays.All)
    }
    addBaseCode("langTypes", classOf[TeachLangType])
    addBaseInfo("campuses", classOf[Campus])
    put("departmentAll", departmentService.getColleges)
    put("departs", getDeparts)
    forward()
  }

  def search(): String = {
    val lessons = lessonSearchHelper.searchLesson()
    val guapaiStatus = new HashMap[Lesson, Boolean]()
    for (lesson <- lessons) {
      guapaiStatus.put(lesson, false)
      for (tag <- lesson.getTags if tag.id == LessonTag.PredefinedTags.GUAPAI.id) {
        guapaiStatus.put(lesson, true)
      }
    }
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    val arrangeInfo = new HashMap[String, String]()
    for (oneTask <- lessons) {
      arrangeInfo.put(oneTask.id.toString, digestor.digest(getTextResource, oneTask, ":teacher+ :day :units :weeks :room"))
    }
    put("arrangeInfo", arrangeInfo)
    put("guapaiStatus", guapaiStatus)
    put("lessons", lessons)
    put("weekStates", new WeekStates())
    forward()
  }

  def arrangeInfoList(): String = {
    val tasks = lessonSearchHelper.searchLesson()
    put("tasks", tasks)
    val digestor = CourseActivityDigestor.getInstance.setDelimeter("<br>")
    val arrangeInfo = new HashMap()
    var iter = tasks.iterator()
    while (iter.hasNext) {
      val task = iter.next().asInstanceOf[Lesson]
      arrangeInfo.put(task.id.toString, digestor.digest(getTextResource, task))
    }
    put("arrangeInfo", arrangeInfo)
    forward()
  }

  def info(): String = {
    val lessonId = getLongId("lesson")
    val lesson = entityDao.get(classOf[Lesson], lessonId)
    put("guapaiTagId", LessonTag.PredefinedTags.GUAPAI.id)
    put("fakeGender", courseLimitService.extractGender(lesson.getTeachClass))
    put("educationLimit", courseLimitService.xtractEducationLimit(lesson.getTeachClass))
    put("adminclassLimit", courseLimitService.xtractAdminclassLimit(lesson.getTeachClass))
    put("attendDepartLimit", courseLimitService.xtractAttendDepartLimit(lesson.getTeachClass))
    put("stdTypeLimit", courseLimitService.xtractStdTypeLimit(lesson.getTeachClass))
    put("majorLimit", courseLimitService.xtractMajorLimit(lesson.getTeachClass))
    put("directionLimit", courseLimitService.xtractDirectionLimit(lesson.getTeachClass))
    put("programLimit", courseLimitService.xtractProgramLimit(lesson.getTeachClass))
    put("lesson", lesson)
    val query = OqlBuilder.from(classOf[LessonMaterial], "book")
    query.where("book.lesson = :lesson", lesson)
    val lessonMaterials = entityDao.search(query)
    if (CollectUtils.isNotEmpty(lessonMaterials)) {
      put("lessonMaterial", lessonMaterials.get(0))
    }
    put("weekStates", new WeekStates())
    forward()
  }

  protected override def getExportDatas(): Iterable[_] = {
    val lessonIds = Strings.splitToLong(get("lessonIds"))
    if (lessonIds == null || lessonIds.length == 0) {
      val query = lessonSearchHelper.buildQuery().limit(null)
      val lessons = entityDao.search(query)
      lessons
    } else {
      entityDao.get(classOf[Lesson], lessonIds)
    }
  }

  protected def getPropertyExtractor(): PropertyExtractor = {
    val pe = new TeachTaskPropertyExtractor(getTextResource)
    pe.setSemesterService(semesterService)
    pe.setCourseLimitService(courseLimitService)
    pe.setLessonPlanRelationService(lessonPlanRelationService)
    pe.setEntityDao(entityDao)
    pe
  }

  def getElectStd(): String = {
    val lessonIds = getLongIds("lesson")
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val map = CollectUtils.newHashMap()
    for (lesson <- lessons) {
      val hql = CollectUtils.newArrayList()
      val query = OqlBuilder.from(classOf[CourseLimitGroup], "coursegroup")
      query.where("coursegroup.lesson.id =:lessonId", lesson.id)
      val group = entityDao.search(query)
      for (courseLimitGroup2 <- group; item <- courseLimitGroup2.getItems) {
        val hqlString = getConditionByItem(item)
        hql.add(hqlString)
        map.put(lesson, hql)
      }
    }
    val stdCount = CollectUtils.newHashMap()
    for ((key, value) <- map) {
      val key = key.asInstanceOf[Lesson]
      val value = value.asInstanceOf[List[String]]
      var itemHQL = ""
      for (i <- 0 until value.size) {
        itemHQL += value.get(i) + " and "
      }
      if (itemHQL.length > 0) {
        val hql = "from org.openurp.edu.base.Student std"
        itemHQL = itemHQL.substring(0, itemHQL.length - 5)
        val builder = OqlBuilder.from(hql)
        builder.where(itemHQL)
        val students = entityDao.search(builder)
        stdCount.put(key, students)
      } else {
        //continue
      }
    }
    val stds = CollectUtils.newHashSet()
    var it = stdCount.keySet.iterator()
    while (it.hasNext) {
      val key = it.next()
      val stdList = stdCount.get(key)
      var itor = stdList.iterator()
      while (itor.hasNext) {
        val std = itor.next()
        if (stds.contains(std)) {
          //continue
        } else {
          stds.add(std)
        }
      }
    }
    put("stdCount", stdCount)
    put("stds", stds)
    forward()
  }

  def getConditionByItem(item: CourseLimitItem): String = {
    var hql = ""
    var metaName = ""
    if (null == item || null == item.getMeta) {
      return null
    }
    metaName = if (item.getMeta.id == CourseLimitMetaEnum.GRADE.getMetaId) item.getMeta.getName else if (item.getMeta.id == CourseLimitMetaEnum.STDTYPE.getMetaId) "type.id" else item.getMeta.getName + ".id"
    if (null != item.getOperator) item.getOperator match {
      case EQUAL => hql = hql + "std." + metaName.toLowerCase() + "=" + item.getContentForHql
      case NOT_EQUAL => hql = hql + "std." + metaName.toLowerCase() + "<>" + item.getContentForHql
      case IN => hql = hql + "std." + metaName.toLowerCase() + " " + item.getOperator + 
        "(" + 
        ArrayUtils.toString(Strings.split(item.getContentForHql))
        .substring(1, ArrayUtils.toString(Strings.split(item.getContentForHql))
        .length - 
        1) + 
        ")"
      case NOT_IN => hql = hql + "std." + metaName.toLowerCase() + " not in" + "(" + 
        ArrayUtils.toString(Strings.split(item.getContentForHql))
        .substring(1, ArrayUtils.toString(Strings.split(item.getContentForHql))
        .length - 
        1) + 
        ")"
      case GREATE_EQUAL_THAN => hql = hql + "std." + metaName.toLowerCase() + ">=" + item.getContentForHql
      case LESS_EQUAL_THAN => hql = hql + "std." + metaName.toLowerCase() + " <= " + item.getContentForHql
      case NULL => hql = hql + "std." + metaName.toLowerCase() + ".id " + " is null "
      case _ => //break
    }
    hql
  }

  def getScaleStd(): String = {
    val lessonIds = getLongIds("lesson")
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    val map = CollectUtils.newHashMap()
    for (lesson <- lessons) {
      val hql = CollectUtils.newArrayList()
      val query = OqlBuilder.from(classOf[CourseLimitGroup], "coursegroup")
      query.where("coursegroup.lesson.id =:lessonId", lesson.id)
      val group = entityDao.search(query)
      for (courseLimitGroup2 <- group; item <- courseLimitGroup2.getItems) {
        val hqlString = getConditionByItem(item)
        hql.add(hqlString)
        map.put(lesson, hql)
      }
    }
    val stdCount = CollectUtils.newHashMap()
    for ((key, value) <- map) {
      val key = key.asInstanceOf[Lesson]
      val value = value.asInstanceOf[List[String]]
      var itemHQL = ""
      for (i <- 0 until value.size) {
        itemHQL += value.get(i) + " and "
      }
      if (itemHQL.length > 0) {
        val hql = "from org.openurp.edu.base.Student std"
        itemHQL = itemHQL.substring(0, itemHQL.length - 5)
        val builder = OqlBuilder.from(hql)
        builder.where(itemHQL)
        val students = entityDao.search(builder)
        stdCount.put(key, students)
      } else {
        //continue
      }
    }
    val equalCount = CollectUtils.newHashMap()
    val gtCount = CollectUtils.newHashMap()
    val ltCount = CollectUtils.newHashMap()
    for ((key, value) <- stdCount) {
      val key = key.asInstanceOf[Lesson]
      val std = value.asInstanceOf[List[Student]]
      for (lesson <- lessons) {
        var count = 0
        if (lesson.id == key.id) {
          if (lesson.getTeachClass.getLimitCount == std.size) {
            equalCount.put(lesson, std.size)
          } else if (lesson.getTeachClass.getLimitCount > std.size) {
            count = lesson.getTeachClass.getLimitCount - std.size
            gtCount.put(lesson, count)
          } else if (lesson.getTeachClass.getLimitCount < std.size) {
            count = std.size - lesson.getTeachClass.getLimitCount
            ltCount.put(lesson, count)
          }
        }
      }
    }
    put("stdCount", stdCount)
    put("equalCount", equalCount)
    put("gtCount", gtCount)
    put("ltCount", ltCount)
    forward()
  }
}
