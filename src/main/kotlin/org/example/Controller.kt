package org.example

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {
    @GetMapping("/foo", produces = ["application/json"])
    @ResponseBody
    fun foo(): Foo {
        val auth = SecurityContextHolder.getContext().authentication
        return Foo(auth.name)
    }
}

data class Foo(val name: String)