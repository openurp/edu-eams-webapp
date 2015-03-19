package org.openurp.edu.eams.teach.grade.setting

import java.lang.reflect.Type
import org.openurp.edu.teach.code.ExamStatus
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer



class ExamStatusJsonAdapter extends JsonSerializer[ExamStatus] {

  def serialize(examStatus: ExamStatus, `type`: Type, jsc: JsonSerializationContext): JsonElement = {
    val jsonObject = new JsonObject()
    jsonObject.addProperty("id", examStatus.id)
    jsonObject.addProperty("name", examStatus.getName)
    jsonObject
  }
}
