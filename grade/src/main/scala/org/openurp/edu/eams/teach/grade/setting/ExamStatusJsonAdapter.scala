package org.openurp.edu.eams.teach.grade.setting

import java.lang.reflect.Type
import org.openurp.edu.eams.teach.code.industry.ExamStatus
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

import scala.collection.JavaConversions._

class ExamStatusJsonAdapter extends JsonSerializer[ExamStatus] {

  def serialize(examStatus: ExamStatus, `type`: Type, jsc: JsonSerializationContext): JsonElement = {
    val jsonObject = new JsonObject()
    jsonObject.addProperty("id", examStatus.getId)
    jsonObject.addProperty("name", examStatus.getName)
    jsonObject
  }
}
