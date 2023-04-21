package org.example

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter
import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator
import org.jasig.cas.client.validation.TicketValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.web.CasAuthenticationEntryPoint
import org.springframework.security.cas.web.CasAuthenticationFilter
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.AuthenticationEntryPoint

@Configuration
class CasConfig {
    val appUrl = "http://localhost:8080"
    val opintopolkuHostname = "virkailija.testiopintopolku.fi"

    @Bean
    fun serviceProperties(): ServiceProperties =
        ServiceProperties().apply {
            service = "$appUrl/j_spring_cas_security_check"
            isSendRenew = false
            isAuthenticateAllArtifacts = true
        }

    @Bean
    fun casAuthenticationFilter(
        authenticationConfiguration: AuthenticationConfiguration,
        serviceProperties: ServiceProperties,
    ): CasAuthenticationFilter {
        val casAuthenticationFilter = OpintopolkuCasAuthenticationFilter(serviceProperties)
        casAuthenticationFilter.setAuthenticationManager(authenticationConfiguration.authenticationManager)
        casAuthenticationFilter.setFilterProcessesUrl("/j_spring_cas_security_check")
        return casAuthenticationFilter
    }
    @Bean
    fun ticketValidator(): TicketValidator =
        Cas30ServiceTicketValidator("https://$opintopolkuHostname/cas");

    @Bean
    fun authenticationEntryPoint(
        serviceProperties: ServiceProperties,
    ): AuthenticationEntryPoint {
        val entryPoint = CasAuthenticationEntryPoint()
        entryPoint.loginUrl = "https://$opintopolkuHostname/cas/login"
        entryPoint.serviceProperties = serviceProperties
        return entryPoint
    }

    @Bean
    fun casAuthenticationProvider(
        userDetailsService: UserDetailsService,
        serviceProperties: ServiceProperties,
        ticketValidator: TicketValidator,
    ): CasAuthenticationProvider =
        CasAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setServiceProperties(serviceProperties)
            setTicketValidator(ticketValidator)
            setKey("example-service")
        }

    @Bean
    fun userDetailsService(): UserDetailsService =
        OphUserDetailsServiceImpl("https://$opintopolkuHostname", "1.2.246.562.10.00000000001.example-service")
}