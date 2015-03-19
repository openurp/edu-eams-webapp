package org.openurp.edu.eams.teach.grade.setting

import java.lang.reflect.Type
import org.openurp.edu.teach.code.GradeType
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer



class GradeTypeJsonAdapter extends JsonSerializer[GradeType] {

  def serialize(gradeType: GradeType, `type`: Type, jsc: JsonSerializationContext): JsonElement = {
    val jsonObject = new JsonObject()
    jsonObject.addProperty("id", gradeType.id)
    jsonObject.addProperty("name", gradeType.getName)
    jsonObject
  }
}
