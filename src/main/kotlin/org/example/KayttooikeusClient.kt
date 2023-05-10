package org.example

import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils
import org.example.cas.CasAuthenticatingClient
import org.example.cas.CasConfig
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class KayttooikeusClient : CasAuthenticatingClient(
    targetServiceUrl = "https://${CasConfig.virkailijaHost}/kayttooikeus-service"
) {
    override val log = Logger.getLogger(this.javaClass.name)

    fun kayttooikeudet(username: String): List<Kayttajatiedot> {
        val req = HttpGet("https://${CasConfig.virkailijaHost}/kayttooikeus-service/kayttooikeus/kayttaja?username=$username")
        return executeRequest(req, httpContext).use { response ->
            val body = EntityUtils.toString(response.entity)
            log.info(body)
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
