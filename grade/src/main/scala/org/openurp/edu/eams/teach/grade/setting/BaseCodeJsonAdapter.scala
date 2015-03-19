package org.openurp.edu.eams.teach.grade.setting

import java.lang.reflect.Type
import org.beangle.commons.entity.pojo.BaseCode
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer



class BaseCodeJsonAdapter extends JsonSerializer[BaseCode] {

  def serialize(baseCode: BaseCode, `type`: Type, jsc: JsonSerializationContext): JsonElement = {
    val jsonObject = new JsonObject()
    jsonObject.addProperty("id", baseCode.id)
    jsonObject.addProperty("code", baseCode.getCode)
    jsonObject.addProperty("name", baseCode.getName)
    jsonObject
  }
}
