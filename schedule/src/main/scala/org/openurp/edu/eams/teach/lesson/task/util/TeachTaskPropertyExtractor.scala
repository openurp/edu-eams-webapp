package org.openurp.edu.eams.teach.lesson.task.util

import java.text.MessageFormat
import org.beangle.commons.collection.Collections
import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.openurp.base.Room
import org.openurp.edu.base.Teacher
import org.openurp.code.edu.Education
import org.openurp.edu.eams.core.service.SemesterService
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.exam.ExamActivity
import org.openurp.edu.teach.exam.ExamRoom
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.service.LessonLimitService
import org.openurp.edu.eams.teach.lesson.task.service.LessonPlanRelationService
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor
import org.openurp.edu.eams.teach.lesson.util.ExamActivityDigestor
import org.openurp.edu.eams.teach.time.util.TermCalculator
import org.openurp.edu.base.code.CourseHourType
import org.openurp.edu.teach.code.ExamType



class TeachTaskPropertyExtractor(resource: TextResource) extends DefaultPropertyExtractor(resource) {

  var courseActivityFormat: String = _

  var examActivityFormat: String = _

  var examType: ExamType = _

  var semesterService: SemesterService = _

  var lessonLimitService: LessonLimitService = _

  var lessonPlanRelationService: LessonPlanRelationService = _

  var entityDao: EntityDao = _

  var hourTypes: Seq[CourseHourType] = Collections.newBuffer[CourseHourType]

  var currLimitContents: Pair[Long, collection.mutable.Map[String, String]] = null

  def getPropertyValue(target: Any, property: String): AnyRef = {
    val lesson = target.asInstanceOf[Lesson]
    val digestor = CourseActivityDigestor.getInstance
    val suggestDigestor = SuggestActivityDigestor.getInstance
    val examDigestor = ExamActivityDigestor.getInstance
    if ("fake.teachers" == property) {
      val sb = new StringBuilder()
      var iter = lesson.teachers.iterator
      while (iter.hasNext) {
        val teacher = iter.next()
        sb.append(teacher.name).append('[').append(teacher.code)
          .append(']')
        if (iter.hasNext) {
          sb.append(',')
        }
      }
      sb.toString
    } else if ("teachers.eduDegreeInside" == property) {
      var eduDegreeNames = ""
      if (lesson.teachers != null) {
        val singleTeacher = if (lesson.teachers.size == 1) true else false
        var iter = lesson.teachers.iterator
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
      if (lesson.teachers != null) {
        val singleTeacher = if (lesson.teachers.size == 1) true else false
        var iter = lesson.teachers.iterator
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
      getPropertyIn(lesson.teachers, "teacherType.name")
    } else if ("teachers.department.name" == property) {
      getPropertyIn(lesson.teachers, "department.name")
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
      lesson.semester.schoolYear + '-' + lesson.semester.name
    } else if ("fake.auditStatus" == property) {
      lesson.auditStatus.fullName
    } else if ("fake.arrangesuggest" == property) {
      val arrangeSuggestQuery = OqlBuilder.from(classOf[ArrangeSuggest], "suggest")
      arrangeSuggestQuery.where("suggest.lesson = :lesson", lesson)
      val suggests = entityDao.search(arrangeSuggestQuery)
      if (Collections.isNotEmpty(suggests)) {
        val suggest = suggests.get(0)
        var str = suggestDigestor.digest(textResource, suggest)
        if (Collections.isNotEmpty(suggest.getRooms)) {
          str += "建议教室:" + 
            Strings.join(CollectionUtils.collect(suggest.getRooms, new Transformer() {

            def transform(input: AnyRef): AnyRef = {
              return input.asInstanceOf[Room].getName
            }
          }), ",")
        }
        if (Collections.isNotEmpty(suggest.getRooms)) {
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
      lesson.semester.schoolYear + " " + lesson.semester.name
    } else if ("courseSchedule.activities.weeks" == property) {
      digestor.digest(textResource, lesson, CourseActivityDigestor.weeks)
    } else if ("fake.weeks" == property) {
      String.valueOf(lesson.schedule.endWeek - lesson.schedule.startWeek + 
        1)
    } else if ("fake.arrange" == property) {
      digestor.digest(textResource, lesson)
    } else if ("roomType.name" == property) {
      if (lesson.schedule.roomType != null) {
        return lesson.schedule.roomType.name
      }
      ""
    } else if ("fake.courseSchedule.practiceHour" == property) {
      var `type`: CourseHourType = null
      for (hourType <- hourTypes if hourType.name.indexOf("实践") != -1) {
        `type` = hourType
      }
      if (`type` != null) {
        lesson.course.getHour(`type`)
      }
      null
    } else if ("fake.courseSchedule.theoryHour" == property) {
      var `type`: CourseHourType = null
      for (hourType <- hourTypes if hourType.name.indexOf("理论") != -1) {
        `type` = hourType
      }
      if (`type` != null) {
        lesson.course.getHour(`type`)
      }
      null
    } else if ("fake.courseSchedule.operateHour" == property) {
      var `type`: CourseHourType = null
      for (hourType <- hourTypes if hourType.name.indexOf("上机") != -1 || hourType.name.indexOf("操作") != -1) {
        `type` = hourType
      }
      if (`type` != null) {
        return lesson.course.getHour(`type`)
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
      val rooms = Collections.newSet[Room]
      for (activity <- lesson.schedule.activities) {
        rooms ++= activity.rooms
      }
      val seats = Array.ofDim[Integer](rooms.size)
      val i = 0
      for (room <- rooms) {
        seats(i += 1) = room.capacity
      }
      Strings.join(seats, ',')
    } else if ("courseSchedule.activities.time" == property) {
      digestor.digest(textResource, lesson, ":day :units")
    } else if ("courseSchedule.activities.room" == property) {
      val rooms = Collections.newSet[Room]
      for (activity <- lesson.schedule.activities) {
        rooms ++= activity.rooms
      }
      val names = Array.ofDim[String](rooms.size)
      val i = 0
      for (room <- rooms) {
        names(i += 1) = room.name
      }
      Strings.join(names, ",")
    } else if ("exam.date" == property) {
      examDigestor.digest(lesson.exam.activities(examType), textResource, ExamActivityDigestor.weeks + " " + ExamActivityDigestor.day)
    } else if ("exam.time" == property) {
      val format = new StringBuilder()
      format.append(ExamActivityDigestor.weeks)
      format.append(" ")
      format.append(ExamActivityDigestor.day)
      examDigestor.digest(lesson.exam.activities(examType), textResource, format.toString)
    } else if ("exam.rooms" == property) {
      val format = new StringBuilder()
      format.append(ExamActivityDigestor.room)
      format.append("(").append(ExamActivityDigestor.district)
        .append(", ")
        .append(ExamActivityDigestor.building)
        .append(")")
      examDigestor.digest(lesson.exam.activities(examType), textResource, format.toString)
    } else if ("exam.department.name" == property) {
      val value = new StringBuilder()
      val activity = lesson.exam.activities(examType)
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
      getPropertyIn(lesson.exam .getExaminers(examType), "name")
    } else if ("task.term" == property) {
      var term = 0
      val termCalc = new TermCalculator(semesterService, lesson.semester)
      term = termCalc.getTerm(lesson.teachClass.grade, true)
      new java.lang.Long(term)
    } else if ("task.courseSchedule" == property) {
      var arrangeInfo = ""
      val weeks = lesson.schedule.weeks
      val weekUnits = lesson.schedule.weekHour
      if (0 != weeks && 0 != weekUnits) {
        arrangeInfo = weeks + "*" + weekUnits
      }
      arrangeInfo
    } else if ("education.name" == property) {
      val pair = lessonLimitService.xtractEducationLimit(lesson.teachClass)
      var educationStr = ""
      if (pair._1 == Operator.NOT_IN || pair._1 == Operator.NOT_EQUAL) {
        val educationsForCurrPorject = lesson.project.educations.asInstanceOf[collection.mutable.Buffer[Education]]
        educationsForCurrPorject --= pair._2
        for (education <- educationsForCurrPorject) {
          educationStr += education.name + ","
        }
        if (educationStr.length > 0) {
          educationStr = educationStr.substring(0, educationStr.length - 1)
        }
        return educationStr
      }
      if (pair._1 == Operator.IN || pair._1 == Operator.Equals) {
        for (education <- pair._2) {
          educationStr += education.name + ","
        }
        if (educationStr.length > 0) {
          educationStr = educationStr.substring(0, educationStr.length - 1)
        }
        return educationStr
      }
      super.getPropertyValue(target, property)
    } else if ("teachLang.name" == property) {
      if (lesson.langType != null) {
        return lesson.langType.name
      }
      ""
    } else {
      super.getPropertyValue(target, property)
    }
  }

  private def getContents(lesson: Lesson): collection.mutable.Map[String, String] = {
    val lessonId = lesson.id
    var contents: collection.mutable.Map[String, String] = null
    if (null == currLimitContents || currLimitContents._1  != lessonId) {
      var fullname = lesson.teachClass.fullname
      if (fullname.endsWith("...")) {
        fullname = fullname.substring(0, fullname.length - 3)
      }
      if (fullname.startsWith(",")) {
        fullname = fullname.substring(1)
      }
      contents = Collections.newMap[String,String]
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
      currLimitContents = new Pair[Long, collection.mutable.Map[String, String]](lessonId, contents)
      contents
    }
    currLimitContents._2 
  }
}
