plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)

    application
}

group = "com.pluralsight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.coroutines.core)
    implementation(libs.kotlin.logging)
    implementation(libs.logback)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.pluralsight.kai.MainKt")
}



tasks.test {
    useJUnitPlatform()
}