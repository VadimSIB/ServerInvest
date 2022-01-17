package investment.web.apis.authenticate

import investment.utils.JsonUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationFilter : AbstractAuthenticationProcessingFilter(AntPathRequestMatcher("/api/authentications", "POST")) {
    @Throws(AuthenticationException::class, IOException::class)
    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        logger.debug("Processing login request")
        val requestBody = IOUtils.toString(request.reader)
        val loginRequest = JsonUtils.toObject(requestBody, LoginRequest::class.java)
        if (loginRequest == null || loginRequest.isInvalid) {
            throw InsufficientAuthenticationException("Invalid authentication request")
        }
        val token = UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
        return authenticationManager.authenticate(token)
    }

    internal class LoginRequest {
        var username: String? = null
        var password: String? = null
        val isInvalid: Boolean
            get() = StringUtils.isBlank(username) || StringUtils.isBlank(password)

    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthenticationFilter::class.java)
    }
}
