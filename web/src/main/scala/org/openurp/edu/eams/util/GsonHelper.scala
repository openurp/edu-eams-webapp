package org.openurp.edu.eams.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject



object GsonHelper {

  def isNull(element: JsonElement): Boolean = element == null || element.isJsonNull

  def isNotNull(element: JsonElement): Boolean = !isNull(element)

  def getLong(json: JsonObject, property: String): java.lang.Long = getAsLong(json.get(property))

  def getPrimitiveLong(json: JsonObject, property: String): Long = getAsPrimitiveLong(json.get(property))

  def getInteger(json: JsonObject, property: String): java.lang.Integer = getAsInteger(json.get(property))

  def getPrimitiveInteger(json: JsonObject, property: String): Int = getAsPrimitiveInt(json.get(property))

  def getDouble(json: JsonObject, property: String): java.lang.Double = getAsDouble(json.get(property))

  def getPrimitiveDouble(json: JsonObject, property: String): Double = {
    getAsPrimitiveDouble(json.get(property))
  }

  def getFloat(json: JsonObject, property: String): java.lang.Float = getAsFloat(json.get(property))

  def getPrimitiveFloat(json: JsonObject, property: String): Float = getAsPrimitiveFloat(json.get(property))

  def getBoolean(json: JsonObject, property: String): java.lang.Boolean = getAsBoolean(json.get(property))

  def getString(json: JsonObject, property: String): String = getAsString(json.get(property))

  def getAsString(element: JsonElement): String = {
    if (isNull(element)) null else element.getAsString
  }

  def getAsLong(element: JsonElement): java.lang.Long = {
    if (isNull(element)) null else element.getAsLong
  }

  def getAsPrimitiveLong(element: JsonElement): Long = {
    val r = getAsLong(element)
    if (r == null) 0l else r
  }

  def getAsInteger(element: JsonElement): java.lang.Integer = {
    if (isNull(element)) null else element.getAsInt
  }

  def getAsPrimitiveInt(element: JsonElement): Int = {
    val r = getAsInteger(element)
    if (r == null) 0 else r
  }

  def getAsBoolean(element: JsonElement): java.lang.Boolean = {
    if (isNull(element)) null else element.getAsBoolean
  }

  def getAsPrimitiveBool(element: JsonElement): Boolean = {
    val r = getAsBoolean(element)
    if (r == null) false else r.booleanValue()
  }

  def getAsDouble(element: JsonElement): java.lang.Double = {
    if (isNull(element)) null else element.getAsDouble
  }

  def getAsPrimitiveDouble(element: JsonElement): Double = {
    val r = getAsDouble(element)
    if (r == null) 0d else r
  }

  def getAsFloat(element: JsonElement): java.lang.Float = {
    if (isNull(element)) null else element.getAsFloat
  }

  def getAsPrimitiveFloat(element: JsonElement): Float = {
    val r = getAsFloat(element)
    if (r == null) 0f else r
  }
}
