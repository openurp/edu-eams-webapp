package org.openurp.edu.eams.base.model.json

import java.lang.reflect.Type
import org.openurp.edu.eams.base.model.RoomBean
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer



class RoomBeanGsonAdapter extends JsonSerializer[RoomBean] with JsonDeserializer[RoomBean] {

  def serialize(src: RoomBean, typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
    val json = new JsonObject()
    json.addProperty("id", src.id)
    json.addProperty("code", src.getCode)
    json.addProperty("name", src.getName)
    json.addProperty("capacity", src.getCapacity)
    if (src.getCampus != null) {
      val campusJson = new JsonObject()
      campusJson.addProperty("id", src.getCampus.id)
      campusJson.addProperty("name", src.getCampus.getName)
      campusJson.addProperty("code", src.getCampus.getCode)
      json.add("campus", campusJson)
    } else {
      json.add("campus", JsonNull.INSTANCE)
    }
    if (src.getBuilding != null) {
      val buildingJson = new JsonObject()
      buildingJson.addProperty("id", src.getBuilding.id)
      buildingJson.addProperty("name", src.getBuilding.getName)
      buildingJson.addProperty("code", src.getBuilding.getCode)
      json.add("building", buildingJson)
    } else {
      json.add("building", JsonNull.INSTANCE)
    }
    if (src.getType != null) {
      val typeJson = new JsonObject()
      typeJson.addProperty("id", src.getType.id)
      typeJson.addProperty("name", src.getType.getName)
      typeJson.addProperty("code", src.getType.getCode)
      json.add("type", typeJson)
    } else {
      json.add("type", JsonNull.INSTANCE)
    }
    json
  }

  def deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RoomBean = {
    null
  }
}
