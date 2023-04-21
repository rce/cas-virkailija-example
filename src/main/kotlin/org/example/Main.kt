package org.example
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
@SpringBootApplication
class Main
fun main(args: Array<String>) {
    runApplication<Main>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}