package investment.security.handlers

import lombok.NoArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@NoArgsConstructor
class AjaxAuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {
    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse, exception: AuthenticationException) {
        response.sendError(HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED_MESSAGE)
    }

    companion object {
        const val UNAUTHORIZED_MESSAGE = "Authentication failed"
    }
}
