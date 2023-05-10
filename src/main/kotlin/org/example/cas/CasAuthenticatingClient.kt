package org.example.cas

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.example.Config
import java.util.logging.Logger

abstract class CasAuthenticatingClient(val targetServiceUrl: String) {
    open val log = Logger.getLogger(this.javaClass.name)

    val mapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    val httpContext = HttpClientContext().apply {
        cookieStore = BasicCookieStore()
    }
    val authenticatedHttpClient: CloseableHttpClient = HttpClients.custom().build()

    protected fun executeRequest(req: HttpRequestBase, context: HttpClientContext): CloseableHttpResponse {
        req.setHeader(BasicHeader(CasClient.CAS_SECURITY_TICKET, getTicket(context)))
        req.setHeader("Caller-Id", "1.2.246.562.10.00000000001.rce-example")
        req.setHeader("CSRF", "CSRF")
        req.setHeader("Cookie", "CSRF=CSRF")

        val response = authenticatedHttpClient.execute(req, httpContext)
        return if (!isUnauthorized(response)) {
            response
        } else {
            response.close()
            log.info("CAS redirect detected, retrying request with new ticket")
            req.setHeader(BasicHeader(CasClient.CAS_SECURITY_TICKET, refreshCasTicket(context)))
            authenticatedHttpClient.execute(req, httpContext)
        }
    }

    // Unauthorized response is returned when the provided CAS ticket is invalid
    private fun isUnauthorized(response: CloseableHttpResponse): Boolean =
        response.statusLine.statusCode == 401

    private fun getTicket(context: HttpClientContext): String =
        when (val maybeTicket = context.getAttribute("cas_ticket") as String?) {
            null -> refreshCasTicket(context)
            else -> maybeTicket
        }

    private fun refreshCasTicket(context: HttpClientContext): String {
        val ticket = CasClient.getTicket(
            "https://${Config.virkailijaHost}/cas",
            Config.palvelukayttajaUsername,
            Config.palvelukayttajaPassword,
            targetServiceUrl,
        )
        context.setAttribute("cas_ticket", ticket)
        return ticket
    }
}