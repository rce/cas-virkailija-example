package org.example

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class Controller {
    @GetMapping("/foo", produces = ["application/json"])
    @ResponseBody
    fun foo(): Foo {
        val auth = SecurityContextHolder.getContext().authentication
        val permissions = auth.authorities.map { it.authority }
        return Foo(auth.name, permissions)
    }
}

data class Foo(val name: String, val permissions: List<String>)