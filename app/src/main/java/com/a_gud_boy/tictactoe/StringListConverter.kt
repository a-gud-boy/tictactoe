package com.a_gud_boy.tictactoe

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONException

class StringListConverter {
    @TypeConverter
    fun fromStringList(stringList: List<String>?): String? {
        return stringList?.let { JSONArray(it).toString() }
    }

    @TypeConverter
    fun toStringList(jsonString: String?): List<String>? {
        if (jsonString == null) {
            return null
        }
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: JSONException) {
            // Optionally log the error or return emptyList/null based on desired error handling
            // For now, returning null if parsing fails, consistent with nullable return type.
            null
        }
    }
}
