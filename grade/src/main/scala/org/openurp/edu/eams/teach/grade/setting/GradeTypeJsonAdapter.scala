package org.openurp.edu.eams.teach.grade.setting

import java.lang.reflect.Type
import org.openurp.edu.eams.teach.code.industry.GradeType
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

import scala.collection.JavaConversions._

class GradeTypeJsonAdapter extends JsonSerializer[GradeType] {

  def serialize(gradeType: GradeType, `type`: Type, jsc: JsonSerializationContext): JsonElement = {
    val jsonObject = new JsonObject()
    jsonObject.addProperty("id", gradeType.getId)
    jsonObject.addProperty("name", gradeType.getName)
    jsonObject
  }
}
