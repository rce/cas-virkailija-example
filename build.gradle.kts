import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.11"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://artifactory.opintopolku.fi/artifactory/oph-sade-snapshot-local") {
        mavenContent {
            snapshotsOnly()
        }
    }
    maven(url = "https://artifactory.opintopolku.fi/artifactory/oph-sade-release-local") {
        mavenContent {
            releasesOnly()
        }
    }

}

dependencies {
    implementation("fi.vm.sade.java-utils:opintopolku-cas-servlet-filter:0.1.2-SNAPSHOT")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-cas")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//application {
//    mainClass.set("MainKt")
//}