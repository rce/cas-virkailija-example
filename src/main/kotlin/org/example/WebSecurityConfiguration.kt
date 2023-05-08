package org.example;

import org.jasig.cas.client.session.SingleSignOutFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.cas.web.CasAuthenticationFilter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    val allowLogoutUsingHttpGet = true

    @Bean
    fun singleSignOutFilter(): SingleSignOutFilter =
        SingleSignOutFilter().apply {
            setIgnoreInitConfiguration(true)
        }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authenticationEntryPoint: AuthenticationEntryPoint,
        singleSignOutFilter: SingleSignOutFilter,
        casAuthenticationFilter: CasAuthenticationFilter,
    ): SecurityFilterChain {
        if (allowLogoutUsingHttpGet) { http.csrf().disable() }

        http
            .logout { logout ->
                logout.logoutUrl("/logout")
                logout.logoutSuccessUrl("https://virkailija.testiopintopolku.fi/cas/logout")
                //logout.invalidateHttpSession(true)
            }
            .authorizeHttpRequests()
            .anyRequest().authenticated()
            .and()
            .addFilter(casAuthenticationFilter)
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
            .and()
            .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter::class.java)
        return http.build()
    }
}
