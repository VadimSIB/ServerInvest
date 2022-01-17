package investment.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.Writer

object JsonUtils {
    private val logger = LoggerFactory.getLogger(JsonUtils::class.java)
    fun toJson(`object`: Any?): String {
        val mapper = ObjectMapper()
        return try {
            mapper.writeValueAsString(`object`)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Failed to convert object to JSON string", e)
        }
    }

    fun <T> toObject(json: String, clazz: Class<T>): T? {
        return try {
            val objectMapper = ObjectMapper()
            objectMapper.readValue(json, clazz)
        } catch (e: IOException) {
            logger.error("Failed to convert string `" + json + "` class `" + clazz.name + "`", e)
            null
        }
    }

    @Throws(IOException::class)
    fun write(writer: Writer?, value: Any?) {
        ObjectMapper().writeValue(writer, value)
    }
}
