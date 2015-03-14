package org.openurp.edu.eams.teach.election.service.cache

import java.util.List
import java.util.Map
import java.util.Set
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.dao.query.builder.OqlBuilder
import org.beangle.commons.lang.Throwables
import org.openurp.edu.eams.teach.election.ElectionProfile
import org.openurp.edu.teach.lesson.Lesson
import ProfileLessonCountProvider._

import scala.collection.JavaConversions._

object ProfileLessonCountProvider {

  private val refreshInterval = 1000 * 5

  private val profileId2CountJson = CollectUtils.newHashMap()

  private val profileId2LastUpdateTime = CollectUtils.newHashMap()

  private val profileId2Turn = CollectUtils.newHashMap()

  private val urgentQuery = OqlBuilder.from(classOf[ElectionProfile].getName, "p")
    .select("p.id, p.turn")
    .where("current_time() between p.beginAt-1/24 and p.endAt")
}

class ProfileLessonCountProvider extends AbstractProfileLessonProvider {

  def run() {
    try {
      while (true) {
        val urgentIds = CollectUtils.newHashSet()
        val ss = entityDao.search(urgentQuery)
        for (result <- ss) {
          val profileId = result(0).asInstanceOf[java.lang.Long]
          val turn = result(1).toString
          profileId2Turn.put(profileId, turn)
          urgentIds.add(profileId)
          profileId2CountJson.put(profileId, makeCountJson(profileId))
          profileId2LastUpdateTime.put(profileId, System.currentTimeMillis())
        }
        profileId2Turn.keySet.retainAll(urgentIds)
        profileId2CountJson.keySet.retainAll(urgentIds)
        profileId2LastUpdateTime.keySet.retainAll(urgentIds)
        logger.debug(Thread.currentThread().getName + " preparing profile lesson count data finished!")
        Thread.sleep(refreshInterval)
      }
    } catch {
      case e: InterruptedException => logger.error(Throwables.getStackTrace(e))
    }
  }

  def getJson(profileId: java.lang.Long): String = {
    var json = profileId2CountJson.get(profileId)
    if (json == null) {
      synchronized (classOf[ProfileLessonCountProvider]) {
        json = profileId2CountJson.get(profileId)
        if (json == null) {
          json = makeCountJson(profileId)
          profileId2CountJson.put(profileId, json)
          profileId2LastUpdateTime.put(profileId, System.currentTimeMillis())
        }
      }
    }
    json
  }

  def getLastUpdateTime(profileId: java.lang.Long): String = {
    String.valueOf(profileId2LastUpdateTime.get(profileId))
  }

  private def makeCountJson(profileId: java.lang.Long): String = {
    val start = System.currentTimeMillis()
    val builder = OqlBuilder.from(classOf[Lesson].getName + " lesson")
    builder.select("lesson.id, lesson.teachClass.stdCount, lesson.teachClass.limitCount")
      .where("exists (from " + classOf[ElectionProfile].getName + 
      " profile join profile.electableLessons electableLessonId where profile.id=:profile and electableLessonId=lesson.id)", 
      profileId)
    val lessonStdCounts = entityDao.search(builder).asInstanceOf[List[Array[Any]]]
    val size = lessonStdCounts.size
    val sb = new StringBuilder(33 * size + 45 + 1)
    sb.append("/*sc 当前人数, lc 人数上限*/\nwindow.lessonId2Counts={")
    for (i <- 0 until size) {
      val countArr = lessonStdCounts.get(i)
      val id = countArr(0).asInstanceOf[java.lang.Long]
      val stdCount = countArr(1).asInstanceOf[java.lang.Integer]
      val limitCount = countArr(2).asInstanceOf[java.lang.Integer]
      sb.append('\'').append(id).append("':{sc:").append(stdCount)
        .append(",lc:")
        .append(limitCount)
        .append('}')
      if (i != size - 1) {
        sb.append(',')
      }
    }
    sb.append('}')
    logger.debug("Election Lesson Count rending :" + (System.currentTimeMillis() - start))
    sb.toString
  }
}
