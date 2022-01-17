package investment.results

import org.springframework.util.Assert
import java.util.*

class ApiResult : HashMap<String?, Any?>() {
    fun add(key: String?, value: Any?): ApiResult {
        Assert.hasText(key, "Parameter `key` must not be blank")
        Assert.notNull(value, "Parameter `value` must not be null")
        this[key] = value
        return this
    }

    companion object {
        private const val serialVersionUID = 877825499039674411L
        private const val MESSAGE_KEY = "message"
        private const val ERROR_CODE_KEY = "errorReferenceCode"
        fun blank(): ApiResult {
            return ApiResult()
        }

        fun message(message: String): ApiResult {
            Assert.hasText(message, "Parameter `message` must not be blank")
            val apiResult = ApiResult()
            apiResult["message"] = message
            return apiResult
        }

        fun error(message: String, errorReferenceCode: String): ApiResult {
            Assert.hasText(message, "Parameter `message` must not be blank")
            Assert.hasText(errorReferenceCode, "Parameter `errorReferenceCode` must not be blank")
            val apiResult = ApiResult()
            apiResult[MESSAGE_KEY] = message
            apiResult[ERROR_CODE_KEY] = errorReferenceCode
            return apiResult
        }
    }
}
