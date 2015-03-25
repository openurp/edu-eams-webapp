package org.openurp.edu.eams.teach.schedule.web.action

import java.util.Arrays


import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.base.Project
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.helper.LessonSearchHelper
import org.openurp.edu.eams.teach.lesson.model.CourseScheduleBean.CourseStatusEnum
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.teach.schedule.model.LessonForDepart
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class AllocateLessonForArrangeAction extends SemesterSupportAction {

  var lessonSearchHelper: LessonSearchHelper = _

  var lessonService: LessonService = _

  private def getSemester(): Semester = {
    val semesterId = getInt("semester.id")
    if (semesterId == null) (if (getAttribute("semester") == null) semesterService.getCurSemester(getProject) else getAttribute("semester").asInstanceOf[Semester]) else entityDao.get(classOf[Semester], 
      semesterId)
  }

  private def getLessonForDepartsBySemester(semester: Semester, departments: List[Department]): OqlBuilder[LessonForDepart] = {
    val builder = OqlBuilder.from(classOf[LessonForDepart], "lessonForDepart")
      .where("lessonForDepart.semester = :semester", semester)
      .where("lessonForDepart.project = :project", getProject)
    if (departments.isEmpty) {
      builder.where("1=2")
    } else {
      builder.where("lessonForDepart.department in (:departments)", departments)
    }
    builder
  }

  private def getWastedLessonIds(semester: Semester): Map[Department, Set[Long]] = {
    val builder = OqlBuilder.from(classOf[LessonForDepart], "lessonForDepart")
    builder.join("lessonForDepart.lessonIds", "lessonId")
    builder.where("not exists(from org.openurp.edu.teach.lesson.Lesson lesson where lessonId=lesson.id)")
    builder.where("lessonForDepart.semester = :semester", semester)
    builder.where("lessonForDepart.project = :project", getProject)
    builder.select("lessonForDepart,lessonId")
    val lessonForDepartAndLessonIds = entityDao.search(builder).asInstanceOf[List[Array[Any]]]
    val wastedLessonIds = CollectUtils.newHashMap()
    for (objects <- lessonForDepartAndLessonIds) {
      val department = objects(0).asInstanceOf[LessonForDepart].department
      val lessonId = objects(1).asInstanceOf[java.lang.Long]
      if (wastedLessonIds.keySet.contains(department)) {
        wastedLessonIds.get(department).add(lessonId)
      } else {
        wastedLessonIds.put(department, CollectUtils.newHashSet(lessonId))
      }
    }
    wastedLessonIds
  }

  override def index(): String = {
    setSemesterDataRealm(hasStdTypeCollege)
    val semester = getSemester
    val project = getProject
    if (semester == null) {
      return forwardError("error.parameters.needed")
    }
    val builder = OqlBuilder.from(classOf[LessonForDepart], "lessonForDepart")
    builder.join("lessonForDepart.lessonIds", "lessonId")
    builder.where("not exists(from org.openurp.edu.teach.lesson.Lesson lesson where lessonId=lesson.id)")
    builder.where("lessonForDepart.semester = :semester", semester)
    builder.where("lessonForDepart.project = :project", project)
    builder.select("lessonForDepart,lessonId")
    val lessonForDepartAndLessonIds = entityDao.search(builder).asInstanceOf[List[Array[Any]]]
    val toSave = CollectUtils.newArrayList()
    val wastedLessonIds = CollectUtils.newHashMap()
    for (objects <- lessonForDepartAndLessonIds) {
      val lessonForDepart = objects(0).asInstanceOf[LessonForDepart]
      val lessonId = objects(1).asInstanceOf[java.lang.Long]
      if (wastedLessonIds.keySet.contains(lessonForDepart)) {
        wastedLessonIds.get(lessonForDepart).add(lessonId)
      } else {
        wastedLessonIds.put(lessonForDepart, CollectUtils.newHashSet(lessonId))
      }
    }
    for (lessonForDepart <- wastedLessonIds.keySet) {
      lessonForDepart.getLessonIds.removeAll(wastedLessonIds.get(lessonForDepart))
      toSave.add(lessonForDepart)
    }
    entityDao.saveOrUpdate(toSave)
    val departmentQuery = OqlBuilder.from(classOf[LessonForDepart], "lfd").where("lfd.semester= :semester", 
      semester)
      .where("lfd.project = :project", project)
      .select("select distinct lfd.department")
    val departments = entityDao.search(departmentQuery).asInstanceOf[List[Department]]
    val departs = getDeparts
    var lessons: List[Lesson] = null
    if (departs.isEmpty) {
      lessons = Collections.emptyList()
    } else {
      val query = OqlBuilder.from(classOf[Lesson], "lesson")
      query.where("lesson.teachDepart in (:departments)", departs)
      query.where("lesson.semester = :semester", semester)
      query.where("lesson.project = :project", project)
      lessons = entityDao.search(query)
    }
    val departmentLesson = CollectUtils.newHashMap()
    val lessonForDeparts = entityDao.search(getLessonForDepartsBySemester(semester, departments))
    var size = 0
    for (lessonForDepart <- lessonForDeparts) {
      val department = lessonForDepart.department
      val lessonIds = lessonForDepart.getLessonIds
      departmentLesson.put(department, lessonIds.size)
      size += lessonIds.size
    }
    val keySet = departmentLesson.keySet
    for (department <- departments if !keySet.contains(department)) {
      departmentLesson.put(department, 0)
    }
    put("departmentMap", departmentLesson)
    put("notAllocateSize", lessons.size - size)
    put("totalSize", lessons.size)
    put("semester", semester)
    val defaultDepartmentId = getLong("defaultDepartmentId")
    if (null != defaultDepartmentId) {
      put("defaultDepartmentId", defaultDepartmentId)
    }
    var defaultNotLocate = getBoolean("defaultNotLocate")
    if (defaultNotLocate == null) {
      defaultNotLocate = true
    }
    put("defaultNotLocate", defaultNotLocate)
    forward()
  }

  override def search(): String = {
    val builder = lessonSearchHelper.buildQuery()
    builder.where("lesson.project.id=:projectid1", getSession.get("projectId").asInstanceOf[java.lang.Integer])
    val semester = getSemester
    val notLocate = getBool("notLocate")
    val departmentId = getInt("departmentId")
    val departments = lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(getProject), getDeparts, 
      semester)
    put("notLocate", notLocate)
    if (notLocate) {
      builder.where("not exists (from org.openurp.edu.eams.teach.schedule.model.LessonForDepart lfd join lfd.lessonIds lessonId where lesson.id = lessonId)")
      put("departmentsToLocate", departments)
      val locateDepartmentId = getInt("locateDepartmentId")
      if (null != locateDepartmentId) {
        put("lessons", entityDao.search(builder))
        put("courseStatusEnums", CourseStatusEnum.values)
        put("semester", semester)
        put("locateDepartment", entityDao.get(classOf[Department], locateDepartmentId))
        return forward()
      }
    }
    if (null != departmentId) {
      builder.where("exists (from org.openurp.edu.eams.teach.schedule.model.LessonForDepart lfd join lfd.lessonIds lessonId where lesson.id = lessonId and lfd.department.id = " + 
        departmentId + 
        ")")
      put("department", entityDao.get(classOf[Department], departmentId))
    }
    put("lessons", entityDao.search(builder))
    put("courseStatusEnums", CourseStatusEnum.values)
    put("semester", semester)
    if (!notLocate && departmentId == null) {
      val lessonDepartMap = CollectUtils.newHashMap()
      val lessonForDeparts = entityDao.search(getLessonForDepartsBySemester(semester, departments))
      for (lessonForDepart <- lessonForDeparts; lessonId <- lessonForDepart.getLessonIds) {
        lessonDepartMap.put(entityDao.get(classOf[Lesson], lessonId), lessonForDepart.department)
      }
      put("lessonDepartMap", lessonDepartMap)
    }
    put("teachDeparts", departments)
    forward()
  }

  def batchEditArrangeTime(): String = {
    val semester = getSemester
    val departments = lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(getProject), getDeparts, 
      semester)
    val lessonForDeparts = entityDao.search(getLessonForDepartsBySemester(getSemester, departments))
    var lessonForDepartIds = ""
    for (i <- 0 until lessonForDeparts.size) {
      if (i > 0) {
        lessonForDepartIds += ","
      }
      lessonForDepartIds += lessonForDeparts.get(i).id
    }
    put("lessonForDeparts", lessonForDeparts)
    put("semester", semester)
    put("lessonForDepartIds", lessonForDepartIds)
    forward()
  }

  def batchSaveArrangeTime(): String = {
    val lessonForDepartIds = getLongIds("lessonForDepart")
    val lessonForDeparts = entityDao.get(classOf[LessonForDepart], lessonForDepartIds)
    for (lessonForDepart <- lessonForDeparts) {
      lessonForDepart.setBeginAt(getDateTime("lessonForDepart" + lessonForDepart.id + ".beginAt"))
      lessonForDepart.setEndAt(getDateTime("lessonForDepart" + lessonForDepart.id + ".endAt"))
    }
    try {
      entityDao.saveOrUpdate(lessonForDeparts)
      redirect("index", "info.save.success")
    } catch {
      case e: Exception => redirect("index", "info.save.failure")
    }
  }

  override def save(): String = {
    val departmentId = getIntId("department")
    val lessonIds = getLongIds("lesson")
    if (null == departmentId || ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.parameters.needed")
    }
    val semester = getSemester
    val project = getProject
    val department = Model.newInstance(classOf[Department], departmentId)
    val lessons = entityDao.get(classOf[Lesson], lessonIds)
    if (lessonIds.length != lessons.size) {
      return forwardError("所选任务可能已被删除")
    }
    val lessonIdSet = CollectUtils.newHashSet(Arrays.asList(lessonIds:_*))
    val lessonForDeparts = entityDao.search(OqlBuilder.from(classOf[LessonForDepart], "lessonForDepart")
      .where("lessonForDepart.department.id = :departmentId", departmentId)
      .where("lessonForDepart.semester = :semester", semester)
      .where("lessonForDepart.project = :project", project))
    var lessonForDepart: LessonForDepart = null
    if (lessonForDeparts.isEmpty) {
      lessonForDepart = new LessonForDepart(lessonIdSet, department, semester, project)
    } else {
      lessonForDepart = lessonForDeparts.get(0)
      lessonForDepart.addLessonIds(lessonIdSet)
    }
    try {
      entityDao.saveOrUpdate(Collections.singletonList(lessonForDepart))
      redirect("index", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("index", "info.save.failure")
      }
    }
  }

  override def remove(): String = {
    val departmentId = getIntId("department")
    val notLocate = getBool("notLocate")
    val lessonIds = getLongIds("lesson")
    if (null == departmentId || ArrayUtils.isEmpty(lessonIds)) {
      return forwardError("error.parameters.needed")
    }
    val semester = getSemester
    val project = getProject
    val lessonForDeparts = entityDao.search(OqlBuilder.from(classOf[LessonForDepart], "lessonForDepart")
      .where("lessonForDepart.department.id = :departmentId", departmentId)
      .where("lessonForDepart.semester = :semester", semester)
      .where("lessonForDepart.project = :project", project))
    if (lessonForDeparts.isEmpty) {
      return forwardError("error.parameters.illegal")
    }
    val lessonForDepart = lessonForDeparts.get(0)
    lessonForDepart.removeLessonIds(Arrays.asList(lessonIds:_*))
    try {
      if (lessonForDepart.getLessonIds.isEmpty) {
        entityDao.remove(Collections.singletonList(lessonForDepart))
      } else {
        entityDao.saveOrUpdate(Collections.singletonList(lessonForDepart))
      }
      redirect("index", "info.save.success", "&defaultDepartmentId=" + departmentId + "&defaultNotLocate=" + 
        notLocate)
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("index", "info.save.failure", "&defaultDepartmentId=" + departmentId + "&defaultNotLocate=" + 
          notLocate)
      }
    }
  }

  def autoLocate(): String = {
    val semester = getSemester
    val project = getProject
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("not exists(from org.openurp.edu.eams.teach.schedule.model.LessonForDepart lessonForDepart join lessonForDepart.lessonIds lessonId where lesson.id = lessonId)")
      .where("lesson.semester = :semester", semester)
      .where("lesson.project = :project", project)
    val lessons = entityDao.search(builder)
    val departmentRestricts = lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(project), 
      getDeparts, semester)
    val lessonForDeparts = entityDao.search(getLessonForDepartsBySemester(semester, departmentRestricts))
    val lessonForDepartMap = CollectUtils.newHashMap()
    for (lessonForDepart <- lessonForDeparts) {
      lessonForDepartMap.put(lessonForDepart.department, lessonForDepart)
    }
    val departments = lessonForDepartMap.keySet
    for (lesson <- lessons) {
      val department = lesson.getTeachDepart
      val lessonId = lesson.id
      var lessonForDepart: LessonForDepart = null
      if (departments.contains(department)) {
        lessonForDepart = lessonForDepartMap.get(department)
        lessonForDepart.addLessonId(lessonId)
      } else {
        lessonForDepart = new LessonForDepart()
        lessonForDepart.setDepartment(department)
        lessonForDepart.addLessonId(lessonId)
        lessonForDepart.setProject(project)
        lessonForDepart.setSemester(semester)
        lessonForDepartMap.put(department, lessonForDepart)
      }
    }
    try {
      entityDao.saveOrUpdate(lessonForDepartMap.values)
      redirect("index", "info.save.success")
    } catch {
      case e: Exception => {
        logger.info("saveAndForwad failure", e)
        redirect("index", "info.save.failure")
      }
    }
  }
}
