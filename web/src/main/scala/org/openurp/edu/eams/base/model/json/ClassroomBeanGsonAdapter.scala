package org.openurp.edu.eams.base.model.json

import java.lang.reflect.Type
import org.openurp.edu.eams.base.model.ClassroomBean
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer

import scala.collection.JavaConversions._

class ClassroomBeanGsonAdapter extends JsonSerializer[ClassroomBean] with JsonDeserializer[ClassroomBean] {

  def serialize(src: ClassroomBean, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val json = new JsonObject()
    json.addProperty("id", src.getId)
    json.addProperty("code", src.getCode)
    json.addProperty("name", src.getName)
    json.addProperty("capacity", src.getCapacity)
    if (src.getCampus != null) {
      val campusJson = new JsonObject()
      campusJson.addProperty("id", src.getCampus.getId)
      campusJson.addProperty("name", src.getCampus.getName)
      campusJson.addProperty("code", src.getCampus.getCode)
      json.add("campus", campusJson)
    } else {
      json.add("campus", JsonNull.INSTANCE)
    }
    if (src.getBuilding != null) {
      val buildingJson = new JsonObject()
      buildingJson.addProperty("id", src.getBuilding.getId)
      buildingJson.addProperty("name", src.getBuilding.getName)
      buildingJson.addProperty("code", src.getBuilding.getCode)
      json.add("building", buildingJson)
    } else {
      json.add("building", JsonNull.INSTANCE)
    }
    if (src.getType != null) {
      val typeJson = new JsonObject()
      typeJson.addProperty("id", src.getType.getId)
      typeJson.addProperty("name", src.getType.getName)
      typeJson.addProperty("code", src.getType.getCode)
      json.add("type", typeJson)
    } else {
      json.add("type", JsonNull.INSTANCE)
    }
    json
  }

  def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ClassroomBean = {
    null
  }
}
