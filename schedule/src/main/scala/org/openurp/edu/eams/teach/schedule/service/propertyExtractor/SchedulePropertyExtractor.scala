package org.openurp.edu.eams.teach.schedule.service.propertyExtractor


import org.beangle.data.model.dao.EntityDao
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.TextResource
import org.beangle.commons.transfer.exporter.DefaultPropertyExtractor
import org.openurp.edu.base.Teacher
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.util.CourseActivityDigestor



class SchedulePropertyExtractor(textResource: TextResource) extends DefaultPropertyExtractor(textResource) {

  private var entityDao: EntityDao = _

  private var weekState: java.lang.Long = _

  private var courseUnit: java.lang.Integer = _

  private var weekday: java.lang.Integer = _

  private var buildingId: java.lang.Long = _

  private var format: String = ":units节  第:weeks周 :room"

  def this(textResource: TextResource, entityDao: EntityDao) {
    super(textResource)
    this.entityDao = entityDao
  }

  def getPropertyValue(target: AnyRef, property: String): AnyRef = {
    val lesson = target.asInstanceOf[Lesson]
    val teachers = lesson.getTeachers
    val digestor = CourseActivityDigestor.getInstance.setDelimeter(";")
    val builder = OqlBuilder.from(classOf[CourseActivity], "activity")
    builder.where("activity.lesson = :lesson", lesson)
    if (null != weekday) builder.where("activity.time.day = " + weekday)
    if (null != courseUnit) builder.where("activity.time.startUnit <= " + courseUnit + " and activity.time.endUnit >= " + 
      courseUnit)
    if (null != weekState) builder.where("bitand(activity.time.state," + weekState + ")>0")
    if (null != buildingId) builder.where("exists(from courseActivity.rooms as cr where cr.building.id=" + 
      buildingId + 
      ")")
    if ("arrangeInfo" == property) {
      digestor.digest(textResource, lesson, ":day :units :weeks :room :roomCode")
    } else if ("monday" == property) {
      builder.where("activity.time.day = 1")
      val activities = entityDao.search(builder)
      if (activities.isEmpty) {
        ""
      } else {
        digestor.digest(textResource, activities, format)
      }
    } else if ("tuesday" == property) {
      builder.where("activity.time.day = 2")
      val activities = entityDao.search(builder)
      if (activities.isEmpty) {
        ""
      } else {
        digestor.digest(textResource, activities, format)
      }
    } else if ("wednesday" == property) {
      builder.where("activity.time.day = 3")
      val activities = entityDao.search(builder)
      if (activities.isEmpty) {
        ""
      } else {
        digestor.digest(textResource, activities, format)
      }
    } else if ("thursday" == property) {
      builder.where("activity.time.day = 4")
      val activities = entityDao.search(builder)
      if (activities.isEmpty) {
        ""
      } else {
        digestor.digest(textResource, activities, format)
      }
    } else if ("friday" == property) {
      builder.where("activity.time.day = 5")
      val activities = entityDao.search(builder)
      if (activities.isEmpty) {
        ""
      } else {
        digestor.digest(textResource, activities, format)
      }
    } else if ("saturday" == property) {
      builder.where("activity.time.day = 6")
      val activities = entityDao.search(builder)
      if (activities.isEmpty) {
        ""
      } else {
        digestor.digest(textResource, activities, format)
      }
    } else if ("sunday" == property) {
      builder.where("activity.time.day = 7")
      val activities = entityDao.search(builder)
      if (activities.isEmpty) {
        ""
      } else {
        digestor.digest(textResource, activities, format)
      }
    } else if ("teachers" == property) {
      if (teachers.size == 0) {
        return "未安排教师"
      }
      var teacherName = ""
      for (i <- 0 until teachers.size) {
        if (i > 0) {
          teacherName += ","
        }
        teacherName += teachers.get(i).getName
      }
      teacherName
    } else if ("teacherTitles" == property) {
      val titles = new StringBuilder()
      for (i <- 0 until teachers.size) {
        if (i > 0) {
          titles.append(",")
        }
        if (null == teachers.get(i).getTitle) titles.append("无") else titles.append(teachers.get(i).getTitle.getName)
      }
      titles.toString
    } else {
      super.getPropertyValue(target, property)
    }
  }

  def setWeekState(weekState: java.lang.Long) {
    this.weekState = weekState
    this.format = Strings.replace(format, " 第:weeks周", "")
  }

  def setCourseUnit(courseUnit: java.lang.Integer) {
    this.courseUnit = courseUnit
    if (null != courseUnit) {
      this.format = ":room"
    }
  }

  def setWeekday(weekday: java.lang.Integer) {
    this.weekday = weekday
    if (null != weekday) this.format = Strings.replace(format, " 第:weeks周", "")
  }

  def setBuildingId(buildingId: java.lang.Long) {
    this.buildingId = buildingId
  }
}
