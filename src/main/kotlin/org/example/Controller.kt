package org.example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller(
    @Autowired val oppijanumerorekisteriClient: OppijanumerorekisteriClient,
) {
    @GetMapping("/foo", produces = ["application/json"])
    @ResponseBody
    //fun foo(): String = oppijanumerorekisteriClient.findByOid("1.2.246.562.24.40858866951").toString()
    fun foo(): String = oppijanumerorekisteriClient.yleistunnisteHae(OppijanumerorekisteriClient.YleistunnisteHaeRequest(
        etunimet = "testi-3",
        hetu = "161052-777V",
        kutsumanimi = "testi-3",
        sukunimi = "testi-3"
    )).toString()
    fun bar(): Kayttajatiedot? = getKayttajatiedot()

    fun getKayttajatiedot(): Kayttajatiedot? =
        SecurityContextHolder.getContext().authentication?.principal as? Kayttajatiedot
}
