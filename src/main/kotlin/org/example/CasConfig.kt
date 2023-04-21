package org.example

import com.google.gson.Gson
import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter
import fi.vm.sade.javautils.httpclient.apache.ApacheOphHttpClient
import fi.vm.sade.properties.OphProperties
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator
import org.jasig.cas.client.validation.TicketValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.web.CasAuthenticationEntryPoint
import org.springframework.security.cas.web.CasAuthenticationFilter
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.InputStreamReader

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
        AnotherUserDetailsService("https://$opintopolkuHostname", "1.2.246.562.10.00000000001.example-service")
}

class AnotherUserDetailsService(urlVirkailija: String, callerId: String) : UserDetailsService {
    val httpClient = ApacheOphHttpClient.createDefaultOphClient(
        callerId,
        OphProperties("/oph.properties").addOverride("url-virkailija", urlVirkailija)
    )

    override fun loadUserByUsername(username: String): UserDetails {
        return httpClient.get("kayttooikeus-service.kayttooikeus.kayttaja.byUsername", username)
            .expectStatus(200, 404)
            .execute { response ->
                if (response.statusCode == 404) {
                    throw UsernameNotFoundException(
                        String.format(
                            "Käyttäjää ei löytynyt käyttäjätunnuksella '%s'",
                            username
                        )
                    )
                }
                Gson().fromJson(InputStreamReader(response.asInputStream()), Kayttaja::class.java)
            }

    }

}

class Kayttaja(
    val oidHenkilo: String,
    private val username: String,
    val kayttajaTyyppi: String,
    val organisaatiot: List<Organisaatio>,
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        TODO("Not yet implemented")
    }

    override fun getPassword(): String {
        TODO("Not yet implemented")
    }

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAccountNonLocked(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCredentialsNonExpired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEnabled(): Boolean {
        TODO("Not yet implemented")
    }
}

data class Organisaatio(val organisaatioOid: String, val kayttooikeudet: List<Kayttooikeus>)
data class Kayttooikeus(val palvelu: String, val oikeus: String)