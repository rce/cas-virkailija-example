package org.example

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
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
class OppijanumerorekisteriClient {
    val mapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    val log = Logger.getLogger(this.javaClass.name)

    private final val requestInterceptor: HttpRequestInterceptor = HttpRequestInterceptor { request, context ->
        log.info("Adding Authorization header")
        when (val ticket = context.getAttribute("cas_ticket") as String?) {
            null -> {
                log.info("Getting new auth header")
                val newTicket = CasClient.getTicket("https://${CasConfig.virkailijaHost}/cas", CasConfig.username, CasConfig.password, "https://${CasConfig.virkailijaHost}/oppijanumerorekisteri-service")
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

    fun findByOid(oid: String): Henkilo {
        val req = HttpGet("https://${CasConfig.virkailijaHost}/oppijanumerorekisteri-service/henkilo/$oid")
        return authenticatedHttpClient.execute(req, httpContext).use { response ->
            val body = EntityUtils.toString(response.entity)
            println(body)
            return@use mapper.readValue(body)
        }
    }
}

data class Henkilo(
    val oidHenkilo: String,
    val hetu: String?,
    val kaikkiHetut: List<String>,
    val passivoitu: Boolean,
    val etunimet: String,
    val kutsumanimi: String,
    val sukunimi: String,
    val aidinkieli: JsonNode?,
    val asiointiKieli: Asiointikieli,
    val kansalaisuus: List<Any>,
    val kasittelijaOid: String,
    val syntymaaika: String,
    val sukupuoli: String,
    val kotikunta: JsonNode?,
    val oppijanumero: String?,
    val turvakielto: Boolean,
    val eiSuomalaistaHetua: Boolean,
    val yksiloity: Boolean,
    val yksiloityVTJ: Boolean,
    val yksilointiYritetty: Boolean,
    val duplicate: Boolean,
    val created: Long,
    val modified: Long,
    val vtjsynced: JsonNode?,
    val yhteystiedotRyhma: List<YhteystietoRyhma>,
    val yksilointivirheet: List<JsonNode>,
    val passinumerot: List<JsonNode>,
    val kielisyys: List<JsonNode>,
    val henkiloTyyppi: String
)
data class Asiointikieli(val kieliKoodi: String, val kieliTyyppi: String)
data class YhteystietoRyhma(
    val id: Long,
    val ryhmaKuvaus: String,
    val ryhmaAlkuperaTieto: String,
    val readOnly: Boolean,
    val yhteystieto: List<Yhteystieto>,
)
data class Yhteystieto(val yhteystietoTyyppi: String, val yhteystietoArvo: String?)