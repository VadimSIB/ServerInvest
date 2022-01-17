package investment.web.apis.authenticate

import investment.results.ApiResult
import investment.utils.JsonUtils
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SimpleAuthenticationSuccessHandler : AuthenticationSuccessHandler {
    @Throws(IOException::class)
    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                         authentication: Authentication) {
        response.status = HttpStatus.OK.value()
        JsonUtils.write(response.writer, ApiResult.message("authenticated"))
    }
}
