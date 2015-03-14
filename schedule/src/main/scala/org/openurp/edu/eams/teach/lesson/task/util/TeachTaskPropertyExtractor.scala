package org.openurp.edu.eams.teach.lesson.task.util

import java.text.MessageFormat
import java.util.ArrayList
import java.util.HashSet
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Set
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.EntityDao
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.tuple.Pair
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.openurp.base.Room
import org.openurp.edu.base.Teacher
import org.openurp.code.edu.Education
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.eams.teach.Textbook
import org.openurp.edu.eams.teach.code.industry.ExamType
import org.openurp.edu.eams.teach.code.school.CourseHourType
import org.openurp.edu.eams.teach.lesson.ArrangeSuggest
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitMeta.Operator
import org.openurp.edu.eams.teach.lesson.CourseMaterial
import org.openurp.edu.eams.teach.lesson.ExamActivity
import org.openurp.edu.eams.teach.lesson.ExamRoom
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.teach.lesson.LessonMaterial
import org.openurp.edu.eams.teach.lesson.service.CourseLimitService
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.ExamActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.SuggestActivityDigestor
import org.openurp.edu.eams.teach.time.util.TermCalculator

import scala.collection.JavaConversions._

class TeachTaskPropertyExtractor(resource: TextResource) extends DefaultPropertyExtractor(resource) {

  protected var courseActivityFormat: String = _

  protected var examActivityFormat: String = _

  protected var examType: ExamType = _

  protected var semesterService: SemesterService = _

  protected var courseLimitService: CourseLimitService = _

  protected var lessonPlanRelationService: LessonPlanRelationService = _

  protected var entityDao: EntityDao = _

  protected var hourTypes: List[CourseHourType] = new ArrayList[CourseHourType]()

  private var currLimitContents: Pair[Long, Map[String, String]] = null

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val lesson = target.asInstanceOf[Lesson]
    val digestor = CourseActivityDigestor.getInstance
    val suggestDigestor = SuggestActivityDigestor.getInstance
    val examDigestor = ExamActivityDigestor.getInstance
    if ("fake.teachers" == property) {
      val sb = new StringBuilder()
      var iter = lesson.getTeachers.iterator()
      while (iter.hasNext) {
        val teacher = iter.next()
        sb.append(teacher.getName).append('[').append(teacher.getCode)
          .append(']')
        if (iter.hasNext) {
          sb.append(',')
        }
      }
      sb.toString
    } else if ("teachers.eduDegreeInside" == property) {
      var eduDegreeNames = ""
      if (lesson.getTeachers != null) {
        val singleTeacher = if (lesson.getTeachers.size == 1) true else false
        var iter = lesson.getTeachers.iterator()
        while (iter.hasNext) {
          val teacher = iter.next().asInstanceOf[Teacher]
          if (singleTeacher) {
            return super.getPropertyValue(teacher, "degreeInfo.eduDegreeInside.name")
          } else {
            if (eduDegreeNames != "") eduDegreeNames += " "
            eduDegreeNames += super.getPropertyValue(teacher, "degreeInfo.eduDegreeInside.name") + 
              " "
          }
        }
      }
      eduDegreeNames
    } else if ("teachers.degree" == property) {
      var degreeNames = ""
      if (lesson.getTeachers != null) {
        val singleTeacher = if (lesson.getTeachers.size == 1) true else false
        var iter = lesson.getTeachers.iterator()
        while (iter.hasNext) {
          val teacher = iter.next().asInstanceOf[Teacher]
          if (singleTeacher) {
            return super.getPropertyValue(teacher, "degreeInfo.degree.name")
          } else {
            if (degreeNames != "") degreeNames += " "
            degreeNames += super.getPropertyValue(teacher, "degreeInfo.degree.name") + 
              " "
          }
        }
      }
      degreeNames
    } else if ("teachers.teacherType" == property) {
      getPropertyIn(lesson.getTeachers, "teacherType.name")
    } else if ("teachers.department.name" == property) {
      getPropertyIn(lesson.getTeachers, "department.name")
    } else if ("fake.grades" == property) {
      val result = getContents(lesson).get("年级")
      if (null == result) "" else result
    } else if ("fake.educations" == property) {
      val result = getContents(lesson).get("学历层次")
      if (null == result) "" else result
    } else if ("fake.departments" == property) {
      val result = getContents(lesson).get("院系")
      if (null == result) "" else result
    } else if ("fake.stdTypes" == property) {
      val result = getContents(lesson).get("学生类别")
      if (null == result) "" else result
    } else if ("fake.majors" == property) {
      val result = getContents(lesson).get("专业")
      if (null == result) "" else result
    } else if ("fake.directions" == property) {
      val result = getContents(lesson).get("方向")
      if (null == result) "" else result
    } else if ("fake.adminClasses" == property) {
      val result = getContents(lesson).get("班级")
      if (null == result) "" else result
    } else if ("fake.semester" == property) {
      lesson.getSemester.getSchoolYear + '-' + lesson.getSemester.getName
    } else if ("fake.auditStatus" == property) {
      lesson.getAuditStatus.getFullName
    } else if ("fake.arrangesuggest" == property) {
      val arrangeSuggestQuery = OqlBuilder.from(classOf[ArrangeSuggest], "suggest")
      arrangeSuggestQuery.where("suggest.lesson = :lesson", lesson)
      val suggests = entityDao.search(arrangeSuggestQuery)
      if (CollectUtils.isNotEmpty(suggests)) {
        val suggest = suggests.get(0)
        var str = suggestDigestor.digest(textResource, suggest)
        if (CollectUtils.isNotEmpty(suggest.getRooms)) {
          str += "建议教室:" + 
            Strings.join(CollectionUtils.collect(suggest.getRooms, new Transformer() {

            def transform(input: AnyRef): AnyRef = {
              return input.asInstanceOf[Classroom].getName
            }
          }), ",")
        }
        if (CollectUtils.isNotEmpty(suggest.getRooms)) {
          var iter = suggest.getRooms.iterator()
          while (iter.hasNext) {
            str += iter.next().getName
            if (iter.hasNext) {
              str += ", "
            }
          }
        }
        if (Strings.isNotBlank(suggest.getRemark)) {
          str += " 文字建议:" + suggest.getRemark
        }
        return str
      }
      ""
    } else if ("semester" == property) {
      lesson.getSemester.getSchoolYear + " " + lesson.getSemester.getName
    } else if ("courseSchedule.activities.weeks" == property) {
      digestor.digest(textResource, lesson, CourseActivityDigestor.weeks)
    } else if ("fake.weeks" == property) {
      String.valueOf(lesson.getCourseSchedule.getEndWeek - lesson.getCourseSchedule.getStartWeek + 
        1)
    } else if ("fake.arrange" == property) {
      digestor.digest(textResource, lesson)
    } else if ("roomType.name" == property) {
      if (lesson.getCourseSchedule.getRoomType != null) {
        return lesson.getCourseSchedule.getRoomType.getName
      }
      ""
    } else if ("fake.courseSchedule.practiceHour" == property) {
      var `type`: CourseHourType = null
      for (hourType <- hourTypes if hourType.getName.indexOf("实践") != -1) {
        `type` = hourType
      }
      if (`type` != null) {
        return lesson.getCourse.getHour(`type`)
      }
      null
    } else if ("fake.courseSchedule.theoryHour" == property) {
      var `type`: CourseHourType = null
      for (hourType <- hourTypes if hourType.getName.indexOf("理论") != -1) {
        `type` = hourType
      }
      if (`type` != null) {
        return lesson.getCourse.getHour(`type`)
      }
      null
    } else if ("fake.courseSchedule.operateHour" == property) {
      var `type`: CourseHourType = null
      for (hourType <- hourTypes if hourType.getName.indexOf("上机") != -1 || hourType.getName.indexOf("操作") != -1) {
        `type` = hourType
      }
      if (`type` != null) {
        return lesson.getCourse.getHour(`type`)
      }
      null
    } else if ("fake.materials" == property) {
      val sb = new StringBuilder()
      val query = OqlBuilder.from(classOf[LessonMaterial], "book")
      query.where("book.lesson = :lesson", lesson)
      val material = entityDao.uniqueResult(query)
      if (material != null) {
        var iter = material.getBooks.iterator()
        while (iter.hasNext) {
          val book = iter.next()
          sb.append(MessageFormat.format("名称:{0},作者:{1},ISBN:{2},出版社:{3}", book.getName, if (book.getAuthor == null) "" else book.getAuthor, 
            if (book.getIsbn == null) "" else book.getIsbn, if (book.getPress == null) "" else book.getPress.getName))
          if (iter.hasNext) {
            sb.append("\n")
          }
        }
      } else {
        val queryCourse = OqlBuilder.from(classOf[CourseMaterial], "book")
        queryCourse.where("book.course = :course", lesson.getCourse)
        queryCourse.where("book.department = :department", lesson.getTeachDepart)
        queryCourse.where("book.semester = :semester", lesson.getSemester)
        val courseMaterial = entityDao.uniqueResult(queryCourse)
        if (courseMaterial != null) {
          var iter = courseMaterial.getBooks.iterator()
          while (iter.hasNext) {
            val book = iter.next()
            sb.append(MessageFormat.format("名称:{0},作者:{1},ISBN:{2},出版社:{3}", book.getName, if (book.getAuthor == null) "" else book.getAuthor, 
              if (book.getIsbn == null) "" else book.getIsbn, if (book.getPress == null) "" else book.getPress.getName))
            if (iter.hasNext) {
              sb.append("\n")
            }
          }
        }
      }
      sb.toString
    } else if ("courseSchedule.activities.room.capacityOfCourse" == property) {
      val rooms = new HashSet[Classroom]()
      for (activity <- lesson.getCourseSchedule.getActivities) {
        rooms.addAll(activity.getRooms)
      }
      val seats = Array.ofDim[Integer](rooms.size)
      val i = 0
      for (room <- rooms) {
        seats(i += 1) = room.getCapacity
      }
      Strings.join(seats, ',')
    } else if ("courseSchedule.activities.time" == property) {
      digestor.digest(textResource, lesson, ":day :units")
    } else if ("courseSchedule.activities.room" == property) {
      val rooms = new HashSet[Classroom]()
      for (activity <- lesson.getCourseSchedule.getActivities) {
        rooms.addAll(activity.getRooms)
      }
      val names = Array.ofDim[String](rooms.size)
      val i = 0
      for (room <- rooms) {
        names(i += 1) = room.getName
      }
      Strings.join(names, ",")
    } else if ("exam.date" == property) {
      examDigestor.digest(lesson.getExamSchedule.getActivity(examType), textResource, ExamActivityDigestor.weeks + " " + ExamActivityDigestor.day)
    } else if ("exam.time" == property) {
      val format = new StringBuilder()
      format.append(ExamActivityDigestor.weeks)
      format.append(" ")
      format.append(ExamActivityDigestor.day)
      examDigestor.digest(lesson.getExamSchedule.getActivity(examType), textResource, format.toString)
    } else if ("exam.rooms" == property) {
      val format = new StringBuilder()
      format.append(ExamActivityDigestor.room)
      format.append("(").append(ExamActivityDigestor.district)
        .append(", ")
        .append(ExamActivityDigestor.building)
        .append(")")
      examDigestor.digest(lesson.getExamSchedule.getActivity(examType), textResource, format.toString)
    } else if ("exam.department.name" == property) {
      val value = new StringBuilder()
      val activity = lesson.getExamSchedule.getActivity(examType)
      if (null != activity) {
        for (er <- activity.getExamRooms) {
          if (value.length > 0) {
            value.append(" ")
          }
          value.append(er.department)
        }
      }
      value.toString
    } else if ("exam.teachers" == property) {
      getPropertyIn(lesson.getExamSchedule.getExaminers(examType), "name")
    } else if ("task.term" == property) {
      var term = 0
      val termCalc = new TermCalculator(semesterService, lesson.getSemester)
      term = termCalc.getTerm(lesson.getTeachClass.grade, true)
      new java.lang.Long(term)
    } else if ("task.courseSchedule" == property) {
      var arrangeInfo = ""
      val weeks = lesson.getCourseSchedule.getWeeks
      val weekUnits = lesson.getCourseSchedule.getWeekHour
      if (0 != weeks && 0 != weekUnits) {
        arrangeInfo = weeks + "*" + weekUnits
      }
      arrangeInfo
    } else if ("education.name" == property) {
      val pair = courseLimitService.xtractEducationLimit(lesson.getTeachClass)
      var educationStr = ""
      if (pair._1 == Operator.NOT_IN || pair._1 == Operator.NOT_EQUAL) {
        val educationsForCurrPorject = lesson.getProject.educations
        educationsForCurrPorject.removeAll(pair._2)
        for (education <- educationsForCurrPorject) {
          educationStr += education.getName + ","
        }
        if (educationStr.length > 0) {
          educationStr = educationStr.substring(0, educationStr.length - 1)
        }
        return educationStr
      }
      if (pair._1 == Operator.IN || pair._1 == Operator.EQUAL) {
        for (education <- pair._2) {
          educationStr += education.getName + ","
        }
        if (educationStr.length > 0) {
          educationStr = educationStr.substring(0, educationStr.length - 1)
        }
        return educationStr
      }
      super.getPropertyValue(target, property)
    } else if ("teachLang.name" == property) {
      if (lesson.getLangType != null) {
        return lesson.getLangType.getName
      }
      ""
    } else {
      super.getPropertyValue(target, property)
    }
  }

  private def getContents(lesson: Lesson): Map[String, String] = {
    val lessonId = lesson.getId
    var contents: Map[String, String] = null
    if (null == currLimitContents || currLimitContents.getLeft != lessonId) {
      var fullname = lesson.getTeachClass.getFullname
      if (fullname.endsWith("...")) {
        fullname = fullname.substring(0, fullname.length - 3)
      }
      if (fullname.startsWith(",")) {
        fullname = fullname.substring(1)
      }
      contents = CollectUtils.newHashMap()
      val groups = fullname.split(";")
      for (group <- groups) {
        val items = group.split(",")
        for (item <- items) {
          val pair = item.split(":")
          if (pair.length == 2) {
            var content = contents.get(pair(0))
            if (null == content) {
              content = pair(1)
            } else {
              content += " " + pair(1)
            }
            contents.put(pair(0), content)
          }
        }
      }
      currLimitContents = new Pair[Long, Map[String, String]](lessonId, contents)
      return contents
    }
    currLimitContents.getRight
  }

  def setSemesterService(semesterService: SemesterService) {
    this.semesterService = semesterService
  }

  def setCourseLimitService(courseLimitService: CourseLimitService) {
    this.courseLimitService = courseLimitService
  }

  def getLessonPlanRelationService(): LessonPlanRelationService = lessonPlanRelationService

  def setLessonPlanRelationService(lessonPlanRelationService: LessonPlanRelationService) {
    this.lessonPlanRelationService = lessonPlanRelationService
  }

  def setCourseActivityFormat(courseActivityFormat: String) {
    this.courseActivityFormat = courseActivityFormat
  }

  def setEntityDao(entityDao: EntityDao) {
    this.entityDao = entityDao
    hourTypes = entityDao.getAll(classOf[CourseHourType])
  }
}
