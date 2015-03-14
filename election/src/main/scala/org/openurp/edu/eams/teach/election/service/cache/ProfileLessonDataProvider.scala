package org.openurp.edu.eams.teach.election.service.cache

import java.util.List
import java.util.Map
import java.util.Set
import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.edu.eams.base.Campus
import org.openurp.base.Department
import org.openurp.edu.eams.base.model.CampusBean
import org.openurp.edu.eams.base.model.DepartmentBean
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.eams.core.model.MajorBean
import org.openurp.edu.eams.core.model.ProjectBean
import org.openurp.edu.teach.Course
import org.openurp.edu.eams.teach.code.school.CourseAbilityRate
import org.openurp.edu.teach.code.CourseType
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.ElectionProfileBean
import org.openurp.edu.eams.teach.lesson.CourseActivity
import org.openurp.edu.teach.lesson.CourseLimitGroup
import org.openurp.edu.teach.lesson.CourseLimitItem
import org.openurp.edu.teach.lesson.CourseLimitMeta
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.CourseLimitGroupBean
import org.openurp.edu.eams.teach.lesson.model.CourseLimitItemBean
import org.openurp.edu.eams.teach.lesson.model.CourseLimitMetaBean
import org.openurp.edu.eams.teach.lesson.model.LessonBean
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.teach.model.CourseBean
import freemarker.template.utility.StringUtil
import ProfileLessonDataProvider._

import scala.collection.JavaConversions._

object ProfileLessonDataProvider {

  private val refreshInterval = 1000 * 60 * 10

  private val profileId2LessonJson = CollectUtils.newHashMap()

  private val profileId2LastUpdateTime = CollectUtils.newHashMap()

  private val profileId2Lessons = CollectUtils.newHashMap()

  private val urgentQuery = OqlBuilder.from(classOf[ElectionProfile].getName, "p")
    .select("p.id")
    .where("current_time() between p.beginAt-1/24 and p.endAt")
}

class ProfileLessonDataProvider extends AbstractProfileLessonProvider {

  def run() {
    synchronized (this) {
      try {
        while (true) {
          val urgentPids = entityDao.search(urgentQuery).asInstanceOf[List[Long]]
          for (profileId <- urgentPids) {
            profileId2LessonJson.put(profileId, makeLessonJsons(profileId))
            profileId2LastUpdateTime.put(profileId, System.currentTimeMillis())
          }
          profileId2Lessons.keySet.retainAll(urgentPids)
          profileId2LessonJson.keySet.retainAll(urgentPids)
          profileId2LastUpdateTime.keySet.retainAll(urgentPids)
          this.wait(refreshInterval)
        }
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
    }
  }

  def notifyThread() {
    synchronized (this) {
      this.notify()
    }
  }

  def getIdToJson(profileId: java.lang.Long): Map[Long, String] = profileId2LessonJson.get(profileId)

  def getLastUpdateTime(profileId: java.lang.Long): String = {
    String.valueOf(profileId2LastUpdateTime.get(profileId))
  }

  def getLessons(profileId: java.lang.Long): List[Lesson] = {
    if (profileId2Lessons.isEmpty) {
      makeLessonJsons(profileId)
    }
    profileId2Lessons.get(profileId)
  }

  private def makeLessonJsons(profileId: java.lang.Long): Map[Long, String] = {
    val start = System.currentTimeMillis()
    val resultDatas = genNewData(profileId)
    val lessons = resultDatas.get("lessons").asInstanceOf[List[Lesson]]
    profileId2Lessons.put(profileId, cloneLessons(lessons))
    val profile = resultDatas.get("profile").asInstanceOf[ElectionProfile]
    val activityInfoMap = resultDatas.get("activityInfoMap").asInstanceOf[Map[Long, Map[Long, Array[Any]]]]
    val activityRooms = resultDatas.get("activityRooms").asInstanceOf[Map[Long, String]]
    val lessonTeachers = resultDatas.get("lessonTeachers").asInstanceOf[Map[Long, String]]
    val res = CollectUtils.newHashMap()
    val tmp_sb = new StringBuilder(550)
    val lessonsSize = lessons.size
    for (i <- 0 until lessonsSize) {
      val lesson = lessons.get(i)
      val credits = lesson.getCourse.getCredits
      val scheduled = activityInfoMap.containsKey(lesson.getId)
      val withdrawable = profile.getWithdrawableLessons.contains(lesson.getId)
      val teachers = lessonTeachers.get(lesson.getId)
      val campus = lesson.getCampus
      val remark = lesson.getRemark
      tmp_sb.append("{id:").append(lesson.getId).append(",no:'")
        .append(lesson.getNo)
        .append("',name:'")
        .append(StringUtil.javaScriptStringEnc(lesson.getCourse.getName))
        .append('\'')
      tmp_sb.append(",code:'").append(lesson.getCourse.getCode)
        .append("',")
        .append("credits:")
        .append(credits)
      tmp_sb.append(",courseId:").append(lesson.getCourse.getId)
      tmp_sb.append(",startWeek:").append(lesson.getCourseSchedule.getStartWeek)
        .append(",endWeek:")
        .append(lesson.getCourseSchedule.getEndWeek)
      tmp_sb.append(",courseTypeId:").append(lesson.getCourseType.getId)
        .append(",courseTypeName:'")
        .append(lesson.getCourseType.getName)
        .append('\'')
      tmp_sb.append(",courseTypeCode:'").append(lesson.getCourseType.getCode)
        .append('\'')
      tmp_sb.append(",scheduled:").append(scheduled).append(",hasTextBook:false,period:")
        .append(lesson.getCourse.getPeriod)
      tmp_sb.append(",weekHour:").append(lesson.getCourse.getWeekHour)
        .append(",withdrawable:")
        .append(withdrawable)
      tmp_sb.append(",textbooks:''").append(",teachers:'")
      if (teachers != null) {
        tmp_sb.append(StringUtil.javaScriptStringEnc(teachers))
      }
      tmp_sb.append('\'')
      tmp_sb.append(",campusCode:'")
      if (campus != null) {
        tmp_sb.append(campus.getCode)
      }
      tmp_sb.append('\'')
      tmp_sb.append(",campusName:'")
      if (campus != null) {
        tmp_sb.append(campus.getName)
      }
      tmp_sb.append('\'')
      tmp_sb.append(",remark:'")
      if (remark != null) {
        tmp_sb.append(StringUtil.javaScriptStringEnc(remark))
      }
      tmp_sb.append('\'')
      tmp_sb.append(",arrangeInfo:[")
      val arranges = activityInfoMap.get(lesson.getId)
      if (arranges != null) {
        val arrangeSize = arranges.keySet.size
        var j = 0
        for (activityId <- arranges.keySet) {
          val rooms = activityRooms.get(activityId)
          tmp_sb.append("{weekDay:").append(arranges.get(activityId)(0))
          tmp_sb.append(",weekState:'").append(arranges.get(activityId)(1))
            .append('\'')
          tmp_sb.append(",startUnit:").append(arranges.get(activityId)(2))
          tmp_sb.append(",endUnit:").append(arranges.get(activityId)(3))
          tmp_sb.append(",rooms:'")
          if (rooms != null) {
            tmp_sb.append(rooms)
          }
          tmp_sb.append('\'')
          tmp_sb.append('}')
          if (j < arrangeSize - 1) {
            tmp_sb.append(',')
          }
          j += 1
        }
      }
      tmp_sb.append("]")
      tmp_sb.append("}")
      res.put(lesson.getId, tmp_sb.toString)
      tmp_sb.delete(0, tmp_sb.length)
    }
    logger.debug("Render Lesson Json in " + (System.currentTimeMillis() - start) + 
      "ms")
    res
  }

  private def genNewData(profileId: java.lang.Long): Map[String, Any] = {
    val data = CollectUtils.newHashMap()
    val profile = entityDao.get(classOf[ElectionProfileBean], profileId)
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.semester=:semester", profile.getSemester)
    builder.where("exists (from " + classOf[ElectionProfile].getName + 
      " profile join profile.electableLessons electableLessonId where profile.id=:profile and electableLessonId=lesson.id)", 
      profile.getId)
    builder.orderBy("lesson.campus.id,lesson.course.code")
    val lessons = entityDao.search(builder)
    buildDataInfos(data, lessons)
    data.put("lessons", lessons)
    data.put("profile", profile)
    data
  }

  def buildDataInfos(data: Map[String, Any], lessons: List[Lesson]) {
    val teacherNames = CollectUtils.newArrayList()
    val teacherBuilder = OqlBuilder.from(classOf[Lesson].getName + " lesson")
    teacherBuilder.where("lesson in (:lessons)")
    teacherBuilder.join("lesson.teachers", "teacher")
    teacherBuilder.select("lesson.id,teacher.name")
    var i = 0
    while (i < lessons.size) {
      var end = i + 500
      if (end > lessons.size) {
        end = lessons.size
      }
      teacherBuilder.param("lessons", ArrayUtils.subarray(lessons.toArray(), i, end))
      teacherNames.addAll(entityDao.search(teacherBuilder).asInstanceOf[List[Array[Any]]])
      i += 500
    }
    val lessonTeachers = CollectUtils.newHashMap()
    for (objects <- teacherNames) {
      val lessonId = objects(0).asInstanceOf[java.lang.Long]
      val teacherName = objects(1)
      if (teacherName != null) {
        val existsTeacherName = "," + lessonTeachers.get(lessonId) + ","
        if (lessonTeachers.containsKey(lessonId) && 
          !Strings.contains(existsTeacherName, "," + teacherName.toString + ",")) {
          lessonTeachers.put(lessonId, lessonTeachers.get(lessonId) + "," + teacherName.toString)
        } else {
          lessonTeachers.put(lessonId, teacherName.toString)
        }
      }
    }
    val roomNames = CollectUtils.newArrayList()
    val roomBuilder = OqlBuilder.from(classOf[CourseActivity].getName + " activity")
    roomBuilder.where("activity.lesson in (:lessons)")
    roomBuilder.join("activity.rooms", "room")
    roomBuilder.select("activity.id,room.name")
    var j = 0
    while (j < lessons.size) {
      var end = j + 500
      if (end > lessons.size) {
        end = lessons.size
      }
      roomBuilder.param("lessons", ArrayUtils.subarray(lessons.toArray(), j, end))
      roomNames.addAll(entityDao.search(roomBuilder).asInstanceOf[List[Array[Any]]])
      j += 500
    }
    val activityRooms = CollectUtils.newHashMap()
    for (objects <- roomNames) {
      val lessonId = objects(0).asInstanceOf[java.lang.Long]
      val roomName = objects(1)
      if (roomName != null) {
        val existsRoomName = "," + activityRooms.get(lessonId) + ","
        if (activityRooms.containsKey(lessonId) && 
          !Strings.contains(existsRoomName, "," + roomName.toString + ",")) {
          activityRooms.put(lessonId, activityRooms.get(lessonId) + "," + roomName.toString)
        } else {
          activityRooms.put(lessonId, roomName.toString)
        }
      }
    }
    val activityInfos = CollectUtils.newArrayList()
    val activityInfoBuilder = OqlBuilder.from(classOf[CourseActivity].getName + " activity")
    activityInfoBuilder.where("activity.lesson in (:lessons)")
    activityInfoBuilder.select("activity.lesson.id,activity.id,activity.time.weekday,activity.time.weekState,activity.time.startUnit,activity.time.endUnit")
    var k = 0
    while (k < lessons.size) {
      var end = k + 500
      if (end > lessons.size) {
        end = lessons.size
      }
      activityInfoBuilder.param("lessons", ArrayUtils.subarray(lessons.toArray(), k, end))
      activityInfos.addAll(entityDao.search(activityInfoBuilder).asInstanceOf[List[Array[Any]]])
      k += 500
    }
    val activityInfoMap = CollectUtils.newHashMap()
    for (objects <- activityInfos) {
      val infos = Array.ofDim[Any](4)
      for (l <- 2 until objects.length) {
        infos(l - 2) = objects(l)
      }
      if (activityInfoMap.containsKey(objects(0).asInstanceOf[java.lang.Long])) {
        activityInfoMap.get(objects(0)).put(objects(1).asInstanceOf[java.lang.Long], infos)
      } else {
        val subInfos = CollectUtils.newHashMap()
        subInfos.put(objects(1).asInstanceOf[java.lang.Long], infos)
        activityInfoMap.put(objects(0).asInstanceOf[java.lang.Long], subInfos)
      }
    }
    data.put("activityInfoMap", activityInfoMap)
    data.put("activityRooms", activityRooms)
    data.put("lessonTeachers", lessonTeachers)
  }

  private def cloneLessons(lessons: List[Lesson]): List[Lesson] = {
    val start = System.currentTimeMillis()
    val courses = CollectUtils.newHashMap()
    val courseTypes = CollectUtils.newHashMap()
    val educations = CollectUtils.newHashMap()
    val campuses = CollectUtils.newHashMap()
    val departs = CollectUtils.newHashMap()
    val projects = CollectUtils.newHashMap()
    val metas = CollectUtils.newHashMap()
    val abilityRates = CollectUtils.newHashMap()
    val majors = CollectUtils.newHashMap()
    val courseRateInfos = CollectUtils.newArrayList()
    val rateBuilder = OqlBuilder.from(classOf[Lesson], "lesson")
    rateBuilder.where("lesson in (:lessons)")
    rateBuilder.join("lesson.course.abilityRates", "abilityRate")
    rateBuilder.select("lesson.course.id,abilityRate.id")
    var k = 0
    while (k < lessons.size) {
      var end = k + 500
      if (end > lessons.size) {
        end = lessons.size
      }
      rateBuilder.param("lessons", ArrayUtils.subarray(lessons.toArray(), k, end))
      courseRateInfos.addAll(entityDao.search(rateBuilder).asInstanceOf[List[Array[Any]]])
      k += 500
    }
    val courseRates = CollectUtils.newHashMap()
    for (objects <- courseRateInfos) {
      val courseId = objects(0).asInstanceOf[java.lang.Long]
      val rateId = objects(1).asInstanceOf[java.lang.Integer]
      if (courseRates.containsKey(courseId)) {
        courseRates.get(courseId).add(rateId)
      } else {
        courseRates.put(courseId, CollectUtils.newHashSet(rateId))
      }
    }
    val courseXmajorInfos = CollectUtils.newArrayList()
    val xmajorBuilder = OqlBuilder.from(classOf[Lesson], "lesson")
    xmajorBuilder.where("lesson in (:lessons)")
    xmajorBuilder.join("lesson.course.xmajors", "xmajor")
    xmajorBuilder.select("lesson.course.id,xmajor.id")
    k = 0
    while (k < lessons.size) {
      var end = k + 500
      if (end > lessons.size) {
        end = lessons.size
      }
      xmajorBuilder.param("lessons", ArrayUtils.subarray(lessons.toArray(), k, end))
      courseXmajorInfos.addAll(entityDao.search(xmajorBuilder).asInstanceOf[List[Array[Any]]])
      k += 500
    }
    val courseXmajors = CollectUtils.newHashMap()
    for (objects <- courseXmajorInfos) {
      val courseId = objects(0).asInstanceOf[java.lang.Long]
      val xmajorId = objects(1).asInstanceOf[java.lang.Integer]
      if (courseXmajors.containsKey(objects)) {
        courseXmajors.get(courseId).add(xmajorId)
      } else {
        courseXmajors.put(courseId, CollectUtils.newHashSet(xmajorId))
      }
    }
    val result = CollectUtils.newArrayList(lessons.size)
    for (l <- lessons) {
      val nl = new LessonBean()
      result.add(nl)
      nl.setId(l.getId)
      nl.setNo(l.getNo)
      val courseTypeId = l.getCourseType.getId
      var courseType = courseTypes.get(courseTypeId)
      if (null == courseType) {
        courseType = new CourseType(courseTypeId)
        courseType.setCode(l.getCourseType.getCode)
        courseTypes.put(courseTypeId, courseType)
      }
      nl.setCourseType(courseType)
      if (null != l.getCampus) {
        val campusId = l.getCampus.getId
        var campus = campuses.get(campusId)
        if (null == campus) {
          campus = new CampusBean(campusId)
          campuses.put(campusId, campus)
        }
        nl.setCampus(campus)
      }
      val courseId = l.getCourse.getId
      var course = courses.get(courseId)
      if (null == course) {
        course = new CourseBean(courseId)
        course.setCode(l.getCourse.getCode)
        course.setCredits(l.getCourse.getCredits)
        if (null != l.getCourse.education) {
          val educationId = l.getCourse.education.getId
          var education = educations.get(educationId)
          if (null == education) {
            education = new Education(educationId)
            educations.put(educationId, education)
          }
          course.setEducation(education)
        }
        if (courseRates.get(l.getCourse.getId) != null) {
          for (orate <- courseRates.get(l.getCourse.getId)) {
            var rate = abilityRates.get(orate)
            if (null == rate) {
              rate = new CourseAbilityRate()
              rate.setId(orate)
              abilityRates.put(orate, rate)
            }
            course.getAbilityRates.add(rate)
          }
        }
        if (courseXmajors.get(l.getCourse.getId) != null) {
          for (omajor <- courseXmajors.get(l.getCourse.getId)) {
            var major = majors.get(omajor)
            if (null == major) {
              major = new MajorBean(omajor)
              majors.put(omajor, major)
            }
            course.getXmajors.add(major)
          }
        }
        courses.put(courseId, course)
      }
      nl.setCourse(course)
      val departId = l.getTeachDepart.getId
      var depart = departs.get(departId)
      if (null == depart) {
        depart = new DepartmentBean(departId)
        depart.setCode(l.getTeachDepart.getCode)
        departs.put(departId, depart)
      }
      nl.setTeachDepart(depart)
      val projectId = l.getProject.getId
      var project = projects.get(projectId)
      if (null == project) {
        project = new ProjectBean(projectId)
        projects.put(projectId, project)
      }
      nl.setProject(project)
      val teachclass = new TeachClassBean()
      teachclass.setLesson(nl)
      teachclass.setGrade(l.getTeachClass.grade)
      for (og <- l.getTeachClass.getLimitGroups) {
        val group = new CourseLimitGroupBean()
        teachclass.addLimitGroups(group)
        group.setId(og.getId)
        group.setForClass(og.isForClass)
        for (oi <- og.getItems) {
          val item = new CourseLimitItemBean()
          item.setId(oi.getId)
          item.setOperator(oi.getOperator)
          item.setContent(oi.getContent)
          var meta = metas.get(oi.getMeta.getId)
          if (null == meta) {
            meta = new CourseLimitMetaBean()
            meta.setId(oi.getMeta.getId)
            metas.put(oi.getMeta.getId, meta)
          }
          item.setMeta(meta)
          group.getItems.add(item)
        }
      }
      nl.setTeachClass(teachclass)
    }
    logger.debug("clone {} lessons using {}", lessons.size, System.currentTimeMillis() - start)
    result
  }
}
