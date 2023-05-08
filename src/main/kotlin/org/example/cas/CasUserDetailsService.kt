package org.example.cas

import org.example.KayttooikeusClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class CasUserDetailsService(
    @Autowired val kayttooikeusClient: KayttooikeusClient,
) : AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
    override fun loadUserDetails(token: CasAssertionAuthenticationToken): UserDetails {
        val username = token.name
        val users = kayttooikeusClient.kayttooikeudet(username)
        val user = users.find { it.username == username }
        return user ?: throw UsernameNotFoundException("User '$username' not found")
    }
}
