package org.example

object Config {
    val palvelukayttajaUsername = System.getenv("PALVELUKAYTTAJA_USERNAME")
    val palvelukayttajaPassword = System.getenv("PALVELUKAYTTAJA_PASSWORD")
    val appUrl = "http://localhost:8080"
    val virkailijaHost = "virkailija.testiopintopolku.fi"
}