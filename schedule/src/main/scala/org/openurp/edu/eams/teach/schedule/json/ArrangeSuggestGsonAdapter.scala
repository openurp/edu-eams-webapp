package org.openurp.edu.eams.teach.schedule.json

import java.lang.reflect.Type
import java.util.Set
import org.beangle.commons.entity.metadata.Model
import org.openurp.base.Room
import org.openurp.edu.eams.base.model.ClassroomBean
import org.openurp.edu.teach.lesson.Lesson
import org.openurp.edu.eams.teach.lesson.SuggestActivity
import org.openurp.edu.eams.teach.lesson.model.ArrangeSuggestBean
import org.openurp.edu.eams.teach.lesson.model.SuggestActivityBean
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken

import scala.collection.JavaConversions._

class ArrangeSuggestGsonAdapter extends JsonSerializer[ArrangeSuggestBean] with JsonDeserializer[ArrangeSuggestBean] {

  def serialize(activity: ArrangeSuggestBean, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val json = new JsonObject()
    json.addProperty("id", activity.getId)
    json.addProperty("lessonId", activity.getLesson.getId)
    json.addProperty("remark", activity.getRemark)
    json.add("activities", context.serialize(activity.getActivities, new TypeToken[Set[SuggestActivityBean]]() {
    }.getType))
    json.add("rooms", context.serialize(activity.getRooms, new TypeToken[Set[ClassroomBean]]() {
    }.getType))
    json
  }

  def deserialize(activityJson: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ArrangeSuggestBean = {
    val arrange = new ArrangeSuggestBean()
    val json = activityJson.getAsJsonObject
    arrange.setId(json.getAsJsonPrimitive("id").getAsLong)
    arrange.setLesson(Model.newInstance(classOf[Lesson], json.getAsJsonPrimitive("lessonId").getAsLong))
    arrange.setRemark(json.getAsJsonPrimitive("remark").getAsString)
    val rooms = context.deserialize(json.getAsJsonArray("rooms"), new TypeToken[Set[ClassroomBean]]() {
    }.getType)
    arrange.setRooms(rooms)
    val activities = context.deserialize(json.getAsJsonArray("activities"), new TypeToken[Set[SuggestActivityBean]]() {
    }.getType)
    for (act <- activities) {
      act.setArrangeSuggest(arrange)
    }
    arrange.setActivities(activities)
    arrange
  }
}
