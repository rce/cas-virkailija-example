package org.example

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fi.vm.sade.java_utils.security.OpintopolkuCasAuthenticationFilter
import fi.vm.sade.javautils.http.OphHttpClient
import fi.vm.sade.javautils.http.OphHttpRequest
import fi.vm.sade.javautils.http.auth.CasAuthenticator
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
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.*
import org.springframework.security.web.AuthenticationEntryPoint
import java.util.*

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

    @Bean
    fun userDetailsService(): AuthenticationUserDetailsService<CasAssertionAuthenticationToken> =
        CasUserDetailsService()
}

class CasUserDetailsService : AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
    val callerId = "1.2.246.562.10.00000000001.example-service"
    val authenticator = CasAuthenticator.Builder()
        .username(System.getenv("PALVELUKAYTTAJA_USERNAME"))
        .password(System.getenv("PALVELUKAYTTAJA_PASSWORD"))
        .webCasUrl("https://virkailija.testiopintopolku.fi/cas")
        .casServiceUrl("https://virkailija.testiopintopolku.fi/kayttooikeus-service/j_spring_cas_security_check")
        .build()
    val httpClient = OphHttpClient.Builder(callerId).authenticator(authenticator).build()

    override fun loadUserDetails(token: CasAssertionAuthenticationToken): UserDetails {
        val username = token.name
        val request = OphHttpRequest.Builder.get("https://virkailija.testiopintopolku.fi/kayttooikeus-service/kayttooikeus/kayttaja?username=$username").build()
        val users: Optional<List<Kayttajatiedot>> = httpClient.execute<List<Kayttajatiedot>>(request)
            .expectedStatus(200, 404)
            .mapWith { response: String ->
                val listType = object : TypeToken<List<Kayttajatiedot>>() {}.getType()
                Gson().fromJson(response, listType)
            }

        when {
            users.isEmpty() -> throw UsernameNotFoundException("User '$username' not found")
            else -> return users.get().find { it.username == username }
                ?: throw UsernameNotFoundException("User '$username' not found")
        }
    }
}

data class Kayttajatiedot(
    val oidHenkilo: String,
    private val username: String,
    val kayttajaTyyppi: String,
    val organisaatiot: List<Organisaatio>,
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf()
    override fun getPassword(): String? = null
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}

data class Organisaatio(
    val organisaatioOid: String,
    val kayttooikeudet: List<Kayttooikeus>,
)

data class Kayttooikeus(
    val palvelu: String,
    val oikeus: String,
)
