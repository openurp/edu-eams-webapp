package org.openurp.edu.eams.teach.schedule.json

import java.lang.reflect.Type
import org.openurp.edu.base.Teacher
import org.openurp.edu.eams.core.model.TeacherBean
import org.openurp.edu.eams.teach.lesson.CourseTime
import org.openurp.edu.eams.teach.lesson.model.ArrangeSuggestBean
import org.openurp.edu.eams.teach.lesson.model.SuggestActivityBean
import org.openurp.edu.eams.util.GsonHelper
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer



class SuggestActivityGsonAdapter extends JsonSerializer[SuggestActivityBean] with JsonDeserializer[SuggestActivityBean] {

  def serialize(activity: SuggestActivityBean, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val activityJson = new JsonObject()
    activityJson.addProperty("id", activity.id)
    if (activity.getArrangeSuggest != null) {
      activityJson.addProperty("arrangeSuggestId", activity.getArrangeSuggest.id)
    } else {
      activityJson.add("arrangeSuggestId", JsonNull.INSTANCE)
    }
    val timeJson = new JsonObject()
    timeJson.addProperty("startTime", activity.getTime.start)
    timeJson.addProperty("startUnit", activity.getTime.getStartUnit)
    timeJson.addProperty("endTime", activity.getTime.end)
    timeJson.addProperty("endUnit", activity.getTime.getEndUnit)
    timeJson.addProperty("weekday", activity.getTime.day)
    timeJson.addProperty("weekState", activity.getTime.getWeekState)
    timeJson.addProperty("weekStateNum", activity.getTime.state)
    activityJson.add("time", timeJson)
    val teacherJsonArray = new JsonArray()
    for (teacher <- activity.getTeachers) {
      val teacherJson = new JsonObject()
      teacherJson.addProperty("id", teacher.id)
      teacherJson.addProperty("code", teacher.getCode)
      teacherJson.addProperty("name", teacher.getName)
      teacherJsonArray.add(teacherJson)
    }
    activityJson.add("teachers", teacherJsonArray)
    activityJson
  }

  def deserialize(activityJson: JsonElement, typeOfT: Type, context: JsonDeserializationContext): SuggestActivityBean = {
    val activity = new SuggestActivityBean()
    val json = activityJson.getAsJsonObject
    activity.setId(GsonHelper.getLong(json, "id"))
    activity.setArrangeSuggest(new ArrangeSuggestBean(GsonHelper.getLong(json, "arrangeSuggestId")))
    activity.setTime(new CourseTime())
    val timeJSON = json.getAsJsonObject("time")
    if (GsonHelper.isNotNull(timeJSON)) {
      activity.getTime.setStartTime(GsonHelper.getInteger(timeJSON, "startTime"))
      activity.getTime.setStartUnit(GsonHelper.getInteger(timeJSON, "startUnit"))
      activity.getTime.setEndTime(GsonHelper.getInteger(timeJSON, "endTime"))
      activity.getTime.setEndUnit(GsonHelper.getInteger(timeJSON, "endUnit"))
      activity.getTime.setWeekday(GsonHelper.getInteger(timeJSON, "weekday"))
      activity.getTime.setWeekState(GsonHelper.getString(timeJSON, "weekState"))
      activity.getTime.setWeekStateNum(GsonHelper.getLong(timeJSON, "weekStateNum"))
    }
    val teachersJson = json.getAsJsonArray("teachers")
    if (GsonHelper.isNotNull(teachersJson)) {
      for (i <- 0 until teachersJson.size) {
        val teacherJson = teachersJson.get(i).getAsJsonObject
        activity.getTeachers.add(new TeacherBean(GsonHelper.getLong(teacherJson, "id")))
      }
    }
    activity
  }
}
