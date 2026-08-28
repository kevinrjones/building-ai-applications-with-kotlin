plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.versionUpdate)
    alias(libs.plugins.catalogUpdate)

    application
}

repositories { mavenCentral() }

dependencies {
    implementation(libs.clikt)
    implementation(libs.coroutines.core)
    implementation(libs.kotlin.logging)
    implementation(libs.logback)
    implementation(libs.okhttp)
    implementation(libs.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}

application {
    mainClass.set("com.pluralsight.kai.MainKt")
}

tasks.test { useJUnitPlatform() }
