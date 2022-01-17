package investment.web.apis.authenticate

import investment.results.ApiResult
import investment.utils.JsonUtils
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SimpleAuthenticationFailureHandler : AuthenticationFailureHandler {
    @Throws(IOException::class)
    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse,
                                         exception: AuthenticationException) {
        response.status = HttpStatus.BAD_REQUEST.value()
        val failure: ApiResult
        failure = if (exception is BadCredentialsException) {
            ApiResult.message("Invalid credentials")
        } else if (exception is InsufficientAuthenticationException) {
            ApiResult.message("Invalid authentication request")
        } else {
            ApiResult.message("Authentication failure")
        }
        JsonUtils.write(response.writer, failure)
    }
}
