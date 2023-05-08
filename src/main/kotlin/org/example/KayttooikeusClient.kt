package org.example

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.*
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponseInterceptor
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.example.cas.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class KayttooikeusClient {
    val mapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    val log = Logger.getLogger(this.javaClass.name)

    private final val requestInterceptor: HttpRequestInterceptor = HttpRequestInterceptor { request, context ->
        log.info("Adding Authorization header")
        when (val ticket = context.getAttribute("cas_ticket") as String?) {
            null -> {
                log.info("Getting new auth header")
                val newTicket = CasClient.getTicket("https://${CasConfig.virkailijaHost}/cas", CasConfig.username, CasConfig.password, "https://${CasConfig.virkailijaHost}/kayttooikeus-service")
                context.setAttribute("cas_ticket", newTicket)
                request.setHeader(BasicHeader(CasClient.CAS_SECURITY_TICKET, newTicket))
            }
            else -> {
                log.info("Using existing auth header")
                request.setHeader(BasicHeader(CasClient.CAS_SECURITY_TICKET, ticket))
            }
        }
    }

    private final val responseInterceptor: HttpResponseInterceptor = HttpResponseInterceptor { response, context ->
        if (context.getAttribute("cas_ticket") != null) {
            val isCasRedirect = response.statusLine.statusCode == 302 && response.getFirstHeader("Location").value.contains("/cas/login")
            if (isCasRedirect) {
                log.info("Invalidating auth header")
                context.removeAttribute("cas_ticket")
            }
        }
    }

    val httpContext = HttpClientContext().apply {
        cookieStore = BasicCookieStore()
    }

    val authenticatedHttpClient: CloseableHttpClient = HttpClients.custom()
        .addInterceptorFirst(requestInterceptor)
        .addInterceptorFirst(responseInterceptor)
        .build()

    fun kayttooikeudet(username: String): List<Kayttajatiedot> {
        val req = HttpGet("https://${CasConfig.virkailijaHost}/kayttooikeus-service/kayttooikeus/kayttaja?username=$username")
        return authenticatedHttpClient.execute(req, httpContext).use { response ->
            val body = EntityUtils.toString(response.entity)
            println(body)
            return@use mapper.readValue(body)
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
