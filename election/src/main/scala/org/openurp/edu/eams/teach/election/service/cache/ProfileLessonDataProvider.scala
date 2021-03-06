package org.openurp.edu.eams.teach.election.service.cache




import org.apache.commons.lang3.ArrayUtils
import org.beangle.commons.collection.Collections
import org.beangle.data.jpa.dao.OqlBuilder
import org.beangle.commons.lang.Strings
import org.openurp.base.Campus
import org.openurp.base.Department
import org.openurp.edu.eams.base.model.CampusBean
import org.openurp.edu.eams.base.model.DepartmentBean
import org.openurp.edu.base.Major
import org.openurp.edu.base.Project
import org.openurp.code.edu.Education
import org.openurp.edu.eams.core.model.MajorBean
import org.openurp.edu.eams.core.model.ProjectBean
import org.openurp.edu.base.Course
import org.openurp.edu.eams.teach.code.school.CourseAbilityRate
import org.openurp.edu.base.code.CourseType
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.eams.teach.election.model.ElectionProfileBean
import org.openurp.edu.teach.schedule.CourseActivity
import org.openurp.edu.teach.lesson.LessonLimitGroup
import org.openurp.edu.teach.lesson.LessonLimitItem
import org.openurp.edu.teach.lesson.LessonLimitMeta
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.model.LessonLimitGroupBean
import org.openurp.edu.eams.teach.lesson.model.LessonLimitItemBean
import org.openurp.edu.eams.teach.lesson.model.LessonLimitMetaBean
import org.openurp.edu.eams.teach.lesson.model.LessonBean
import org.openurp.edu.teach.lesson.model.TeachClassBean
import org.openurp.edu.teach.model.CourseBean
import freemarker.template.utility.StringUtil
import ProfileLessonDataProvider._



object ProfileLessonDataProvider {

  private val refreshInterval = 1000 * 60 * 10

  private val profileId2LessonJson = Collections.newMap[Any]

  private val profileId2LastUpdateTime = Collections.newMap[Any]

  private val profileId2Lessons = Collections.newMap[Any]

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

  def idToJson(profileId: java.lang.Long): Map[Long, String] = profileId2LessonJson.get(profileId)

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
    val res = Collections.newMap[Any]
    val tmp_sb = new StringBuilder(550)
    val lessonsSize = lessons.size
    for (i <- 0 until lessonsSize) {
      val lesson = lessons.get(i)
      val credits = lesson.getCourse.getCredits
      val scheduled = activityInfoMap.containsKey(lesson.id)
      val withdrawable = profile.getWithdrawableLessons.contains(lesson.id)
      val teachers = lessonTeachers.get(lesson.id)
      val campus = lesson.getCampus
      val remark = lesson.getRemark
      tmp_sb.append("{id:").append(lesson.id).append(",no:'")
        .append(lesson.getNo)
        .append("',name:'")
        .append(StringUtil.javaScriptStringEnc(lesson.getCourse.getName))
        .append('\'')
      tmp_sb.append(",code:'").append(lesson.getCourse.getCode)
        .append("',")
        .append("credits:")
        .append(credits)
      tmp_sb.append(",courseId:").append(lesson.getCourse.id)
      tmp_sb.append(",startWeek:").append(lesson.getCourseSchedule.getStartWeek)
        .append(",endWeek:")
        .append(lesson.getCourseSchedule.getEndWeek)
      tmp_sb.append(",courseTypeId:").append(lesson.getCourseType.id)
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
      val arranges = activityInfoMap.get(lesson.id)
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
      res.put(lesson.id, tmp_sb.toString)
      tmp_sb.delete(0, tmp_sb.length)
    }
    logger.debug("Render Lesson Json in " + (System.currentTimeMillis() - start) + 
      "ms")
    res
  }

  private def genNewData(profileId: java.lang.Long): Map[String, Any] = {
    val data = Collections.newMap[Any]
    val profile = entityDao.get(classOf[ElectionProfileBean], profileId)
    val builder = OqlBuilder.from(classOf[Lesson], "lesson")
    builder.where("lesson.semester=:semester", profile.getSemester)
    builder.where("exists (from " + classOf[ElectionProfile].getName + 
      " profile join profile.electableLessons electableLessonId where profile.id=:profile and electableLessonId=lesson.id)", 
      profile.id)
    builder.orderBy("lesson.campus.id,lesson.course.code")
    val lessons = entityDao.search(builder)
    buildDataInfos(data, lessons)
    data.put("lessons", lessons)
    data.put("profile", profile)
    data
  }

  def buildDataInfos(data: Map[String, Any], lessons: List[Lesson]) {
    val teacherNames = Collections.newBuffer[Any]
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
    val lessonTeachers = Collections.newMap[Any]
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
    val roomNames = Collections.newBuffer[Any]
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
    val activityRooms = Collections.newMap[Any]
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
    val activityInfos = Collections.newBuffer[Any]
    val activityInfoBuilder = OqlBuilder.from(classOf[CourseActivity].getName + " activity")
    activityInfoBuilder.where("activity.lesson in (:lessons)")
    activityInfoBuilder.select("activity.lesson.id,activity.id,activity.time.day,activity.time.state,activity.time.startUnit,activity.time.endUnit")
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
    val activityInfoMap = Collections.newMap[Any]
    for (objects <- activityInfos) {
      val infos = Array.ofDim[Any](4)
      for (l <- 2 until objects.length) {
        infos(l - 2) = objects(l)
      }
      if (activityInfoMap.containsKey(objects(0).asInstanceOf[java.lang.Long])) {
        activityInfoMap.get(objects(0)).put(objects(1).asInstanceOf[java.lang.Long], infos)
      } else {
        val subInfos = Collections.newMap[Any]
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
    val courses = Collections.newMap[Any]
    val courseTypes = Collections.newMap[Any]
    val educations = Collections.newMap[Any]
    val campuses = Collections.newMap[Any]
    val departs = Collections.newMap[Any]
    val projects = Collections.newMap[Any]
    val metas = Collections.newMap[Any]
    val abilityRates = Collections.newMap[Any]
    val majors = Collections.newMap[Any]
    val courseRateInfos = Collections.newBuffer[Any]
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
    val courseRates = Collections.newMap[Any]
    for (objects <- courseRateInfos) {
      val courseId = objects(0).asInstanceOf[java.lang.Long]
      val rateId = objects(1).asInstanceOf[java.lang.Integer]
      if (courseRates.containsKey(courseId)) {
        courseRates.get(courseId).add(rateId)
      } else {
        courseRates.put(courseId, Collections.newHashSet(rateId))
      }
    }
    val courseXmajorInfos = Collections.newBuffer[Any]
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
    val courseXmajors = Collections.newMap[Any]
    for (objects <- courseXmajorInfos) {
      val courseId = objects(0).asInstanceOf[java.lang.Long]
      val xmajorId = objects(1).asInstanceOf[java.lang.Integer]
      if (courseXmajors.containsKey(objects)) {
        courseXmajors.get(courseId).add(xmajorId)
      } else {
        courseXmajors.put(courseId, Collections.newHashSet(xmajorId))
      }
    }
    val result = Collections.newBuffer[Any](lessons.size)
    for (l <- lessons) {
      val nl = new LessonBean()
      result.add(nl)
      nl.setId(l.id)
      nl.setNo(l.getNo)
      val courseTypeId = l.getCourseType.id
      var courseType = courseTypes.get(courseTypeId)
      if (null == courseType) {
        courseType = new CourseType(courseTypeId)
        courseType.setCode(l.getCourseType.getCode)
        courseTypes.put(courseTypeId, courseType)
      }
      nl.setCourseType(courseType)
      if (null != l.getCampus) {
        val campusId = l.getCampus.id
        var campus = campuses.get(campusId)
        if (null == campus) {
          campus = new CampusBean(campusId)
          campuses.put(campusId, campus)
        }
        nl.setCampus(campus)
      }
      val courseId = l.getCourse.id
      var course = courses.get(courseId)
      if (null == course) {
        course = new CourseBean(courseId)
        course.setCode(l.getCourse.getCode)
        course.setCredits(l.getCourse.getCredits)
        if (null != l.getCourse.education) {
          val educationId = l.getCourse.education.id
          var education = educations.get(educationId)
          if (null == education) {
            education = new Education(educationId)
            educations.put(educationId, education)
          }
          course.setEducation(education)
        }
        if (courseRates.get(l.getCourse.id) != null) {
          for (orate <- courseRates.get(l.getCourse.id)) {
            var rate = abilityRates.get(orate)
            if (null == rate) {
              rate = new CourseAbilityRate()
              rate.setId(orate)
              abilityRates.put(orate, rate)
            }
            course.getAbilityRates.add(rate)
          }
        }
        if (courseXmajors.get(l.getCourse.id) != null) {
          for (omajor <- courseXmajors.get(l.getCourse.id)) {
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
      val departId = l.getTeachDepart.id
      var depart = departs.get(departId)
      if (null == depart) {
        depart = new DepartmentBean(departId)
        depart.setCode(l.getTeachDepart.getCode)
        departs.put(departId, depart)
      }
      nl.setTeachDepart(depart)
      val projectId = l.getProject.id
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
        val group = new LessonLimitGroupBean()
        teachclass.addLimitGroups(group)
        group.setId(og.id)
        group.setForClass(og.isForClass)
        for (oi <- og.getItems) {
          val item = new LessonLimitItemBean()
          item.setId(oi.id)
          item.setOperator(oi.getOperator)
          item.setContent(oi.getContent)
          var meta = metas.get(oi.getMeta.id)
          if (null == meta) {
            meta = new LessonLimitMetaBean()
            meta.setId(oi.getMeta.id)
            metas.put(oi.getMeta.id, meta)
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
