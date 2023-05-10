package org.example

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.example.cas.CasAuthenticatingClient
import org.example.cas.CasConfig
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class OppijanumerorekisteriClient : CasAuthenticatingClient(
    targetServiceUrl = "https://${CasConfig.virkailijaHost}/oppijanumerorekisteri-service"
)  {
    override val log = Logger.getLogger(this.javaClass.name)

    fun findByOid(oid: String): Henkilo {
        val req = HttpGet("https://${CasConfig.virkailijaHost}/oppijanumerorekisteri-service/henkilo/$oid")
        return executeRequest(req, httpContext).use { response ->
            val body = EntityUtils.toString(response.entity)
            log.info(body)
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