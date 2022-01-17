package investment

import investment.security.handlers.AjaxAuthenticationFailureHandler
import investment.security.handlers.AjaxAuthenticationSuccessHandler
import investment.security.handlers.AjaxLogoutSuccessHandler
import investment.web.apis.authenticate.AuthenticationFilter
import investment.web.apis.authenticate.SimpleAuthenticationFailureHandler
import investment.web.apis.authenticate.SimpleAuthenticationSuccessHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
open class SecurityConfig : WebSecurityConfigurerAdapter() {
    @Bean
    open fun ajaxAuthenticationSuccessHandler(): AjaxAuthenticationSuccessHandler {
        return AjaxAuthenticationSuccessHandler()
    }
    @Bean
    open fun ajaxAuthenticationFailureHandler(): AjaxAuthenticationFailureHandler {
        return AjaxAuthenticationFailureHandler()
    }
    @Bean
    open fun ajaxLogoutSuccessHandler(): AjaxLogoutSuccessHandler {
        return AjaxLogoutSuccessHandler()
    }
    // эти три бина - для перевода запроса c application/x-www-form-urlencoded на json
    @Bean
    @Throws(Exception::class)
    open fun authenticationFilter(): AuthenticationFilter {
        val authenticationFilter = AuthenticationFilter()
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler())
        authenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler())
        authenticationFilter.setAuthenticationManager(authenticationManagerBean())
        return authenticationFilter
    }

    @Bean
    open fun authenticationSuccessHandler(): AuthenticationSuccessHandler {
        return SimpleAuthenticationSuccessHandler()
    }

    @Bean
    open fun authenticationFailureHandler(): AuthenticationFailureHandler {
        return SimpleAuthenticationFailureHandler()
    }

    // zask, список урлов, которые игнорит секьюрити
    override fun configure(web: WebSecurity) {
        web
                .ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/content/**")
                .antMatchers("/test/**")
                .antMatchers("/api/files/**")
                .antMatchers("/api/mapObjects")
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf()
                .disable() // это отключение csrf, которая автоматически отключает GET логаут, но все равно logout требует post !!
                .exceptionHandling()
                .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .addFilterAt(authenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
                .formLogin()
                .loginProcessingUrl("/api/authentications") // этот запрос будет прослушиваться для обработки логина
                .successHandler(ajaxAuthenticationSuccessHandler())
                .failureHandler(ajaxAuthenticationFailureHandler())
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout") // желательно этот урл изменить
                .logoutSuccessHandler(ajaxLogoutSuccessHandler())
                .permitAll() //
                .and()
                .authorizeRequests() //
                .antMatchers("/api/**").hasRole("ADMIN")
    }

    @Autowired
    @Throws(Exception::class)
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth
                .inMemoryAuthentication() // {noop} потребовалось из-за нового формата хранения паролей в 5-й версии SS
                .withUser("admin").password("{noop}123").roles("ADMIN")
    }

    companion object {
        private val PUBLIC = arrayOf(
                "/error", "/login", "/logout", "/admin", "/username")
    }
}
