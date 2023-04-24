package org.example

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {
    @GetMapping("/foo", produces = ["application/json"])
    @ResponseBody
    fun foo(): Kayttajatiedot? = getKayttajatiedot()

    fun getKayttajatiedot(): Kayttajatiedot? =
        SecurityContextHolder.getContext().authentication?.principal as? Kayttajatiedot
}
