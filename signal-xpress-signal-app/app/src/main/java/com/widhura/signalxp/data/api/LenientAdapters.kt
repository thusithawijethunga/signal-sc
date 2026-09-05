package com.widhura.signalxp.data.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Lenient number adapters.
 *
 * The Laravel backend serializes `decimal` casts as JSON STRINGS
 * (e.g. "entry1": "4571.00", "pips": "30.0") while Moshi's default
 * Double adapter only accepts JSON NUMBERS. Without this, every REST
 * sync throws JsonDataException and the app silently keeps stale data.
 * These adapters accept NUMBER, numeric STRING, empty String and NULL.
 */
object LenientDoubleAdapter : JsonAdapter<Double?>() {
    @com.squareup.moshi.FromJson
    override fun fromJson(reader: JsonReader): Double? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.NUMBER -> reader.nextDouble()
            JsonReader.Token.STRING -> {
                val s = reader.nextString()
                if (s.isBlank()) null else s.toDoubleOrNull()
            }
            JsonReader.Token.BOOLEAN -> {
                reader.nextBoolean()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @com.squareup.moshi.ToJson
    override fun toJson(writer: JsonWriter, value: Double?) {
        writer.value(value)
    }
}

object LenientPrimitiveDoubleAdapter : JsonAdapter<Double>() {
    @com.squareup.moshi.FromJson
    override fun fromJson(reader: JsonReader): Double {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                0.0
            }
            JsonReader.Token.NUMBER -> reader.nextDouble()
            JsonReader.Token.STRING -> {
                val s = reader.nextString()
                if (s.isBlank()) 0.0 else s.toDoubleOrNull() ?: 0.0
            }
            JsonReader.Token.BOOLEAN -> {
                reader.nextBoolean()
                0.0
            }
            else -> {
                reader.skipValue()
                0.0
            }
        }
    }

    @com.squareup.moshi.ToJson
    override fun toJson(writer: JsonWriter, value: Double?) {
        writer.value(value)
    }
}

object LenientIntAdapter : JsonAdapter<Int?>() {
    @com.squareup.moshi.FromJson
    override fun fromJson(reader: JsonReader): Int? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull()
            JsonReader.Token.NUMBER -> reader.nextInt()
            JsonReader.Token.STRING -> {
                val s = reader.nextString()
                if (s.isBlank()) null else (s.toIntOrNull() ?: s.toDoubleOrNull()?.toInt())
            }
            JsonReader.Token.BOOLEAN -> {
                reader.nextBoolean()
                null
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    @com.squareup.moshi.ToJson
    override fun toJson(writer: JsonWriter, value: Int?) {
        writer.value(value)
    }
}

/** Factory that installs the lenient number adapters for all Double/Int types. */
class LenientNumberFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null
        return when (type) {
            Double::class.javaObjectType -> LenientDoubleAdapter
            Double::class.javaPrimitiveType -> LenientPrimitiveDoubleAdapter
            Int::class.javaObjectType -> LenientIntAdapter
            Int::class.javaPrimitiveType -> LenientIntAdapter
            else -> null
        }
    }
}
