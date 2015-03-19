package org.openurp.edu.eams.teach.grade.course.web.action



import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.collection.Order
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.entity.metadata.Model
import org.beangle.commons.transfer.exporter.PropertyExtractor
import org.openurp.base.Department
import org.openurp.base.Semester
import org.openurp.edu.teach.code.GradeType
import org.openurp.edu.eams.teach.grade.course.model.GradeStateStat
import org.openurp.edu.eams.teach.grade.course.service.propertyExtractor.GradeStatExtractor
import org.openurp.edu.teach.grade.model.CourseGradeState
import org.openurp.edu.eams.teach.lesson.GradeTypeConstants
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonService
import org.openurp.edu.eams.web.action.common.SemesterSupportAction



class GradeStateStatAction extends SemesterSupportAction {

  private var lessonService: LessonService = _

  protected override def indexSetting() {
    put("gradeTypes", baseCodeService.getCodes(classOf[GradeType]))
    put("departments", lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(getProject), getDeparts, 
      putSemester(null)))
    put("finalId", GradeTypeConstants.FINAL_ID)
    put("GA_ID", GradeTypeConstants.GA_ID)
  }

  override def search(): String = {
    val status = getInt("status")
    val departmentId = getInt("department.id")
    val gradeTypeId = getInt("gradeType.id")
    if (departmentId == null) {
      return forwardError("error.parameters.needed")
    }
    if (status == null) {
      put("lessons", entityDao.search(getQueryBuilder))
    } else {
      put("courseGradeStates", entityDao.search(getQueryBuilder))
      put("status", status)
      put("gradeTypeId", gradeTypeId)
    }
    put("departmentId", departmentId)
    forward()
  }

  protected override def getQueryBuilder(): OqlBuilder[_] = {
    val status = getInt("status")
    val departmentId = getInt("department.id")
    val gradeTypeId = getInt("gradeType.id")
    if (status == null) {
      val lessonBuilder = OqlBuilder.from(classOf[Lesson], "lesson")
      lessonBuilder.where("lesson.teachDepart.id = :departmentId", departmentId)
      lessonBuilder.where("lesson.semester = :semester", putSemester(null))
      lessonBuilder.where("lesson.project = :project", getProject)
      lessonBuilder.where("not exists(from org.openurp.edu.teach.grade.model.ExamGradeState egt where lesson=egt.gradeState.lesson and egt.gradeType.id = :gradeTypeId)", 
        gradeTypeId)
      lessonBuilder.limit(getPageLimit)
      lessonBuilder.orderBy(get(Order.ORDER_STR))
      return lessonBuilder
    }
    val queryInput = OqlBuilder.from(classOf[CourseGradeState], "gradeState")
    queryInput.where("gradeState.lesson.teachDepart.id= :departmentId", departmentId)
    queryInput.where("gradeState.lesson.semester = :semester", putSemester(null))
    queryInput.join("gradeState.states", "examGradeState")
    queryInput.where("examGradeState.gradeType.id =:gradeTypeId", gradeTypeId)
    queryInput.where("examGradeState.status = :status", status)
    queryInput.orderBy(get(Order.ORDER_STR))
    queryInput.limit(getPageLimit)
    queryInput
  }

  def statusStat(): String = {
    val gradeTypeId = getInt("gradeType.id")
    var gradeTypes = CollectUtils.newArrayList()
    val teachDeparts = lessonService.teachDepartsOfSemester(CollectUtils.newArrayList(getProject), getDeparts, 
      putSemester(null))
    if (null != gradeTypeId) {
      gradeTypes.add(baseCodeService.getCode(classOf[GradeType], gradeTypeId))
    } else {
      gradeTypes = baseCodeService.getCodes(classOf[GradeType])
    }
    val departmentId = getInt("department.id")
    var departments = CollectUtils.newArrayList()
    if (null != departmentId) {
      departments.add(entityDao.get(classOf[Department], departmentId))
    } else {
      departments = teachDeparts
    }
    val results = CollectUtils.newArrayList()
    val semester = putSemester(null)
    val unInputLessonMap = CollectUtils.newHashMap()
    for (department <- departments; gradeType <- gradeTypes) {
      val lessonBuilder = OqlBuilder.from(classOf[Lesson], "lesson")
      lessonBuilder.where("lesson.teachDepart = :department", department)
      lessonBuilder.where("lesson.semester = :semester", semester)
      lessonBuilder.where("lesson.project = :project", getProject)
      lessonBuilder.where("not exists(from org.openurp.edu.teach.grade.model.ExamGradeState egt where lesson=egt.gradeState.lesson and egt.gradeType = :gradeType)", 
        gradeType)
      lessonBuilder.select("select count(*)")
      unInputLessonMap.put(department.id + "_" + gradeType.id, entityDao.search(lessonBuilder).get(0))
      val queryInput = OqlBuilder.from(classOf[CourseGradeState], "gradeState")
      if (gradeType.id == GradeTypeConstants.FINAL_ID) {
        //continue
      }
      queryInput.where("gradeState.lesson.teachDepart= :department", department)
      queryInput.where("gradeState.lesson.semester = :semester", semester)
      queryInput.join("gradeState.states", "examGradeState")
      queryInput.where("examGradeState.gradeType=:gradeType", gradeType)
      queryInput.groupBy("examGradeState.status")
      queryInput.orderBy(Order.parse("examGradeState.status"))
      queryInput.select("examGradeState.status,count(*)")
      val queryResults = entityDao.search(queryInput).asInstanceOf[List[Array[Any]]]
      val stat = new GradeStateStat()
      stat.setGradeType(gradeType)
      stat.setDepartment(department)
      for (data <- queryResults) java.lang.Integer.valueOf(data(0).toString) match {
        case 0 => stat.setUnpublished(java.lang.Integer.valueOf(data(1).toString))
        case 1 => stat.setSubmited(java.lang.Integer.valueOf(data(1).toString))
        case 2 => stat.setPublished(java.lang.Integer.valueOf(data(1).toString))
        case _ => //break
      }
      results.add(stat)
    }
    put("unInputLessonMap", unInputLessonMap)
    put("results", results)
    forward()
  }

  def setLessonService(lessonService: LessonService) {
    this.lessonService = lessonService
  }

  protected def getPropertyExtractor(): PropertyExtractor = new GradeStatExtractor(getTextResource)
}
