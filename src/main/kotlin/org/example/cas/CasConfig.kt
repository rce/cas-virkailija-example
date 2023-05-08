package org.example.cas

import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator
import org.jasig.cas.client.validation.TicketValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.web.CasAuthenticationEntryPoint
import org.springframework.security.cas.web.CasAuthenticationFilter
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.web.AuthenticationEntryPoint

@Configuration
class CasConfig {
    companion object {
        val username = System.getenv("PALVELUKAYTTAJA_USERNAME")
        val password = System.getenv("PALVELUKAYTTAJA_PASSWORD")
        val appUrl = "http://localhost:8080"
        val virkailijaHost = "virkailija.testiopintopolku.fi"
    }


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
        Cas30ServiceTicketValidator("https://$virkailijaHost/cas");

    @Bean
    fun authenticationEntryPoint(
        serviceProperties: ServiceProperties,
    ): AuthenticationEntryPoint {
        val entryPoint = CasAuthenticationEntryPoint()
        entryPoint.loginUrl = "https://$virkailijaHost/cas/login"
        entryPoint.serviceProperties = serviceProperties
        return entryPoint
    }

    @Bean
    fun casAuthenticationProvider(
        userDetailsService: AuthenticationUserDetailsService<CasAssertionAuthenticationToken>,
        serviceProperties: ServiceProperties,
        ticketValidator: TicketValidator,
    ): CasAuthenticationProvider =
        CasAuthenticationProvider().apply {
            setAuthenticationUserDetailsService(userDetailsService)
            setServiceProperties(serviceProperties)
            setTicketValidator(ticketValidator)
            setKey("example-service")
        }
}